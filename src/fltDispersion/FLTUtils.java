//
package fltDispersion;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import common.MITgcmUtil.DataPrec;
import miniufo.diagnosis.MDate;
import miniufo.lagrangian.AttachedMeta;
import miniufo.lagrangian.Particle;
import miniufo.lagrangian.Record;
import miniufo.util.Region2D;
import miniufo.util.Region3D;


//
public final class FLTUtils{
	//
	public static final int undef=-999;
	
	
	/**
	 * Prevent construction.
	 */
	private FLTUtils(){}
	
	
	/**
	 * Write a list of particles to an initial file that required by MITgcm.
	 * 
	 * @param	ps		a list of particles
	 * @param	fname	file name for output
	 * @param	prec	data precision
	 */
	public static void toFLTInitFile(List<FltInitData> ps,String fname,DataPrec prec){
		int numOfTrajs=ps.size();
		
		if(numOfTrajs<1) throw new IllegalArgumentException("number of trajectory should be at least 1");
		
		try(FileOutputStream fos=new FileOutputStream(fname)){
			FileChannel fc=fos.getChannel();
			
			int oneTimeRec=FltInitData.nFields*(prec==DataPrec.float32?4:8)*(numOfTrajs+1);
			
			ByteBuffer buf=ByteBuffer.allocate(oneTimeRec);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			// for header
			FltInitData header=new FltInitData();
			header.npart =numOfTrajs;
			header.tstart=-1;
			header.xpart = 0;
			header.ypart = 0;
			header.kpart = 0;
			header.kfloat= numOfTrajs;
			header.iup   = 0;
			header.itop  = 0;
			header.tend  =-1;
			
			if(prec==DataPrec.float32){
				putIntoBufferAsFloat(buf,header);
				ps.stream().forEach(p->putIntoBufferAsFloat(buf,p));
				
			}else{
				putIntoBufferAsDouble(buf,header);
				ps.stream().forEach(p->putIntoBufferAsDouble(buf,p));
			}
			
			buf.clear();
			
			if(fc.write(buf)!=oneTimeRec) throw new IllegalArgumentException("incomplete writing");
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	
	/**
	 * read particle initial file and return a corresponding list.
	 * 
	 * @param	fname	file name for output
	 * @param	prec	data precision
	 */
	public static List<FltInitData> readFLTInitFile(String fname,DataPrec prec,Predicate<FltInitData> cond){
		List<FltInitData> ls=new ArrayList<>();
		
		try(FileInputStream fis=new FileInputStream(fname)){
			FileChannel fc=fis.getChannel();
			
			//int oneRecLen=FltInitData.nFields*(prec==DataPrec.float32?4:8);
			int fileSize =(int)fc.size();
			
			if(fileSize!=fc.size()) throw new IllegalArgumentException("too large file");
			
			ByteBuffer buf=ByteBuffer.allocate(fileSize);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			fc.read(buf); buf.clear();
			
			if(prec==DataPrec.float32){
				// for header
				readFromBufferAsFloat(buf);
				while(buf.hasRemaining()){
					FltInitData fid=readFromBufferAsFloat(buf);
					if(cond.test(fid)) ls.add(fid);
				}
				
			}else{
				// for header
				readFromBufferAsDouble(buf);
				while(buf.hasRemaining()){
					FltInitData fid=readFromBufferAsDouble(buf);
					if(cond.test(fid)) ls.add(fid);
				}
			}
			
			if(buf.hasRemaining()) throw new IllegalArgumentException("incomplete reading");
			
			buf.clear();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return ls;
	}
	
	public static List<FltInitData> readFLTInitFile(String fname,DataPrec prec){
		return readFLTInitFile(fname,prec,fid->true);
	}
	
	
	/**
	 * Deploy a patch of particles within a 2D region.
	 * 
	 * @param	r			2D region to deploy
	 * @param	del			spacing of deployment in both directions (degree)
	 * @param	tstr		start time for tracking
	 * @param	tend		 end  time for tracking
	 * @param	ensemble	number of particles deployed at the same point
	 * @param	prefix		prefix to distinguish between patches
	 */
	public static List<FltInitData> deployPatch2D(Region2D r,float del,float tstr,float tend,int ensemble,int prefix){
		int idx=1+prefix;
		
		List<FltInitData> ps=new ArrayList<>();
		
		int yc=Math.round((r.getYMax()-r.getYMin())/del);
		int xc=Math.round((r.getXMax()-r.getXMin())/del);
		
		if(((r.getYMax()-r.getYMin())%del)/del>0.99f) yc++;
		if(((r.getXMax()-r.getXMin())%del)/del>0.99f) xc++;
		
		for(int j=0;j<yc;j++){ float ypos=r.getYMin()+del*j;
		for(int i=0;i<xc;i++){ float xpos=r.getXMin()+del*i;
			for(int m=0;m<ensemble;m++) ps.add(deployAt2D(idx++,xpos,ypos,tstr,tend));
		}}
		
		return ps;
	}
	
	public static List<FltInitData> deployPatch2D(Region2D r,float del,float tstr,float tend,int ensemble){
		return deployPatch2D(r,del,tstr,tend,ensemble,0);
	}
	
	public static List<FltInitData> deployPatch2D(Region2D r,float del,int ensemble){
		return deployPatch2D(r,del,-1,-1,ensemble);
	}
	
	/**
	 * Deploy a patch of particles within a 3D region.
	 * 
	 * @param	r			3D region to deploy
	 * @param	delH		horizontal spacing of deployment in both directions (degree)
	 * @param	delV		vertical spacing of deployment (m)
	 * @param	ensemble	number of particles deployed at the same point
	 */
	public static List<FltInitData> deployPatch3D(Region3D r,float delH,float delV,float tstr,float tend,int ensemble){
		int idx=1;
		
		List<FltInitData> ps=new ArrayList<>();
		
		int zc=Math.round((r.getLevMax()-r.getLevMin())/delV);
		int yc=Math.round((r.getLatMax()-r.getLatMin())/delH);
		int xc=Math.round((r.getLonMax()-r.getLonMin())/delH);
		
		if(((r.getLevMax()-r.getLevMin())%delV)/delV>0.99f) zc++;
		if(((r.getLatMax()-r.getLatMin())%delH)/delH>0.99f) yc++;
		if(((r.getLonMax()-r.getLonMin())%delH)/delH>0.99f) xc++;
		
		for(int k=0;k<zc;k++){ float lev=r.getLevMin()+delV*k;
		for(int j=0;j<yc;j++){ float lat=r.getLatMin()+delH*j;
		for(int i=0;i<xc;i++){ float lon=r.getLonMin()+delH*i;
			for(int m=0;m<ensemble;m++) ps.add(deployAt3D(idx++,lon,lat,lev,tstr,tend));
		}}}
		
		return ps;
	}
	
	public static List<FltInitData> deployPatch3D(Region3D r,float delH,float delV,int ensemble){
		return deployPatch3D(r,delH,delV,-1,-1,ensemble);
	}
	
	
	/**
	 * Write trajectories if the start point of a FltParticle is in a given 2D region,
	 * as well as a gs for plotting all the trajectories
	 * 
	 * @param	ps		a list of FltParticle data
	 * @param	path	folder for output
	 * @param	r		a given 2D region
	 */
	public static void writeTrajAndGS(List<FltParticle> ps,String path,Region2D r){
		StringBuffer sb=new StringBuffer();
		sb.append("'sdfopen d:/Data/NCEP/OriginalNC/air.2m.mon.mean.nc'\n");
		sb.append("'enable print "+path+"trajectory.gmf'\n\n");
		sb.append("'set grid off'\n");
		sb.append("'set grads off'\n");
		sb.append("'set lon "+r.getXMin()+" "+r.getXMax()+"'\n");
		sb.append("'set lat "+r.getYMin()+" "+r.getYMax()+"'\n");
		sb.append("'set mpdset mres'\n\n");
		sb.append("'setvpage 1 1.3 1 1'\n");
		sb.append("'setlopts 7 0.18 5 5'\n");
		sb.append("'set line 2 1 0.1'\n");
		sb.append("'set cmin 99999'\n");
		sb.append("'d air'\n\n");
		
		for(FltParticle p:ps){
			float lon=p.recs.get(0).getXPos();
			float lat=p.recs.get(0).getYPos();
			
			if(r.inRange(lon,lat)){
				p.toTrajectoryFile(path);
				sb.append("'tctrack "+path+p.id+".txt'\n");
			}
		}
		
		sb.append("\n'draw title GDP drifter trajectories'\n\n");
		sb.append("\n'basemap L 15 1 M'\n\n");
		sb.append("'print'\n");
		sb.append("'c'\n\n");
		sb.append("'disable print'\n");
		sb.append("'close 1'\n");
		sb.append("'reinit'\n");
		
		try(FileWriter fw=new FileWriter(path+"trajectory.gs")){
			fw.write(sb.toString());
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	/**
	 * Write trajectories if the start point of a FltParticle is in a given 3D region,
	 * as well as a gs for plotting all the trajectories
	 * 
	 * @param	ps		a list of FltParticle data
	 * @param	path	folder for output
	 * @param	r		a given 3D region
	 */
	public static void writeTrajAndGS(List<FltParticle> ps,String path,Region3D r){
		StringBuffer sb=new StringBuffer();
		sb.append("'open "+path+"Stat.ctl'\n");
		sb.append("'enable print "+path+"Traj3D.gmf'\n\n");
		sb.append("'set grid off'\n");
		sb.append("'set grads off'\n\n");
		sb.append("'setvpage 1 1.3 1 1'\n");
		sb.append("'setlopts 7 0.18 1000 20'\n");
		sb.append("'set line 2 1 0.1'\n");
		sb.append("'set yflip on'\n");
		sb.append("'d t(t=50)'\n\n");
		
		for(FltParticle p:ps){
			float lon=p.recs.get(0).getXPos();
			float lat=p.recs.get(0).getYPos();
			float lev=Math.abs(p.recs.get(0).getZPos());
			
			if(r.inRange(lon,lat,lev)){
				p.toTrajectoryFile(path);
				sb.append("'fltTrack "+path+p.id+".txt'\n");
			}
		}
		
		sb.append("\n'draw title particle trajectories'\n\n");
		sb.append("'print'\n");
		sb.append("'c'\n\n");
		sb.append("'disable print'\n");
		sb.append("'close 1'\n");
		sb.append("'reinit'\n");
		
		try(FileWriter fw=new FileWriter(path+"Traj3D.gs")){
			fw.write(sb.toString());
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	
	/**
	 * Read a list of particles from the output of MITgcm FLT package.
	 * 
	 * @param	directory	folder that contains all the output files (one per tile)
	 * @param	basetime	start time of the output
	 */
	public static List<FltParticle> readFLTTrajectory(String directory,MDate basetime){
		return readFLTTrajectory(directory,basetime,r->true);
	}
	
	public static List<FltParticle> readFLTTrajectory(String directory,MDate basetime,Predicate<FltRecord> cond){
		int zlevs=1;
		int recLen=(9+4*zlevs)*4;
		int[] totalPS=new int[1];
		
		Stream<Path> files=null;
		
		try{ files=Files.list(Paths.get(directory)).filter(p->p.getFileName().toString().matches("float_trajectories.*.data"));}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		List<FltRecord> records=new ArrayList<>();
		
		files.forEach(p->{
			try(FileChannel fc=FileChannel.open(p)){
				long fileLen=fc.size();
				
				if(fileLen%recLen!=0) throw new IllegalArgumentException("incomplete trajectory file");
				
				if(fileLen>recLen){
					ByteBuffer buf=ByteBuffer.allocate(recLen*500);
					
					buf.order(ByteOrder.BIG_ENDIAN);
					
					// read header and get total number of particles
					fc.read(buf); buf.clear();
					
					FltRecord header=readFromBuffer(1,buf,basetime); buf.clear(); fc.position(recLen);
					
					totalPS[0]=Math.round(header.getXIdx());
					
					// read trajectory data
					while(fc.position()<fileLen){
						int tlen=fc.read(buf)/recLen; buf.clear();
						
						for(int l=0;l<tlen;l++){
							FltRecord r=readFromBuffer(1,buf,basetime);
							
							if(r!=null&&cond.test(r)) records.add(r);
						}
					}
				}
				
			}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		});
		
		Map<Float,List<FltRecord>> res=records.stream().collect(Collectors.groupingBy(rec->rec.getID()));
		
		List<FltParticle> ls=new ArrayList<>(res.size());
		
		res.forEach((k,v)->{
			v.sort((r1,r2)->Long.compare(r1.getTime(),r2.getTime()));
			ls.add(new FltParticle(Math.round(k),v));
		});
		
		ls.sort((p1,p2)->p1.id-p2.id);
		
		if(ls.size()!=totalPS[0])
		System.out.println("warning: size of list ("+ls.size()+") do not equal header ("+totalPS[0]+")");
		
		return ls;
	}
	
	public static List<FltParticle> readFLTCombinedTrajectory(String file,MDate basetime){
		int zlevs=1;
		int recLen=(9+4*zlevs)*4;
		
		List<FltRecord> records=new ArrayList<>();
		
		try(FileChannel fc=FileChannel.open(Paths.get(file))){
			long fileLen=fc.size();
			
			if(fileLen%recLen!=0) throw new IllegalArgumentException("incomplete trajectory file");
			
			records=new ArrayList<>((int)(fileLen/recLen));
			
			ByteBuffer buf=ByteBuffer.allocate(recLen*5000);
			
			buf.order(ByteOrder.BIG_ENDIAN);
			
			// read trajectory data
			while(fc.position()<fileLen){
				int tlen=fc.read(buf)/recLen; buf.clear();
				
				for(int l=0;l<tlen;l++){
					FltRecord fr=readFromBuffer(1,buf,basetime);
					
					if(!isHeader(fr)&&fr!=null) records.add(fr);
					if(isHeader(fr)){
						System.out.println(fr);
						//totalPS=Math.round(fr.getXIdx());
					}
				}
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		Map<Float,List<FltRecord>> res=records.stream().collect(Collectors.groupingBy(rec->rec.getID()));
		
		List<FltParticle> ls=new ArrayList<>(res.size());
		
		res.forEach((k,v)->{
			v.sort((r1,r2)->Long.compare(r1.getTime(),r2.getTime()));
			ls.add(new FltParticle(Math.round(k),v));
		});
		
		ls.sort((p1,p2)->p1.id-p2.id);
		
		return ls;
	}
	
	/**
	 * Read a list of particle profiles from the output of MITgcm FLT package.
	 * 
	 * @param	directory	folder that contains all the output files (one per tile)
	 * @param	basetime	start time of the output
	 */
	public static List<FltParticle> readFLTProfile(int zlevs,String directory,MDate basetime){
		int recLen=(9+4*zlevs)*4;
		int[] totalPS=new int[1];
		
		Stream<Path> files=null;
		
		try{ files=Files.list(Paths.get(directory)).filter(p->p.getFileName().toString().matches("float_profiles.*.data"));}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		List<FltRecord> records=new ArrayList<>();
		
		files.forEach(p->{
			try(FileChannel fc=FileChannel.open(p)){
				long fileLen=fc.size();
				
				if(fileLen%recLen!=0) throw new IllegalArgumentException("incomplete trajectory file");
				
				if(fileLen>recLen){
					ByteBuffer buf=ByteBuffer.allocate(recLen*200);
					
					buf.order(ByteOrder.BIG_ENDIAN);
					
					// read header and get total number of particles
					fc.read(buf); buf.clear();
					
					FltRecord header=readFromBuffer(zlevs,buf,basetime); buf.clear(); fc.position(recLen);
					
					totalPS[0]=Math.round(header.getXIdx());
					
					// read trajectory data
					while(fc.position()<fileLen){
						int tlen=fc.read(buf)/recLen; buf.clear();
						
						for(int l=0;l<tlen;l++){
							FltRecord r=readFromBuffer(zlevs,buf,basetime);
							if(r!=null) records.add(r);
						}
					}
				}
				
			}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		});
		
		Map<Float,List<FltRecord>> res=records.stream().collect(Collectors.groupingBy(rec->rec.getID()));
		
		List<FltParticle> ls=new ArrayList<>(res.size());
		
		res.forEach((k,v)->{
			v.sort((r1,r2)->Long.compare(r1.getTime(),r2.getTime()));
			ls.add(new FltParticle(Math.round(k),v));
		});
		
		ls.sort((p1,p2)->p1.id-p2.id);
		
		if(ls.size()!=totalPS[0])
		throw new IllegalArgumentException("size of list ("+ls.size()+") do not equal header ("+totalPS[0]+")");
		
		return ls;
	}
	
	public static List<FltParticle> readFLTCombinedProfile(int zlevs,String file,MDate basetime){
		int recLen=(9+4*zlevs)*4;
		int totalPS=0;
		
		List<FltRecord> records=new ArrayList<>();
		
		try(FileChannel fc=FileChannel.open(Paths.get(file))){
			long fileLen=fc.size();
			
			if(fileLen%recLen!=0) throw new IllegalArgumentException("incomplete trajectory file");
			
			ByteBuffer buf=ByteBuffer.allocate(recLen*500);
			
			buf.order(ByteOrder.BIG_ENDIAN);
			
			// read trajectory data
			while(fc.position()<fileLen){
				int tlen=fc.read(buf)/recLen; buf.clear();
				
				for(int l=0;l<tlen;l++){
					FltRecord fr=readFromBuffer(zlevs,buf,basetime);
					
					if(!isHeader(fr)&&fr!=null) records.add(fr);
					else totalPS=Math.round(fr.getXIdx());
				}
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		Map<Float,List<FltRecord>> res=records.stream().collect(Collectors.groupingBy(rec->rec.getID()));
		
		List<FltParticle> ls=new ArrayList<>(res.size());
		
		res.forEach((k,v)->{
			v.sort((r1,r2)->Long.compare(r1.getTime(),r2.getTime()));
			ls.add(new FltParticle(Math.round(k),v));
		});
		
		ls.sort((p1,p2)->p1.id-p2.id);
		
		if(ls.size()!=totalPS)
		throw new IllegalArgumentException("size of list ("+ls.size()+") do not equal header ("+totalPS+")");
		
		return ls;
	}
	
	
	/**
	 * Combine all the files of particles output from MITgcm FLT package.
	 * 
	 * @param	directory	folder that contains all the output files (one per tile)
	 * @param	basetime	start time of the output
	 */
	public static void combineFLTOutIntoBin(String directory,MDate basetime){
		List<FltParticle> fps=FLTUtils.readFLTTrajectory(directory,basetime,rec->true);
		
		System.out.println("finish reading "+fps.size()+" FltParticles");
		try(ObjectOutputStream oos=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(directory+"float_trajAll.bin"),819200))){
			for(FltParticle p:fps) oos.writeObject(p);
			oos.writeObject(null);	// this null is used as an EOF by readObject()
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	/**
	 * read FLT binary file [written by the combineFLTOutIntoBin()] as a Stream of particles.
	 * 
	 * @param	directory	folder that contains all the output files (one per tile)
	 * @param	basetime	start time of the output
	public static Stream<Particle> readFLTTrajectory(String combinedFile,Predicate<FltRecord> cond,Function<FltParticle,Particle> mapper){
		ObjectInputStream ois=null;
		
		try{ois=new ObjectInputStream(new BufferedInputStream(new FileInputStream(combinedFile),819200));}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		final ObjectInputStream stream=ois;
		
		Supplier<FltParticle> gen=()->{
			try{ return (FltParticle)stream.readObject();}
			catch(IOException|ClassNotFoundException e){ e.printStackTrace(); System.exit(0);}
			
			throw new IllegalArgumentException("should not happen");
		};
		
		Runnable close=()->{
			try{ stream.close();}
			catch(IOException e){ e.printStackTrace(); System.exit(0);}
		};
		
		return Stream.generate(gen).onClose(close).map(mapper);
	}*/
	
	
	/**
	 * Convert a FltParticle into a Particle.
	 * 
	 * @param	fltP	FLT Particle
	 * @param	llpos	lat/lon position
	 * @param	meta	meta of attached data
	 */
	public static Particle toParticle(FltParticle fltP,boolean llpos,AttachedMeta... meta){
		int attLen=meta.length;
		
		Particle p=new Particle(Integer.toString(fltP.id),fltP.recs.size(),attLen,llpos);
		
		for(FltRecord fr:fltP.recs) p.addRecord(toRecord(fr,attLen));
		
		p.setAttachedMeta(meta);
		
		return p;
	}
	
	public static Particle toParticle(FltParticle fltP,boolean llpos){ return toParticle(fltP,llpos,Particle.UVEL,Particle.VVEL);}
	
	
	/**
	 * Change FltRecord into Record.
	 * 
	 * @param	fltRec	FLT record
	 * @param	attLen	length of attached variables
	 */
	public static Record toRecord(FltRecord fltRec,int attLen){
		long  time=fltRec.getTime();
		float xpos=fltRec.getXPos();
		float ypos=fltRec.getYPos();
		
		Record r=new Record(time,xpos,ypos,attLen);
		
		r.setData(Particle.UVEL,fltRec.getUVel()[0]);
		r.setData(Particle.VVEL,fltRec.getVVel()[0]);
		
		return r;
	}
	
	public static Record toRecord(FltRecord fltRec){ return toRecord(fltRec,2);}
	
	
	/*** helper methods ***/
	private static FltRecord readFromBuffer(int zlevs,ByteBuffer buf,MDate basetime){
		float[] uvel=new float[zlevs];
		float[] vvel=new float[zlevs];
		float[] temp=new float[zlevs];
		float[] salt=new float[zlevs];
		
		float id=buf.getFloat();	// A unique float identifier (1,2,3,...)
		long time=basetime.addSeconds(Math.round(buf.getFloat())).getLongTime();	// current time of observation (in s) + base time
		
		float xpos=buf.getFloat();	// x position of float (in units of XC)
		float ypos=buf.getFloat();	// y position of float (in units of YC)
		float zpos=buf.getFloat();	// z position of float (in units of ZC)
		float xidx=buf.getFloat();	// x-direction grid
		float yidx=buf.getFloat();	// y-direction grid
		float zidx=buf.getFloat();	// z-direction grid
		float pres=buf.getFloat();	// pressure (dbar)
		
		for(int k=0;k<zlevs;k++) uvel[k]=buf.getFloat();	// u-direction velocity (m/s)
		for(int k=0;k<zlevs;k++) vvel[k]=buf.getFloat();	// v-direction velocity (m/s)
		for(int k=0;k<zlevs;k++) temp[k]=buf.getFloat();	// temperature (degree)
		for(int k=0;k<zlevs;k++) salt[k]=buf.getFloat();	// salinity (psu)
		
		boolean isUndef=true;
		for(int k=0;k<zlevs;k++)
		if(uvel[k]!=undef&&vvel[k]!=undef&&temp[k]!=undef&&salt[k]!=undef){ isUndef=false; break;}
		
		if(isUndef) return null;
		else return new FltRecord(id,time,xpos,ypos,zpos,xidx,yidx,zidx,pres,uvel,vvel,temp,salt);
	}
	
	private static FltInitData deployAt2D(float id,float x,float y,float tstr,float tend){
		FltInitData re=new FltInitData();
		
		re.npart =id;					// a unique float identifier (1,2,3,...)
		re.tstart=tstr;					// start date of integration of float (in s)
										// Note: If tstart=-1 floats are integrated right from the beginning
		re.xpart =x;					// x position of float (in units of XC)
		re.ypart =y;					// y position of float (in units of YC)
		re.kpart =0;					// actual vertical level of float
		re.kfloat=5;					// target level of float (should be the same as kpart at the beginning)
		re.iup   =0;					// flag if the float
										// - should profile   ( >  0 = return cycle (in s) to surface)
										// - remain at depth  ( =  0 )
										// - is a 3D float    ( = -1 ).
										// - should be advected WITHOUT additional noise ( = -2 ).
										//   (This implies that the float is non-profiling)
										// - is a mooring     ( = -3 ), i.e. the float is not advected
		re.itop  =0;					// time of float the surface (in s)
		re.tend  =tend;					// end  date of integration of float (in s)
										// Note: If tend=-1 floats are integrated till the end of the integration
		
		return re;
	}
	
	private static FltInitData deployAt3D(float id,float x,float y,float z,float tstr,float tend){
		FltInitData re=new FltInitData();
		
		re.npart =id;					// a unique float identifier (1,2,3,...)
		re.tstart=tstr;					// start date of integration of float (in s)
										// Note: If tstart=-1 floats are integrated right from the beginning
		re.xpart =x;					// x position of float (in units of XC)
		re.ypart =y;					// y position of float (in units of YC)
		re.kpart =-z;					// actual vertical level of float
		re.kfloat=-z;					// target level of float (should be the same as kpart at the beginning)
		re.iup   =-1;					// flag if the float
										// - should profile   ( >  0 = return cycle (in s) to surface)
										// - remain at depth  ( =  0 )
										// - is a 3D float    ( = -1 ).
										// - should be advected WITHOUT additional noise ( = -2 ).
										//   (This implies that the float is non-profiling)
										// - is a mooring     ( = -3 ), i.e. the float is not advected
		re.itop  =0;					// time of float the surface (in s)
		re.tend  =tend;					// end  date of integration of float (in s)
										// Note: If tend=-1 floats are integrated till the end of the integration
		
		return re;
	}
	
	private static FltInitData readFromBufferAsFloat(ByteBuffer buf){
		FltInitData fid=new FltInitData();
		
		fid.npart =buf.getFloat();
		fid.tstart=buf.getFloat();
		fid.xpart =buf.getFloat();
		fid.ypart =buf.getFloat();
		fid.kpart =buf.getFloat();
		fid.kfloat=buf.getFloat();
		fid.iup   =buf.getFloat();
		fid.itop  =buf.getFloat();
		fid.tend  =buf.getFloat();
		
		return fid;
	}
	
	private static FltInitData readFromBufferAsDouble(ByteBuffer buf){
		FltInitData fid=new FltInitData();
		
		fid.npart =(float)buf.getDouble();
		fid.tstart=(float)buf.getDouble();
		fid.xpart =(float)buf.getDouble();
		fid.ypart =(float)buf.getDouble();
		fid.kpart =(float)buf.getDouble();
		fid.kfloat=(float)buf.getDouble();
		fid.iup   =(float)buf.getDouble();
		fid.itop  =(float)buf.getDouble();
		fid.tend  =(float)buf.getDouble();
		
		return fid;
	}
	
	private static void putIntoBufferAsFloat(ByteBuffer buf,FltInitData fid){
		buf.putFloat(fid.npart );
		buf.putFloat(fid.tstart);
		buf.putFloat(fid.xpart );
		buf.putFloat(fid.ypart );
		buf.putFloat(fid.kpart );
		buf.putFloat(fid.kfloat);
		buf.putFloat(fid.iup   );
		buf.putFloat(fid.itop  );
		buf.putFloat(fid.tend  );
	}
	
	private static void putIntoBufferAsDouble(ByteBuffer buf,FltInitData fid){
		buf.putDouble(fid.npart );
		buf.putDouble(fid.tstart);
		buf.putDouble(fid.xpart );
		buf.putDouble(fid.ypart );
		buf.putDouble(fid.kpart );
		buf.putDouble(fid.kfloat);
		buf.putDouble(fid.iup   );
		buf.putDouble(fid.itop  );
		buf.putDouble(fid.tend  );
	}
	
	private static boolean isHeader(FltRecord fr){
		return fr.getID()==0||(fr.getPres()==0&&fr.getUVel()[0]==0&&
			fr.getZIdx()==0&&fr.getVVel()[0]==0&&fr.getTemp()[0]==0&&fr.getSalt()[0]==0
		);
	}
	
	
	/*** test **
	public static void main(String[] args){
		//List<FltParticle> ls=readFLTTrajectory("D:/Data/MITgcm/flt/float/",new MDate(2000,1,1));
		//writeTrajAndGS(ls,"D:/Data/MITgcm/flt/float/TXT/",new Region3D(0,0,00,13300,200,200));
		
		//List<FltParticle> ls=readFLTCombinedTrajectory("D:/Data/MITgcm/flt/float/float_trajectories.001.001.data",new MDate(2000,1,1));System.out.println(ls.size());
		//writeTrajAndGS(ls,"D:/Data/MITgcm/flt/float/TXT/",new Region3D(0,0,00,13300,200,200));
		
		//List<FltParticle> ls=readFLTProfile(1,"D:/Data/MITgcm/flt/float/",new MDate(2000,1,1));
		//writeTrajAndGS(ls,"D:/Data/MITgcm/flt/float/TXT/",new Region(130,10,170,40));
		
		//List<FltParticle> ls=readFLTCombinedProfile(1,"D:/Data/MITgcm/flt/float/float_profiles.dat",new MDate(2000,1,1));
		//writeTrajAndGS(ls,"D:/Data/MITgcm/flt/float/TXT/",new Region(130,10,170,40));
		
		
		
		//List<FltInitData> ps=deployPatch2D(new Region2D(145,30,155,40),0.5f,1);System.out.println(ps.size());
		//toFLTInitFile(ps,"D:/Data/MITgcm/flt/float/flt_init.bin");
		
		//List<FltInitData> ps=deployPatch3D(new Region3D(2000,0,50,6000,200,100),200,10,1);System.out.println(ps.size());
		//toFLTInitFile(ps,"D:/Data/MITgcm/flt/float/flt_init.bin");
		
		List<FltInitData> ls=readFLTInitFile("d:/Data/MITgcm/barotropicDG/BetaCartRL/fltInit_11km_All.bin",DataPrec.float32);
		System.out.println(ls.size()+" "+ls.get(10));
	}*/
}
