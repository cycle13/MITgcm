package SCS;

import java.nio.ByteOrder;
import java.util.Arrays;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import miniufo.util.GridDataFetcher;


// extract bathymetry data from ETOPO 1
public final class SCS10All{
	//
	private static final int xgrid=416;		// 52 * 8
	private static final int ygrid=342;		// 57 * 2 * 3
	private static final int zgrid=30;		// 57 * 2 * 3
	
	private static final float resolution=0.1f;
	
	private static final float lonstr=99f;
	private static final float latstr=0.8f;
	private static final float lonend=lonstr+resolution*(xgrid-1);
	private static final float latend=latstr+resolution*(ygrid-1);
	
	private static final float[] lons=new float[xgrid];
	private static final float[] lats=new float[ygrid];
	private static final float[] levs=new float[]{   0f,
		   5.01f,  15f,  30f,  45f,  60f,  80f, 100f, 125f, 150f, 175f,
		 200f, 250f, 300f, 350f, 400f, 450f, 500f, 600f, 700f, 800f,
		1000f,1250f,1500f,2000f,2500f,3000f,3500f,4000f,4500f,5000f
	};
	
	private static final String path="d:/Data/MITgcm/SCS/Tenth/";
	
	static{
		float[] dlevs=new float[zgrid];
		
		for(int k=0;k<zgrid;k++) dlevs[k]=levs[k+1]-levs[k];
		
		System.out.println(String.format(
			"domain info:\n"+
			"x-grids [%6.2f, %6.2f], total grids %4d = 52 * 8\n"+
			"y-grids [%6.2f, %6.2f], total grids %4d = 57 * 2 * 3\n"+
			"z-grids %s, total grids %2d\n"+
			"dz %s, total grids %2d\n",
			lonstr,lonend,xgrid,latstr,latend,ygrid,Arrays.toString(levs),zgrid+1,Arrays.toString(dlevs),zgrid
		));
		
		for(int i=0;i<xgrid;i++) lons[i]=lonstr+i*resolution;
		for(int j=0;j<ygrid;j++) lats[j]=latstr+j*resolution;
	}
	
	
	//
	public static void main(String[] args){
		/*** for bath **
		extractBath("d:/Data/Bathymetry/ETOPO/ETOPO1_Bed.ctl",path+"BATH/Bath_ETOPO1.bin","z");
		extractBath("d:/Data/Bathymetry/GEBCO/gebco_08_75_-25_165_45.ctl",path+"BATH/Bath_GEBCO.bin","z");
		extractBath("d:/Data/Bathymetry/SRTM/SRTM.ctl",path+"BATH/Bath_SRTM.bin","z");*/
		
		/*** for IC ***/
		//prepareSODA224();
		//prepareSODA226();
		//prepareGODAS();
		prepareHYCOM();
		
		
		/*** for EXF ***/
		//extractEXF("d:/Data/NCEP/FreshWaterFlux/EmP.ctl",path+"EXF/NCEP/EXFemp_NCEP.dat","emp");
		//extractEXF("d:/Data/NCEP/QNet/hflux.ctl",path+"EXF/NCEP/EXFhflux_NCEP.dat","hflux");
		//extractEXF("d:/Data/NCEP/QNet/swflux.ctl",path+"EXF/NCEP/EXFswflux_NCEP.dat","swflux");
	}
	
