package SCS;

import java.nio.ByteOrder;
import java.util.Arrays;

import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import common.MITgcmUtil;
import common.MITgcmUtil.DataPrec;
import static SCS12.ConstructGrids.*;


// extract bathymetry data from ETOPO 1
public final class BalancingFlow2{
	//
	static Variable cTransE(Variable uE,Grid grid){
		int t=uE.getTCount(),z=uE.getZCount(),y=uE.getYCount();
		
		Variable transE=new Variable("transE",true,new Range(t,1,1,1));
		
		float[]      drF =grid.drF;
		float[][]    dyG =grid.dyG;
		float[][] maskInW=grid.maskInW;
		float[][][] hFacW=grid.hFacW;
		
		float[][][][] udata=    uE.getData();
		float[][][][] tdata=transE.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<z;k++)
			for(int j=0;j<y;j++){
				sum+=udata[l][k][j][0]*dyG[j][xgrid-1]*maskInW[j][xgrid-1]*drF[k]*hFacW[k][j][xgrid-1];
				//if(l==0) System.out.println(String.format("E %4d %4d %E %E %E %E %E",k+1,j+1,maskInW[j][xgrid-1],dyG[j][xgrid-1],drF[k],hFacW[k][j][xgrid-1],udata[l][k][j][0]));
			}
			
			tdata[l][0][0][0]=(float)(sum/1e6); // convert to Sverdrup
			
