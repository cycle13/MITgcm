//
package SCS12;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import miniufo.io.IOUtil;
import miniufo.util.GridDataFetcher;
import static SCS12.ConstructGrids.*;


// extract bathymetry data from ETOPO 1
public final class SCS12All{
	//
	public static void main(String[] args){
		/*** for bath ***/
		//extractBath("d:/Data/Bathymetry/ETOPO/ETOPO1_Bed.ctl",path+"BATH/Bath_ETOPO1.bin","z");
		//extractBath("d:/Data/Bathymetry/GEBCO/gebco_08_75_-25_165_45.ctl",path+"BATH/Bath_GEBCO.bin","z");
		//extractBath("d:/Data/Bathymetry/SRTM/SRTM.ctl",path+"BATH/Bath_SRTM.bin","z");
		
		/*** for IC ***/
		//prepareSODA224();
		prepareSODA226();
		//prepareGODAS();
		//prepareHYCOM();
		
		/*** for EXF ***/
		//extractEXF("d:/Data/NCEP/FreshWaterFlux/EmP.ctl",path+"EXF/EXFemp_NCEP.dat","emp");
		//extractEXF("d:/Data/NCEP/QNet/hflux.ctl",path+"EXF/EXFhflux_NCEP.dat","hflux");
		//extractEXF("d:/Data/NCEP/QNet/swflux.ctl",path+"EXF/EXFswflux_NCEP.dat","swflux");
		//extractEXF("d:/Data/CCMP/CCMPStressClim.ctl",path+"EXF/EXFtauxLPCCMP.dat","tauxLP");
		//extractEXF("d:/Data/CCMP/CCMPStressClim.ctl",path+"EXF/EXFtauyLPCCMP.dat","tauyLP");
	}
	
