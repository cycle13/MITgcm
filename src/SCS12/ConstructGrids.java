package SCS12;

import java.util.Arrays;
import miniufo.mathsphysics.MathsPhysicsUtil;


// extract bathymetry data from ETOPO 1
public final class ConstructGrids{
	//
	public static final int xgrid=396;		//
	public static final int ygrid=328;		//
	public static final int zgrid=40;		//
	
	public static final double resolution=1.0/12.0;
	
	public static final double lonstr=99.0;
	public static final double latstr=0.8;
	public static final double lonend=lonstr+resolution*(xgrid-1);
	public static final double latend=latstr+resolution*(ygrid-1);
	
	public static final double[] lons=new double[xgrid];
	public static final double[] lats=new double[ygrid];
	public static final double[] levs=new double[]{   0,
		   5,  10,  15,  20,  25,  30,  35,  40,  50,  60,
		  70,  80,  90, 100, 110, 120, 130, 140, 150, 175,
		 200, 225, 250, 275, 300, 350, 400, 500, 600, 800,
		1000,1250,1500,2000,2500,3000,3500,4000,4500,5000
	};
	
	public static final String path="d:/Data/MITgcm/SCS/Twelfth/";
	
	static{
		double[] dlevs=new double[zgrid];
		
		for(int k=0;k<zgrid;k++) dlevs[k]=levs[k+1]-levs[k];
		
		System.out.println(String.format(
			"domain info:\n"+
			"x-grids [%9.5f, %9.5f], total grids %4d = %s\n"+
			"y-grids [%9.5f, %9.5f], total grids %4d = %s\n"+
			"z-grids %s, total grids %2d\n"+
			"dz %s, total grids %2d\n",
			lonstr,lonend,xgrid,Arrays.toString(MathsPhysicsUtil.factor(xgrid)),
			latstr,latend,ygrid,Arrays.toString(MathsPhysicsUtil.factor(ygrid)),
			Arrays.toString(levs),zgrid+1,Arrays.toString(dlevs),zgrid
		));
		
		for(int i=0;i<xgrid;i++) lons[i]=lonstr+i*resolution;
		for(int j=0;j<ygrid;j++) lats[j]=latstr+j*resolution;
	}
}
