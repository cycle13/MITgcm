package Andaman;

import java.nio.ByteOrder;
import java.util.Arrays;

import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.geophysics.ocean.SeaWater;
import miniufo.geophysics.ocean.SeaWater2;
import miniufo.io.CtlDataWriteStream;
import miniufo.util.GridDataFetcher;


// extract bathymetry data from ETOPO 1
public final class Andaman24th{
	//
	private static final int xgrid=384;		//  96 * 4
	private static final int ygrid=456;		// 114 * 4
	private static final int zgrid=50;		//
	
	private static final float resolution=1f/24f;
	
	private static final float lonstr=85f;
	private static final float latstr=4f;
	private static final float lonend=lonstr+resolution*(xgrid-1);
	private static final float latend=latstr+resolution*(ygrid-1);
	
	private static final float[] lons=new float[xgrid];
	private static final float[] lats=new float[ygrid];
	private static final float[] levs=new float[]{   0f,
		   5.01f,  10f,  15f,  20f,  25f,  30f,  40f,  50f,  60f,  70f,
		  80f,  90f, 100f, 125f, 150f, 175f, 200f, 250f, 300f, 350f,
		 400f, 450f, 500f, 550f, 600f, 650f, 700f, 750f, 800f, 850f,
		 900f,1000f,1100f,1200f,1300f,1400f,1500f,1750f,2000f,2250f,
		2500f,2750f,3000f,3250f,3500f,3750f,4000f,4500f,5000f,5500f
	};
	
	private static final String path="d:/Data/MITgcm/Andaman/24th/";
	
	static{
		float[] dlevs=new float[zgrid];
		
		for(int k=0;k<zgrid;k++) dlevs[k]=levs[k+1]-levs[k];
		
		System.out.println(String.format(
			"domain info:\n"+
			"x-grids [%6.2f, %6.2f], total grids %4d =  96 * 4\n"+
			"y-grids [%6.2f, %6.2f], total grids %4d = 114 * 4\n"+
			"z-grids %s, total grids %2d\n"+
			"dz %s, total grids %2d\n",
			lonstr,lonend,xgrid,latstr,latend,ygrid,Arrays.toString(levs),zgrid+1,Arrays.toString(dlevs),zgrid
		));
		
		for(int i=0;i<xgrid;i++) lons[i]=lonstr+i*resolution;
		for(int j=0;j<ygrid;j++) lats[j]=latstr+j*resolution;
	}
	
	
	//
	public static void main(String[] args){
		/*** for bath ***/
		//extractBath("d:/Data/Bathymetry/GEBCO/gebco_1min_75_-25_165_45.ctl",path+"BATH/Bath_GEBCO.bin","z");
		
		/*** for IC ***/
		//extractIC("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAtempClim.ctl",path+"IC/ICtempSODA224.dat","temp",84,9,false);
		//extractIC("d:/Data/SODA/2.2.4/Clim_1981_2008/SODAsaltClim.ctl",path+"IC/ICsaltSODA224.dat","salt",84,9,false);
		
		//computeDensity(path+"IC/ICtempSODA224.ctl",path+"IC/ICsaltSODA224.ctl",path+"IC/ICdensSODA224.dat");
		
		/*** for OBCS using Matlab script ***/
		extractOBCS(path+"IC/ICtempSODA224.ctl",path+"OBCS/OBCStempSODA224","t");
		extractOBCS(path+"IC/ICsaltSODA224.ctl",path+"OBCS/OBCSsaltSODA224","s");
	}
	
	static void computeDensity(String tfile,String sfile,String dfile){
		DiagnosisFactory df=DiagnosisFactory.parseFile(tfile);
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable t=df.getVariables(new Range("",dd),"t")[0];
		Variable s=DiagnosisFactory.getVariables(sfile,"","s")[0];
		
		Variable[] dens=new Variable[4];
		
		dens[0]=new Variable("den1",t); dens[0].setComment("density from seawater 1");
		dens[1]=new Variable("sgm1",t); dens[0].setComment("potential density from seawater 1");
		dens[2]=new Variable("den2",t); dens[0].setComment("density from seawater 2");
		dens[3]=new Variable("sgm2",t); dens[0].setComment("potential density from seawater 2");
		
		float[][][][] tdata=t.getData();
		float[][][][] sdata=s.getData();
		float[][][][] data0=dens[0].getData();
		float[][][][] data1=dens[1].getData();
		float[][][][] data2=dens[2].getData();
		float[][][][] data3=dens[3].getData();
		
		for(int l=0;l<t.getTCount();l++)
		for(int k=0;k<t.getZCount();k++)
		for(int j=0;j<t.getYCount();j++)
		for(int i=0;i<t.getXCount();i++){
			data0[l][k][j][i]=(float)SeaWater.density(sdata[l][k][j][i],tdata[l][k][j][i],0);
			data1[l][k][j][i]=(float)SeaWater.densitySigmaT(sdata[l][k][j][i],tdata[l][k][j][i],0);
			data2[l][k][j][i]=(float)SeaWater2.density(sdata[l][k][j][i],tdata[l][k][j][i],0);
			data3[l][k][j][i]=(float)SeaWater2.sigmat(sdata[l][k][j][i],tdata[l][k][j][i]);
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(dfile,ByteOrder.BIG_ENDIAN);
		cdws.writeData(dens); cdws.closeFile();
	}
	
	static void extractOBCS(String ipath,String opath,String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable var=df.getVariables(new Range("x(1,1);y(1,1)",dd),vname)[0];
		
		Variable n=new Variable("n",true,new Range(12,zgrid,1,xgrid));
		Variable s=new Variable("s",true,new Range(12,zgrid,1,xgrid));
		Variable e=new Variable("e",true,new Range(12,zgrid,ygrid,1));
		Variable w=new Variable("w",true,new Range(12,zgrid,ygrid,1));
		
		for(int l=0;l<12;l++){
			float[][][] vdata=var.getData()[0];
			float[][][] ndata=  n.getData()[l];
			float[][][] sdata=  s.getData()[l];
			float[][][] edata=  e.getData()[l];
			float[][][] wdata=  w.getData()[l];
			
			for(int k=0;k<zgrid;k++)
			for(int i=0;i<xgrid;i++){
				ndata[k][0][i]=vdata[k][0][0];
				sdata[k][0][i]=vdata[k][0][0];
			}
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++){
				edata[k][j][0]=vdata[k][0][0];
				wdata[k][j][0]=vdata[k][0][0];
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
	
	static void extractIC(String ipath,String opath,String vname,float lon,float lat,boolean surf){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath);
		DataDescriptor dd=df.getDataDescriptor();
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		
		Variable v=null;
		
		if(surf){
			v=new Variable("v",true,new Range(1,1,ygrid,xgrid));
			
			float[][] vdata=v.getData()[0][0];
			Variable xybuf=gdf.prepareXYBuffer(vname,1,1,5);
			
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++) vdata[j][i]=gdf.fetchXYBuffer(lon,lat,xybuf);
			
		}else{
			v=new Variable("v",true,new Range(1,zgrid,ygrid,xgrid));
			
			Variable xyzbuf=gdf.prepareXYZBuffer(vname,1,1,dd.getZCount(),5);
			float[][][] vdata=v.getData()[0];
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++){
				if(k==zgrid-1||k==zgrid-2) vdata[k][j][i]=2f*vdata[k-1][j][i]-vdata[k-2][j][i];
				else vdata[k][j][i]=gdf.fetchXYZBuffer(lon,lat,levs[k+1],xyzbuf);
			}
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