			if(l==0) System.out.println("flowE: "+sum);
		}
		
		return transE;
	}
	
	static Variable cTransW(Variable uW,Grid grid){
		int t=uW.getTCount(),z=uW.getZCount(),y=uW.getYCount();
		
		Variable transW=new Variable("transW",true,new Range(t,1,1,1));
		
		float[]      drF =grid.drF;
		float[][]    dyG =grid.dyG;
		float[][] maskInW=grid.maskInW;
		float[][][] hFacW=grid.hFacW;
		
		float[][][][] udata=    uW.getData();
		float[][][][] tdata=transW.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<z;k++)
			for(int j=0;j<y;j++){
			sum+=udata[l][k][j][0]*dyG[j][1]*drF[k]*hFacW[k][j][1]*maskInW[j][1];
			//if(l==0) System.out.println(String.format("W %4d %4d %E %E %E %E %E",k+1,j+1,maskInW[j][1],dyG[j][1],drF[k],hFacW[k][j][1],udata[l][k][j][0]));
			}
			
			tdata[l][0][0][0]=(float)(sum/1e6);
			
			if(l==0) System.out.println("flowW: "+sum);
		}
		
		return transW;
	}
	
	static Variable cTransS(Variable vS,Grid grid){
		int t=vS.getTCount(),z=vS.getZCount(),x=vS.getXCount();
		
		Variable transS=new Variable("transS",true,new Range(t,1,1,1));
		
		float[]      drF =grid.drF;
		float[][]    dxG =grid.dxG;
		float[][] maskInS=grid.maskInS;
		float[][][] hFacS=grid.hFacS;
		
		float[][][][] vdata=    vS.getData();
		float[][][][] tdata=transS.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<z;k++)
			for(int i=0;i<x;i++){
			sum+=vdata[l][k][0][i]*dxG[1][i]*drF[k]*hFacS[k][1][i]*maskInS[1][i];
			//if(l==0) System.out.println(String.format("S %4d %4d %E %E %E %E %E",k+1,i+1,maskInS[1][i],dxG[1][i],drF[k],hFacS[k][1][i],vdata[l][k][0][i]));
			}
			
			tdata[l][0][0][0]=(float)(sum/1e6);
			
			if(l==0) System.out.println("flowS: "+sum);
		}
		
		return transS;
	}
	
	static Variable cTransN(Variable vN,Grid grid){
		int t=vN.getTCount(),z=vN.getZCount(),x=vN.getXCount();
		
		Variable transN=new Variable("transN",true,new Range(t,1,1,1));
		
		float[]      drF =grid.drF;
		float[][]    dxG =grid.dxG;
		float[][] maskInS=grid.maskInS;
		float[][][] hFacS=grid.hFacS;
		
		float[][][][] vdata=    vN.getData();
		float[][][][] tdata=transN.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<z;k++)
			for(int i=0;i<x;i++){
			sum+=vdata[l][k][0][i]*dxG[ygrid-1][i]*drF[k]*hFacS[k][ygrid-1][i]*maskInS[ygrid-1][i];
			//if(l==0) System.out.println(String.format("N %4d %4d %E %E %E %E %E",k+1,i+1,maskInS[ygrid-1][i],dxG[ygrid-1][i],drF[k],hFacS[k][ygrid-1][i],vdata[l][k][0][i]));
			}
			
			tdata[l][0][0][0]=(float)(sum/1e6);
			
			if(l==0) System.out.println("flowN: "+sum);
		}
		
		return transN;
	}
	
	static Variable cTransF(Variable emp,Grid grid){
		int t=emp.getTCount(),y=emp.getYCount(),x=emp.getXCount();
		
		Variable transF=new Variable("transF",true,new Range(t,1,1,1));
		
		float[][]    RAC =grid.RAC;
		float[][] maskInC=grid.maskInC;
		
		float[][][][] edata=   emp.getData();
		float[][][][] tdata=transF.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int j=0;j<y;j++)
			for(int i=0;i<x;i++)
			sum+=edata[l][0][j][i]*RAC[j][i]*maskInC[j][i];
			
			tdata[l][0][0][0]=(float)(sum/1e6);
		}
		
		return transF;
	}
	
	static void balanceE(Variable uE,Grid grid,double correct){
		int t=uE.getTCount(),z=uE.getZCount(),y=uE.getYCount();
		
		float undef=uE.getUndef();
		float[][][] maskW=grid.maskW;
		float[][][][] udata=uE.getData();
		
		for(int l=0;l<t;l++)
		for(int k=0;k<z;k++)
		for(int j=0;j<y;j++) if(udata[l][k][j][0]!=undef) udata[l][k][j][0]-=correct*maskW[k][j][xgrid-1];
	}
	
	static double cAreaE(Grid grid){
		float[]      drF =grid.drF;
		float[][]    dyG =grid.dyG;
		float[][] maskInW=grid.maskInW;
		float[][][] hFacW=grid.hFacW;
		
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int j=0;j<ygrid;j++)
		sum+=dyG[j][xgrid-1]*maskInW[j][xgrid-1]*drF[k]*hFacW[k][j][xgrid-1];
		
		System.out.println("areaE: "+sum);
		
		return sum;
	}
	
	static double cAreaW(Grid grid){
		float[]      drF =grid.drF;
		float[][]    dyG =grid.dyG;
		float[][] maskInW=grid.maskInW;
		float[][][] hFacW=grid.hFacW;
		
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int j=0;j<ygrid;j++)
		sum+=dyG[j][1]*drF[k]*hFacW[k][j][1]*maskInW[j][1];
		
		System.out.println("areaW: "+sum);
		
		return sum;
	}
	
	static double cAreaS(Grid grid){
		float[]      drF =grid.drF;
		float[][]    dxG =grid.dxG;
		float[][] maskInS=grid.maskInS;
		float[][][] hFacS=grid.hFacS;
		
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int i=0;i<xgrid;i++)
		sum+=dxG[1][i]*drF[k]*hFacS[k][1][i]*maskInS[1][i];
		
		System.out.println("areaS: "+sum);
		
		return sum;
	}
	
	static double cAreaN(Grid grid){
		float[]      drF =grid.drF;
		float[][]    dxG =grid.dxG;
		float[][] maskInS=grid.maskInS;
		float[][][] hFacS=grid.hFacS;
		
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int i=0;i<xgrid;i++)
		sum+=dxG[ygrid-1][i]*drF[k]*hFacS[k][ygrid-1][i]*maskInS[ygrid-1][i];
		
		System.out.println("areaN: "+sum);
		
		return sum;
	}
	
	static void balancing(Variable uE,Variable vS,Variable uW,Variable vN,Grid grid){
		Variable transE=cTransE(uE ,grid);	double aE=cAreaE(grid); System.out.println(transE.getData()[0][0][0][0]*1e6/aE);
		Variable transW=cTransW(uW ,grid);	double aW=cAreaW(grid); System.out.println(transW.getData()[0][0][0][0]*1e6/aW);
		Variable transN=cTransN(vN ,grid);	double aN=cAreaN(grid); System.out.println(transN.getData()[0][0][0][0]*1e6/aN);
		Variable transS=cTransS(vS ,grid);	double aS=cAreaS(grid); System.out.println(transS.getData()[0][0][0][0]*1e6/aS);
		
		Variable netT=transE.plus(transN).minusEq(transW).minusEq(transS).anomalizeT(); // E-W + N-S
		
		double areaOB=aE;//+aW+aN+aS;
		double correct=netT.getData()[0][0][0][0]*1e6/areaOB;
		
		System.out.println("\narea for OBCS is "+areaOB);
		System.out.println("net outward transport is "+netT.getData()[0][0][0][0]+" Sv");
		System.out.println("velocity correction is "+correct+" m/s");
		
		balanceE(uE,grid,correct);
	}
	
	static Variable cSSHVolumn(Variable ssh,Grid grid){
		int t=ssh.getTCount(),y=ssh.getYCount(),x=ssh.getXCount();
		
		Variable sshV=new Variable("sshV",true,new Range(t,1,1,1));
		
		float[][]    RAC =grid.RAC;
		float[][][] maskC=grid.maskC;
		
		float[][][][] edata= ssh.getData();
		float[][][][] tdata=sshV.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int j=0;j<y;j++)
			for(int i=0;i<x;i++)
			sum+=edata[l][0][j][i]*RAC[j][i]*maskC[0][j][i];
			
			tdata[l][0][0][0]=(float)(sum);
		}
		
		return sshV;
	}
	
	static Variable DADt(Variable v){
		int t=v.getTCount(),y=v.getYCount(),x=v.getXCount();
		
		Variable tend=new Variable("tend",true,new Range(t,1,1,1));
		
		float[][][][] vdata=   v.getData();
		float[][][][] tdata=tend.getData();
		
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++){
			tdata[0][0][0][0]=(vdata[1][0][j][i]-vdata[0][0][j][i])/3600f/30f;
			for(int l=1;l<t-1;l++) tdata[l][0][0][0]=(vdata[l+1][0][j][i]-vdata[l-1][0][j][i])/3600f/30f/2f;
			tdata[t-1][0][0][0]=(vdata[t-1][0][j][i]-vdata[t-2][0][j][i])/3600f/30f;
		}
		
		return tend;
	}
	
	
	/*** helper methods ***/
	static class Grid{
		float[]       drF=null;
		float[][]     dyG=null;
		float[][]     dxG=null;
		float[][]     RAC=null;
		float[][] maskInC=null;
		float[][] maskInW=null;
		float[][] maskInS=null;
		float[][][] maskC=null;
		float[][][] maskW=null;
		float[][][] maskS=null;
		float[][][] hFacW=null;
		float[][][] hFacS=null;
		
		public Grid(String path,DataPrec prec){
			drF  =MITgcmUtil.readFloatBE(path+"DRF.data"    ,prec);
			
			dyG  =MITgcmUtil.readFloatBE(path+"DYG.data"    ,xgrid,ygrid,prec);
			dxG  =MITgcmUtil.readFloatBE(path+"DXG.data"    ,xgrid,ygrid,prec);
			RAC  =MITgcmUtil.readFloatBE(path+"RAC.data"    ,xgrid,ygrid,prec);
			
			maskInC=MITgcmUtil.readFloatBE(path+"maskInC.data",xgrid,ygrid,prec);
			maskInS=MITgcmUtil.readFloatBE(path+"maskInS.data",xgrid,ygrid,prec);
			maskInW=MITgcmUtil.readFloatBE(path+"maskInW.data",xgrid,ygrid,prec);
			
			maskC=MITgcmUtil.readFloatBE(path+"maskC.data",xgrid,ygrid,zgrid,prec);
			maskS=MITgcmUtil.readFloatBE(path+"maskS.data",xgrid,ygrid,zgrid,prec);
			maskW=MITgcmUtil.readFloatBE(path+"maskW.data",xgrid,ygrid,zgrid,prec);
			
			hFacW=MITgcmUtil.readFloatBE(path+"hFacW.data"  ,xgrid,ygrid,zgrid,prec);
			hFacS=MITgcmUtil.readFloatBE(path+"hFacS.data"  ,xgrid,ygrid,zgrid,prec);
		}
		
		public String toString(){
			return String.format(
				"Grid info:\ndrF:%4d,  dxG:(%4d,%4d),  dyG:(%4d,%4d)\nhFacW:(%4d,%4d,%4d),  hFacS(%4d,%4d,%4d)",
				drF.length,dxG.length,dxG[0].length,dyG.length,dyG[0].length,
				hFacW.length,hFacW[0].length,hFacW[0][0].length,
				hFacS.length,hFacS[0].length,hFacS[0][0].length
			);
		}
	}
	
	
	//
	public static void main(String[] args){
		cOBCSFlow();System.exit(0);
		
		Grid grid=new Grid(path,DataPrec.float32); System.out.println(grid);
		
		DiagnosisFactory df=DiagnosisFactory.parseFile(path+"runETOPOSODA224/Stat.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable uW =df.getVariables(new Range("x(1,1)"                ,dd),true,"u"  )[0];
		Variable uE =df.getVariables(new Range("x("+xgrid+","+xgrid+")",dd),true,"u"  )[0];
		Variable vS =df.getVariables(new Range("y(1,1)"                ,dd),true,"v"  )[0];
		Variable vN =df.getVariables(new Range("y("+ygrid+","+ygrid+")",dd),true,"v"  )[0];
		Variable ssh=df.getVariables(new Range("z(1,1)"                ,dd),true,"eta")[0];
		
		//Variable emp=DiagnosisFactory.getVariables(path+"EXF/NCEP/EXFemp_NCEP.ctl","",true,"emp")[0];
		
		Variable transE=cTransE(uE ,grid);
		Variable transW=cTransW(uW ,grid);
		Variable transS=cTransS(vS ,grid);
		Variable transN=cTransN(vN ,grid);
		//Variable transF=cTransF(emp,grid);
		Variable sshVol=cSSHVolumn(ssh,grid);
		Variable dsdt=DADt(sshVol);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(path+"transSim.dat");
		cdws.writeData(transE,transW,transS,transN,sshVol,dsdt); cdws.closeFile();
	}
	
	public static void cOBCSFlow(){
		Grid grid=new Grid(path,DataPrec.float32); System.out.println(grid);
		
		Variable uW =DiagnosisFactory.getVariables(path+"OBCS/SODA224/OBCSuSODA224W.ctl","",true,"u"  )[0];
		Variable uE =DiagnosisFactory.getVariables(path+"OBCS/SODA224/OBCSuSODA224E.ctl","",true,"u"  )[0];
		Variable vS =DiagnosisFactory.getVariables(path+"OBCS/SODA224/OBCSvSODA224S.ctl","",true,"v"  )[0];
		Variable vN =DiagnosisFactory.getVariables(path+"OBCS/SODA224/OBCSvSODA224N.ctl","",true,"v"  )[0];
		//Variable ssh=DiagnosisFactory.getVariables(path+"EXF/SODA224/EXFsshSODA224.ctl" ,"",true,"ssh")[0];
		//Variable emp=DiagnosisFactory.getVariables(path+"EXF/NCEP/EXFemp_NCEP.ctl"      ,"",true,"emp")[0];
		
		balancing(uE,vS,uW,vN,grid);
		//balancing(uE,vS,uW,vN,grid);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(path+"OBCS/SODA224/OBCSuSODA224E_correct.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(uE); cdws.closeFile();
		
		Variable transE=cTransE(uE ,grid);
		Variable transW=cTransW(uW ,grid);
		Variable transS=cTransS(vS ,grid);
		Variable transN=cTransN(vN ,grid);
		//Variable transF=cTransF(emp,grid);
		//Variable sshVol=cSSHVolumn(ssh,grid);
		//Variable dsdt=DADt(sshVol);
		
		//CtlDataWriteStream cdws=new CtlDataWriteStream(path+"trans.dat");
		//cdws.writeData(transE,transW,transS,transN,transF,sshVol,dsdt); cdws.closeFile();
	}
}