	static void prepareHYCOM(){
		/*** for IC HYCOM ***/
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICtHYCOM.dat","t",false);
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICsHYCOM.dat","s",false);
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICuHYCOM.dat","u",false);
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICvHYCOM.dat","v",false);
		
		/*** for EXF HYCOM ***/
		extractEXF("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"EXF/EXFsstHYCOM.dat","t");
		extractEXF("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"EXF/EXFsssHYCOM.dat","s");
		extractEXF("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"EXF/EXFetaHYCOM.dat","eta");
		
		/*** for OBCS HYCOM ***/
		extractOBCS("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"OBCS/OBCStHYCOM","t");
		extractOBCS("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"OBCS/OBCSsHYCOM","s");
		extractOBCS("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"OBCS/OBCSuHYCOM","u");
		extractOBCS("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"OBCS/OBCSvHYCOM","v");
	}
	
	static void prepareGODAS(){
		/*** for IC GODAS ***/
		extractIC("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAtempClim.ctl",path+"IC/ICtempSODA224.dat","temp",false);
		extractIC("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAsaltClim.ctl",path+"IC/ICsaltSODA224.dat","salt",false);
		extractIC("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAuClim.ctl",path+"IC/ICuSODA224.dat","u",false);
		extractIC("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAvClim.ctl",path+"IC/ICvSODA224.dat","v",false);
		
		/*** for EXF GODAS ***/
		extractEXF("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAtauxClim.ctl",path+"EXF/EXFtauxSODA224.dat","taux");
		extractEXF("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAtauyClim.ctl",path+"EXF/EXFtauySODA224.dat","tauy");
		extractEXF("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAtempClim.ctl",path+"EXF/EXFsstSODA224.dat","temp");
		extractEXF("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAsaltClim.ctl",path+"EXF/EXFsssSODA224.dat","salt");
		
		/*** for OBCS GODAS ***/
		extractOBCS("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAtempClim.ctl",path+"OBCS/OBCStempSODA224","temp");
		extractOBCS("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAsaltClim.ctl",path+"OBCS/OBCSsaltSODA224","salt");
		extractOBCS("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAuClim.ctl"   ,path+"OBCS/OBCSuSODA224"   ,"u"   );
		extractOBCS("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAvClim.ctl"   ,path+"OBCS/OBCSvSODA224"   ,"v"   );
	}
	
	static void prepareSODA224(){
		/*** for IC SODA 2.2.4 ***/
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICtempSODA224.dat","temp",false);
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICsaltSODA224.dat","salt",false);
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICuSODA224.dat"   ,"u"   ,false);
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICvSODA224.dat"   ,"v"   ,false);
		
		/*** for EXF SODA 2.2.4 ***/
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFtauxSODA224.dat","taux");
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFtauySODA224.dat","tauy");
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFsstSODA224.dat" ,"temp");
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFsssSODA224.dat" ,"salt");
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFsshSODA224.dat" ,"ssh" );
		
		/*** for OBCS SODA 2.2.4 ***/
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCStempSODA224","temp");
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCSsaltSODA224","salt");
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCSuSODA224"   ,"u"   );
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCSvSODA224"   ,"v"   );
	}
	
	static void prepareSODA226(){
		/*** for IC SODA 2.2.6 ***/
		extractIC("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"IC/ICtempSODA226.dat","temp",false);
		extractIC("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"IC/ICsaltSODA226.dat","salt",false);
		extractIC("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"IC/ICuSODA226.dat"   ,"u"   ,false);
		extractIC("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"IC/ICvSODA226.dat"   ,"v"   ,false);
		
		/*** for EXF SODA 2.2.6 ***/
		extractEXF("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"EXF/EXFtauxSODA226.dat","taux");
		extractEXF("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"EXF/EXFtauySODA226.dat","tauy");
		extractEXF("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"EXF/EXFsstSODA226.dat" ,"temp");
		extractEXF("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"EXF/EXFsssSODA226.dat" ,"salt");
		extractEXF("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"EXF/EXFsshSODA226.dat" ,"ssh" );
		
		/*** for OBCS SODA 2.2.6 ***/
		extractOBCS("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"OBCS/OBCStempSODA226","temp");
		extractOBCS("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"OBCS/OBCSsaltSODA226","salt");
		extractOBCS("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"OBCS/OBCSuSODA226"   ,"u"   );
		extractOBCS("d:/Data/SODA/2.2.6/SODA226Clim_1993_2003.ctl",path+"OBCS/OBCSvSODA226"   ,"v"   );
	}
	
	static void extractOBCS(String ipath,String opath,String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		
		Variable n=new Variable("n",true,new Range(12,zgrid,1,xgrid));
		Variable s=new Variable("s",true,new Range(12,zgrid,1,xgrid));
		Variable e=new Variable("e",true,new Range(12,zgrid,ygrid,1));
		Variable w=new Variable("w",true,new Range(12,zgrid,ygrid,1));
		
		n.setUndef(dd.getUndef(null));
		s.setUndef(dd.getUndef(null));
		e.setUndef(dd.getUndef(null));
		w.setUndef(dd.getUndef(null));
		
		for(int l=0;l<12;l++){
			float[][][] xyzbuf=gdf.prepareXYZBuffer(vname,l+1,1,dd.getZCount(),6);
			
			float[][][] ndata=n.getData()[l];
			float[][][] sdata=s.getData()[l];
			float[][][] edata=e.getData()[l];
			float[][][] wdata=w.getData()[l];
			
			for(int k=0;k<zgrid;k++)
			for(int i=0;i<xgrid;i++){
				ndata[k][0][i]=gdf.fetchXYZBuffer((float)lons[i],(float)lats[ygrid-1],(float)levs[k+1],xyzbuf);
				sdata[k][0][i]=gdf.fetchXYZBuffer((float)lons[i],(float)lats[0      ],(float)levs[k+1],xyzbuf);
			}
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++){
				edata[k][j][0]=gdf.fetchXYZBuffer((float)lons[xgrid-1],(float)lats[j],(float)levs[k+1],xyzbuf);
				wdata[k][j][0]=gdf.fetchXYZBuffer((float)lons[0      ],(float)lats[j],(float)levs[k+1],xyzbuf);
			}
		}
		
		n.replaceUndefData(0);
		s.replaceUndefData(0);
		e.replaceUndefData(0);
		w.replaceUndefData(0);
		
		CtlDataWriteStream cdws=null;
		cdws=new CtlDataWriteStream(opath+"N.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(n); cdws.closeFile();
		cdws=new CtlDataWriteStream(opath+"S.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(s); cdws.closeFile();
		cdws=new CtlDataWriteStream(opath+"E.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(e); cdws.closeFile();
		cdws=new CtlDataWriteStream(opath+"W.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(w); cdws.closeFile();
		
		String levels=""; for(int k=1;k<=zgrid;k++) levels+=" "+levs[k];
		
		////// write ctl //////
		StringBuilder sb=null;
		
		sb=new StringBuilder();
		sb.append("dset ^"+IOUtil.getFileName(opath)+"N.dat\n");
		sb.append("title prepared for MITgcm\n");
		sb.append("options big_endian\n");
		sb.append("undef 0\n");
		sb.append(String.format("xdef %3d linear %6.2f %15.13f\n",xgrid,lons[0],resolution));
		sb.append(String.format("ydef   1 linear %6.2f %15.13f\n",lats[lats.length-1],resolution));
		sb.append("zdef  "+zgrid+" levels "+levels+"\n");
		sb.append("tdef  12 linear 01Jan2000 1mo\n");
		sb.append("vars 1\n");
		sb.append(vname+" "+zgrid+" 99 "+vname+"\n");
		sb.append("endvars\n");
		try(FileWriter fw=new FileWriter(opath+"N.ctl")){ fw.write(sb.toString());}
		catch(IOException err){ err.printStackTrace(); System.exit(0);}
		
		sb=new StringBuilder();
		sb.append("dset ^"+IOUtil.getFileName(opath)+"S.dat\n");
		sb.append("title prepared for MITgcm\n");
		sb.append("options big_endian\n");
		sb.append("undef 0\n");
		sb.append(String.format("xdef %3d linear %6.2f %15.13f\n",xgrid,lons[0],resolution));
		sb.append(String.format("ydef   1 linear %6.2f %15.13f\n",lats[0],resolution));
		sb.append("zdef  "+zgrid+" levels "+levels+"\n");
		sb.append("tdef  12 linear 01Jan2000 1mo\n");
		sb.append("vars 1\n");
		sb.append(vname+" "+zgrid+" 99 "+vname+"\n");
		sb.append("endvars\n");
		try(FileWriter fw=new FileWriter(opath+"S.ctl")){ fw.write(sb.toString());}
		catch(IOException err){ err.printStackTrace(); System.exit(0);}
		
		sb=new StringBuilder();
		sb.append("dset ^"+IOUtil.getFileName(opath)+"E.dat\n");
		sb.append("title prepared for MITgcm\n");
		sb.append("options big_endian\n");
		sb.append("undef 0\n");
		sb.append(String.format("xdef   1 linear %6.2f %15.13f\n",lons[lons.length-1],resolution));
		sb.append(String.format("ydef %3d linear %6.2f %15.13f\n",ygrid,lats[0],resolution));
		sb.append("zdef  "+zgrid+" levels "+levels+"\n");
		sb.append("tdef  12 linear 01Jan2000 1mo\n");
		sb.append("vars 1\n");
		sb.append(vname+" "+zgrid+" 99 "+vname+"\n");
		sb.append("endvars\n");
		try(FileWriter fw=new FileWriter(opath+"E.ctl")){ fw.write(sb.toString());}
		catch(IOException err){ err.printStackTrace(); System.exit(0);}
		
		sb=new StringBuilder();
		sb.append("dset ^"+IOUtil.getFileName(opath)+"W.dat\n");
		sb.append("title prepared for MITgcm\n");
		sb.append("options big_endian\n");
		sb.append("undef 0\n");
		sb.append(String.format("xdef   1 linear %6.2f %15.13f\n",lons[0],resolution));
		sb.append(String.format("ydef %3d linear %6.2f %15.13f\n",ygrid,lats[0],resolution));
		sb.append("zdef  "+zgrid+" levels "+levels+"\n");
		sb.append("tdef  12 linear 01Jan2000 1mo\n");
		sb.append("vars 1\n");
		sb.append(vname+" "+zgrid+" 99 "+vname+"\n");
		sb.append("endvars\n");
		try(FileWriter fw=new FileWriter(opath+"W.ctl")){ fw.write(sb.toString());}
		catch(IOException err){ err.printStackTrace(); System.exit(0);}
	}
	
	static void extractIC(String ipath,String opath,String vname,boolean surf){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		
		Variable v=null;
		
		if(surf){
			v=new Variable("v",true,new Range(1,1,ygrid,xgrid)); v.setUndef(dd.getUndef(v.getName()));
			
			float[][] vdata=v.getData()[0][0];
			float[][] xybuf=gdf.prepareXYBuffer(vname,1,1,500);
			
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++) vdata[j][i]=gdf.fetchXYBuffer((float)lons[i],(float)lats[j],xybuf);
			
		}else{
			v=new Variable("v",true,new Range(1,zgrid,ygrid,xgrid)); v.setUndef(dd.getUndef(v.getName()));
			
			float[][][] xyzbuf=gdf.prepareXYZBuffer(vname,1,1,dd.getZCount(),500);
			float[][][] vdata=v.getData()[0];
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++) vdata[k][j][i]=gdf.fetchXYZBuffer((float)lons[i],(float)lats[j],(float)levs[k+1],xyzbuf);
		}
		
		v.replaceUndefData(0);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath,ByteOrder.BIG_ENDIAN);
		cdws.writeData(v); cdws.closeFile();
		
		String levels=""; for(int k=1;k<=zgrid;k++) levels+=" "+levs[k];
		
		////// write ctl //////
		StringBuilder sb=new StringBuilder();
		
		sb.append("dset ^"+IOUtil.getFileName(opath)+"\n");
		sb.append("title prepared for MITgcm\n");
		sb.append("options big_endian\n");
		sb.append("undef 0\n");
		sb.append(String.format("xdef %3d linear %6.2f %15.13f\n",xgrid,lons[0],resolution));
		sb.append(String.format("ydef %3d linear %6.2f %15.13f\n",ygrid,lats[0],resolution));
		sb.append("zdef  "+zgrid+" levels "+levels+"\n");
		sb.append("tdef   1 linear 01Jan2000 1mo\n");
		sb.append("vars 1\n");
		sb.append(vname+" "+zgrid+" 99 "+vname+"\n");
		sb.append("endvars\n");
		
		try(FileWriter fw=new FileWriter(IOUtil.getCompleteFileNameWithoutExtension(opath)+".ctl")){ fw.write(sb.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	static void extractEXF(String ipath,String opath,String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		
		Variable v=new Variable("v",false,new Range(12,1,ygrid,xgrid));
		v.setUndef(dd.getUndef(v.getName()));
		
		float[][][] vdata=v.getData()[0];
		
		for(int l=0;l<12;l++){
			float[][] xybuf=gdf.prepareXYBuffer(vname,l+1,1,10);
			
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++) vdata[j][i][l]=gdf.fetchXYBuffer((float)lons[i],(float)lats[j],xybuf);
		}
		
		v.replaceUndefData(0);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath,ByteOrder.BIG_ENDIAN);
		cdws.writeData(v); cdws.closeFile();
		
		////// write ctl //////
		StringBuilder sb=new StringBuilder();
		
		sb.append("dset ^"+IOUtil.getFileName(opath)+"\n");
		sb.append("title prepared for MITgcm\n");
		sb.append("options big_endian\n");
		sb.append("undef 0\n");
		sb.append(String.format("xdef %3d linear %6.2f %15.13f\n",xgrid,lons[0],resolution));
		sb.append(String.format("ydef %3d linear %6.2f %15.13f\n",ygrid,lats[0],resolution));
		sb.append("zdef   1 levels "+levs[0]+"\n");
		sb.append("tdef  12 linear 01Jan2000 1mo\n");
		sb.append("vars 1\n");
		sb.append(vname+" 0 99 "+vname+"\n");
		sb.append("endvars\n");
		
		try(FileWriter fw=new FileWriter(IOUtil.getCompleteFileNameWithoutExtension(opath)+".ctl")){ fw.write(sb.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	static void extractBath(String ipath,String opath,String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		float[][] xybuf=gdf.prepareXYBuffer(vname,1,1);
		
		Variable v=new Variable("v",new Range(1,1,ygrid,xgrid));
		v.setUndef(dd.getUndef(v.getName()));
		
		float[][] vdata=v.getData()[0][0];
		
		for(int j=0;j<ygrid;j++)
		for(int i=0;i<xgrid;i++){
			float tmp=gdf.fetchXYBuffer((float)lons[i],(float)lats[j],xybuf);
			vdata[j][i]=tmp>=0?0:tmp;
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath,ByteOrder.BIG_ENDIAN);
		cdws.writeData(v); cdws.closeFile();
		
		////// write ctl //////
		StringBuilder sb=new StringBuilder();
		
		sb.append("dset ^"+IOUtil.getFilePrefixName(opath)+".dat\n");
		sb.append("title prepared for MITgcm\n");
		sb.append("options big_endian\n");
		sb.append("undef 0\n");
		sb.append(String.format("xdef %3d linear %6.2f %15.13f\n",xgrid,lons[0],resolution));
		sb.append(String.format("ydef %3d linear %6.2f %15.13f\n",ygrid,lats[0],resolution));
		sb.append("zdef   1 levels "+levs[0]+"\n");
		sb.append("tdef   1 linear 01Jan2000 1mo\n");
		sb.append("vars 1\n");
		sb.append("z 0 99 bathymetry (m)\n");
		sb.append("endvars\n");
		
		try(FileWriter fw=new FileWriter(IOUtil.getCompleteFileNameWithoutExtension(opath)+".ctl")){ fw.write(sb.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
}
