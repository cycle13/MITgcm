//
package SCS12;

import common.MITgcmUtil;
import common.MITgcmUtil.DataPrec;
import static SCS12.ConstructGrids.*;


/**
 * Partial cell grid of MITgcm obtained by reading all grid files
 * 
 * @author miniufo
 */
public final class ModelPartialGrids{
	//
	public float[]       drF=null;
	public float[][]     dyG=null;
	public float[][]     dxG=null;
	public float[][]     RAC=null;
	public float[][] maskInC=null;
	public float[][] maskInW=null;
	public float[][] maskInS=null;
	public float[][][] maskC=null;
	public float[][][] maskW=null;
	public float[][][] maskS=null;
	public float[][][] hFacW=null;
	public float[][][] hFacS=null;
	
	
	/**
	 * constructor
	 * 
	 * @param path	folder name that contains the output MITgcm grid file
	 * @param prec	data precision (32 or 64)
	 */
	public ModelPartialGrids(String path,DataPrec prec){
		drF  =MITgcmUtil.readFloatBE(path+"DRF.data"    ,prec);
		
		dyG    =MITgcmUtil.readFloatBE(path+"DYG.data"    ,xgrid,ygrid,prec);
		dxG    =MITgcmUtil.readFloatBE(path+"DXG.data"    ,xgrid,ygrid,prec);
		RAC    =MITgcmUtil.readFloatBE(path+"RAC.data"    ,xgrid,ygrid,prec);
		maskInC=MITgcmUtil.readFloatBE(path+"maskInC.data",xgrid,ygrid,prec);
		maskInS=MITgcmUtil.readFloatBE(path+"maskInS.data",xgrid,ygrid,prec);
		maskInW=MITgcmUtil.readFloatBE(path+"maskInW.data",xgrid,ygrid,prec);
		
		maskC=MITgcmUtil.readFloatBE(path+"maskC.data",xgrid,ygrid,zgrid,prec);
		maskS=MITgcmUtil.readFloatBE(path+"maskS.data",xgrid,ygrid,zgrid,prec);
		maskW=MITgcmUtil.readFloatBE(path+"maskW.data",xgrid,ygrid,zgrid,prec);
		hFacW=MITgcmUtil.readFloatBE(path+"hFacW.data",xgrid,ygrid,zgrid,prec);
		hFacS=MITgcmUtil.readFloatBE(path+"hFacS.data",xgrid,ygrid,zgrid,prec);
	}
	
	public ModelPartialGrids(String path){ this(path,DataPrec.float32);}
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		return String.format(
			"Grid info:\ndrF:%4d,  dxG:(%4d,%4d),  dyG:(%4d,%4d)\nhFacW:(%4d,%4d,%4d),  hFacS(%4d,%4d,%4d)",
			drF.length,dxG.length,dxG[0].length,dyG.length,dyG[0].length,
			hFacW.length,hFacW[0].length,hFacW[0][0].length,
			hFacS.length,hFacS[0].length,hFacS[0][0].length
		);
	}
}
