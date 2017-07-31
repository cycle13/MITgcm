//
package fltDispersion;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import miniufo.application.basic.ThermoDynamicMethodsInSC;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.MDate;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.SphericalSpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;
import miniufo.lagrangian.Particle;
import miniufo.lagrangian.Record;
import miniufo.util.Region2D;
import miniufo.util.Region3D;


//
public final class FLTUtils{
	
	/**
	 * prevent construction
	 */
	private FLTUtils(){}
	
	
	/**
	 * Write a list of particles to an initial file that required by MITgcm.
	 * 
	 * @param	ps		a list of particles
	 * @param	fname	file name for output
	 */
	public static void toFLTInitFile(List<FltInitData> ps,String fname){
		int numOfTrajs=ps.size();
		
		if(numOfTrajs<1) throw new IllegalArgumentException("number of trajectory should be at least 1");
		
		try(FileOutputStream fos=new FileOutputStream(fname)){
			FileChannel fc=fos.getChannel();
			
			int oneTimeRec=FltInitData.nFields*4*(numOfTrajs+1);
			
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
			
			putIntoBuffer(buf,header);
			
			// for initial records
			ps.stream().forEach(p->putIntoBuffer(buf,p));
			
			buf.clear();
			
			if(fc.write(buf)!=oneTimeRec) throw new IllegalArgumentException("incomplete writing");
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	/**
	 * deploy a patch of particles
	 * 
	 * @param	r			region to deploy
	 * @param	del			spacing of deployment in both directions (degree)
	 * @param	ensemble	number of particles deployed at the same point
	 * @param	initLen		initial length of the particle (pre-allocating memory)
	 * @param	func		function that maps the current Record to StochasticParams
	 */
	public static List<FltInitData> deployPatch2D(Region2D r,float del,int ensemble){
		int idx=1;
		
		List<FltInitData> ps=new ArrayList<>();
		
		int yc=Math.round((r.getLatMax()-r.getLatMin())/del);
		int xc=Math.round((r.getLonMax()-r.getLonMin())/del);
		
		if(((r.getLatMax()-r.getLatMin())%del)/del>0.99f) yc++;
		if(((r.getLonMax()-r.getLonMin())%del)/del>0.99f) xc++;
		
		for(int j=0;j<yc;j++){ float lat=r.getLatMin()+del*j;
		for(int i=0;i<xc;i++){ float lon=r.getLonMin()+del*i;
			for(int m=0;m<ensemble;m++) ps.add(deployAt2D(idx++,lon,lat));
		}}
		
		return ps;
	}
	
	public static List<FltInitData> deployPatch3D(Region3D r,float delH,float delV,int ensemble){
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
			for(int m=0;m<ensemble;m++) ps.add(deployAt3D(idx++,lon,lat,lev));
		}}}
		
		return ps;
	}
	
	/**
	 * output trajectories if the start point is in a given region,
	 * as well as a gs for plotting all the trajectories
	 * 
	 * @param	ls		a list of FltParticle data
	 * @param	path	folder for output
	 * @param	r		a given region
	 */
	public static void writeTrajAndGS(List<FltParticle> ps,String path,Region2D r){
		StringBuffer sb=new StringBuffer();
		sb.append("'sdfopen d:/Data/NCEP/OriginalNC/air.2m.mon.mean.nc'\n");
		sb.append("'enable print "+path+"trajectory.gmf'\n\n");
		sb.append("'set grid off'\n");
		sb.append("'set grads off'\n");
		sb.append("'set lon "+r.getLonMin()+" "+r.getLonMax()+"'\n");
		sb.append("'set lat "+r.getLatMin()+" "+r.getLatMax()+"'\n");
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
	 * @param	fname		file name for output
	 * @param	numOfTrajs	number of trajectories contained in the file
	 */
	public static List<FltParticle> readFLTTrajectory(String directory,MDate basetime){
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
						
						for(int l=0;l<tlen;l++) records.add(readFromBuffer(1,buf,basetime));
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
					
					if(!isHeader(fr)) records.add(fr);
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
						
						for(int l=0;l<tlen;l++) records.add(readFromBuffer(zlevs,buf,basetime));
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
					
					if(!isHeader(fr)) records.add(fr);
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
		
		FltRecord re=new FltRecord(id,time,xpos,ypos,zpos,xidx,yidx,zidx,pres,uvel,vvel,temp,salt);
		
		return re;
	}
	
	private static FltInitData deployAt2D(float id,float lon,float lat){
		FltInitData re=new FltInitData();
		
		re.npart =id;					// a unique float identifier (1,2,3,...)
		re.tstart=-1;					// start date of integration of float (in s)
										// Note: If tstart=-1 floats are integrated right from the beginning
		re.xpart =lon;					// x position of float (in units of XC)
		re.ypart =lat;					// y position of float (in units of YC)
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
		re.tend  =-1;					// end  date of integration of float (in s)
										// Note: If tend=-1 floats are integrated till the end of the integration
		
		return re;
	}
	
	private static FltInitData deployAt3D(float id,float lon,float lat,float lev){
		FltInitData re=new FltInitData();
		
		re.npart =id;					// a unique float identifier (1,2,3,...)
		re.tstart=-1;					// start date of integration of float (in s)
										// Note: If tstart=-1 floats are integrated right from the beginning
		re.xpart =lon;					// x position of float (in units of XC)
		re.ypart =lat;					// y position of float (in units of YC)
		re.kpart =-lev;					// actual vertical level of float
		re.kfloat=-lev;					// target level of float (should be the same as kpart at the beginning)
		re.iup   =-1;					// flag if the float
										// - should profile   ( >  0 = return cycle (in s) to surface)
										// - remain at depth  ( =  0 )
										// - is a 3D float    ( = -1 ).
										// - should be advected WITHOUT additional noise ( = -2 ).
										//   (This implies that the float is non-profiling)
										// - is a mooring     ( = -3 ), i.e. the float is not advected
		re.itop  =0;					// time of float the surface (in s)
		re.tend  =-1;					// end  date of integration of float (in s)
										// Note: If tend=-1 floats are integrated till the end of the integration
		
		return re;
	}
	
	private static void putIntoBuffer(ByteBuffer buf,FltInitData fid){
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
	
	private static boolean isHeader(FltRecord fr){
		return fr.getID()==0||(fr.getPres()==0&&fr.getUVel()[0]==0&&
			fr.getZIdx()==0&&fr.getVVel()[0]==0&&fr.getTemp()[0]==0&&fr.getSalt()[0]==0
		);
	}
	
	
	/*** test ***/
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
		
		/***
		DiagnosisFactory df=DiagnosisFactory.parseFile("D:/Data/MITgcm/flt/float/Stat.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		SphericalSpatialModel ssm=new SphericalSpatialModel(dd);
		
		Range rng=new Range("",dd);
		
		Variable[] vs=df.getVariables(rng,"u","v","w","T","S");
		
		ThermoDynamicMethodsInSC tm=new ThermoDynamicMethodsInSC(ssm);
		
		Variable sgm=tm.cPotentialDensity(vs[4],vs[3]);
		
		DataWrite dw=DataIOFactory.getDataWrite(dd,"d:/Data/MITgcm/flt/float/Stat2.dat");
		dw.writeData(dd,vs[0],vs[1],vs[2],vs[3],vs[4],sgm); dw.closeFile();*/
	}
}