	static void prepareHYCOM(){
		/*** for IC GODAS ***/
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICtHYCOM.dat","t",false);
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICsHYCOM.dat","s",false);
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICuHYCOM.dat","u",false);
		extractIC("d:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl",path+"IC/ICvHYCOM.dat","v",false);
		
		/*** for OBCS GODAS ***/
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
		/*** for IC SODA 2.2.4 **
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICtempSODA224.dat","temp",false);
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICsaltSODA224.dat","salt",false);
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICuSODA224.dat","u",false);
		extractIC("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"IC/ICvSODA224.dat","v",false);*/
		
		/*** for EXF SODA 2.2.4 **
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFtauxSODA224.dat","taux");
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFtauySODA224.dat","tauy");
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFsstSODA224.dat","temp");
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFsssSODA224.dat","salt");*/
		extractEXF("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"EXF/EXFsshSODA224.dat","ssh");
		
		/*** for OBCS SODA 2.2.4 **
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCStempSODA224","temp");
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCSsaltSODA224","salt");
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCSuSODA224"   ,"u"   );
		extractOBCS("d:/Data/SODA/2.2.4/SODA224Clim_1993_2003.ctl",path+"OBCS/OBCSvSODA224"   ,"v"   );*/
	}
	
	static void prepareSODA226(){
		/*** for IC SODA 2.2.6 **
		extractIC("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAtempClim.ctl",path+"IC/ICtempSODA226.dat","temp",false);
		extractIC("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAsaltClim.ctl",path+"IC/ICsaltSODA226.dat","salt",false);
		extractIC("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAuClim.ctl"   ,path+"IC/ICuSODA226.dat"   ,"u"   ,false);
		extractIC("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAvClim.ctl"   ,path+"IC/ICvSODA226.dat"   ,"v"   ,false);*/
		
		/*** for EXF SODA 2.2.6 **
		extractEXF("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAtauxClim.ctl",path+"EXF/EXFtauxSODA226.dat","taux");
		extractEXF("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAtauyClim.ctl",path+"EXF/EXFtauySODA226.dat","tauy");
		extractEXF("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAtempClim.ctl",path+"EXF/EXFsstSODA226.dat","temp");
		extractEXF("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAsaltClim.ctl",path+"EXF/EXFsssSODA226.dat","salt");*/
		extractEXF("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAsshClim.ctl",path+"EXF/EXFsshSODA226.dat","ssh");
		
		/*** for OBCS SODA 2.2.6 **
		extractOBCS("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAtempClim.ctl",path+"OBCS/OBCStempSODA226","temp");
		extractOBCS("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAsaltClim.ctl",path+"OBCS/OBCSsaltSODA226","salt");
		extractOBCS("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAuClim.ctl"   ,path+"OBCS/OBCSuSODA226"   ,"u"   );
		extractOBCS("d:/Data/SODA/2.2.6/Clim_1981_2008/SODAvClim.ctl"   ,path+"OBCS/OBCSvSODA226"   ,"v"   );*/
	}
	
	static void extractOBCS(String ipath,String opath,String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		
		Variable n=new Variable("n",true,new Range(12,zgrid,1,xgrid));
		Variable s=new Variable("s",true,new Range(12,zgrid,1,xgrid));
		Variable e=new Variable("e",true,new Range(12,zgrid,ygrid,1));
		Variable w=new Variable("w",true,new Range(12,zgrid,ygrid,1));
		
		for(int l=0;l<12;l++){
			Variable xyzbuf=gdf.prepareXYZBuffer(vname,l+1,1,dd.getZCount(),5);
			
			float[][][] ndata=n.getData()[l];
			float[][][] sdata=s.getData()[l];
			float[][][] edata=e.getData()[l];
			float[][][] wdata=w.getData()[l];
			
			for(int k=0;k<zgrid;k++)
			for(int i=0;i<xgrid;i++){
				ndata[k][0][i]=gdf.fetchXYZBuffer(lons[i],lats[ygrid-1],levs[k+1],xyzbuf);
				sdata[k][0][i]=gdf.fetchXYZBuffer(lons[i],lats[0      ],levs[k+1],xyzbuf);
			}
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++){
				edata[k][j][0]=gdf.fetchXYZBuffer(lons[xgrid-1],lats[j],levs[k+1],xyzbuf);
				wdata[k][j][0]=gdf.fetchXYZBuffer(lons[0      ],lats[j],levs[k+1],xyzbuf);
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
	}
	
	static void extractIC(String ipath,String opath,String vname,boolean surf){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		
		Variable v=null;
		
		if(surf){
			v=new Variable("v",true,new Range(1,1,ygrid,xgrid));
			
			float[][] vdata=v.getData()[0][0];
			Variable xybuf=gdf.prepareXYBuffer(vname,1,1,5);
			
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++) vdata[j][i]=gdf.fetchXYBuffer(lons[i],lats[j],xybuf);
			
		}else{
			v=new Variable("v",true,new Range(1,zgrid,ygrid,xgrid));
			
			Variable xyzbuf=gdf.prepareXYZBuffer(vname,1,1,dd.getZCount(),5);
			float[][][] vdata=v.getData()[0];
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++) vdata[k][j][i]=gdf.fetchXYZBuffer(lons[i],lats[j],levs[k+1],xyzbuf);
		}
		
		v.replaceUndefData(0);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath,ByteOrder.BIG_ENDIAN);
		cdws.writeData(v); cdws.closeFile();
	}
	
	static void extractEXF(String ipath,String opath,String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		
		Variable v=new Variable("v",false,new Range(12,1,ygrid,xgrid));
		
		float[][][] vdata=v.getData()[0];
		
		for(int l=0;l<12;l++){
			Variable xybuf=gdf.prepareXYBuffer(vname,l+1,1,5);
			
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++) vdata[j][i][l]=gdf.fetchXYBuffer(lons[i],lats[j],xybuf);
		}
		
		v.replaceUndefData(0);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath,ByteOrder.BIG_ENDIAN);
		cdws.writeData(v); cdws.closeFile();
	}
	
	static void extractBath(String ipath,String opath,String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		Variable xybuf=gdf.prepareXYBuffer(vname,1,1);
		
		Variable v=new Variable("v",new Range(1,1,ygrid,xgrid));
		
		float[][] vdata=v.getData()[0][0];
		
		for(int j=0;j<ygrid;j++)
		for(int i=0;i<xgrid;i++){
			float tmp=gdf.fetchXYBuffer(lons[i],lats[j],xybuf);
			vdata[j][i]=tmp>=0?0:tmp;
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath,ByteOrder.BIG_ENDIAN);
		cdws.writeData(v); cdws.closeFile();
	}
}
