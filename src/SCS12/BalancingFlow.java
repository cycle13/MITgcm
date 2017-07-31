//
package SCS12;

import java.nio.ByteOrder;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import common.MITgcmApp;
import static SCS12.ConstructGrids.*;


// extract bathymetry data from ETOPO 1
public final class BalancingFlow extends MITgcmApp{
	
	/**
	 * constructor
	 * 
	 * @param grid	model grid
	 */
	public BalancingFlow(ModelPartialGrids grid){ super(grid);}
	
	
	/**
	 * compute mass transport across each open boundaries
	 */
	public Variable cTransE(Variable uE){
		checkYZDimension(uE);
		
		int t=uE.getTCount();
		
		Variable transE=new Variable("transE",true,new Range(t,1,1,1));
		
		float[][][][] udata=    uE.getData();
		float[][][][] tdata=transE.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++)
			sum+=udata[l][k][j][0]*dyG[j][xgrid-1]*maskInW[j][xgrid-1]*drF[k]*hFacW[k][j][xgrid-1];
			
			tdata[l][0][0][0]=(float)(sum/1e6); // convert to Sverdrup
		}
		
		return transE;
	}
	
	public Variable cTransW(Variable uW){
		checkYZDimension(uW);
		
		int t=uW.getTCount();
		
		Variable transW=new Variable("transW",true,new Range(t,1,1,1));
		
		float[][][][] udata=    uW.getData();
		float[][][][] tdata=transW.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++)
			sum+=udata[l][k][j][0]*dyG[j][1]*drF[k]*hFacW[k][j][1]*maskInW[j][1];
			
			tdata[l][0][0][0]=(float)(sum/1e6);
		}
		
		return transW;
	}
	
	public Variable cTransS(Variable vS){
		checkXZDimension(vS);
		
		int t=vS.getTCount();
		
		Variable transS=new Variable("transS",true,new Range(t,1,1,1));
		
		float[][][][] vdata=    vS.getData();
		float[][][][] tdata=transS.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<zgrid;k++)
			for(int i=0;i<xgrid;i++)
			sum+=vdata[l][k][0][i]*dxG[1][i]*drF[k]*hFacS[k][1][i]*maskInS[1][i];
			
			tdata[l][0][0][0]=(float)(sum/1e6);
		}
		
		return transS;
	}
	
	public Variable cTransN(Variable vN){
		checkXZDimension(vN);
		
		int t=vN.getTCount();
		
		Variable transN=new Variable("transN",true,new Range(t,1,1,1));
		
		float[][][][] vdata=    vN.getData();
		float[][][][] tdata=transN.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int k=0;k<zgrid;k++)
			for(int i=0;i<xgrid;i++)
			sum+=vdata[l][k][0][i]*dxG[ygrid-1][i]*drF[k]*hFacS[k][ygrid-1][i]*maskInS[ygrid-1][i];
			
			tdata[l][0][0][0]=(float)(sum/1e6);
		}
		
		return transN;
	}
	
	public Variable cTransF(Variable emp){
		checkXYDimension(emp);
		
		int t=emp.getTCount();
		
		Variable transF=new Variable("transF",true,new Range(t,1,1,1));
		
		float[][][][] edata=   emp.getData();
		float[][][][] tdata=transF.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++)
			sum+=edata[l][0][j][i]*RAC[j][i]*maskInC[j][i];
			
			tdata[l][0][0][0]=(float)(sum/1e6);
		}
		
		return transF;
	}
	
	public Variable cAreaMean(Variable var){
		checkXYDimension(var);
		
		int t=var.getTCount();
		
		Variable areaMean=new Variable(var.getName()+"M",true,new Range(t,1,1,1));
		areaMean.setComment("areal-mean of "+var.getComment());
		
		double area=cAreaF();
		float[][][][] vdata=     var.getData();
		float[][][][] tdata=areaMean.getData();
		
		for(int l=0;l<t;l++){
			double sum=0;
			
			for(int j=0;j<ygrid;j++)
			for(int i=0;i<xgrid;i++)
			sum+=vdata[l][0][j][i]*RAC[j][i]*maskInC[j][i];
			
			tdata[l][0][0][0]=(float)(sum/area);
		}
		
		return areaMean;
	}
	
	public Variable cNetLateralTransportByEta(Variable etaM){
		if(etaM.getXCount()!=1||etaM.getYCount()!=1) throw new IllegalArgumentException("areal-mean eta should be only one grid");
		
		int t=etaM.getTCount();
		
		Variable netTEta=new Variable("netTEta",true,new Range(t,1,1,1));
		netTEta.setComment("net transport by Eta (Sv)");
		
		float[][][][] edata=   etaM.getData();
		float[][][][] tdata=netTEta.getData();
		
		//tdata[0][0][0][0]=(float)((edata[1][0][0][0]-edata[t-1][0][0][0])/(30.0*86400.0*2.0)*cAreaF()/1e6);
		
		//for(int l=1;l<t-1;l++)
		//tdata[l][0][0][0]=(float)((edata[l+1][0][0][0]-edata[l-1][0][0][0])/(30.0*86400.0*2.0)*cAreaF()/1e6);
		
		//tdata[t-1][0][0][0]=(float)((edata[0][0][0][0]-edata[t-2][0][0][0])/(30.0*86400.0*2.0)*cAreaF()/1e6);
		
		for(int l=0;l<t-1;l++)
		tdata[ l ][0][0][0]=(float)(-(edata[l+1][0][0][0]-edata[l][0][0][0])*cAreaF()/(30.0*86400.0)/1e6);
		
		tdata[t-1][0][0][0]=(float)(-(edata[0][0][0][0]-edata[t-1][0][0][0])*cAreaF()/(30.0*86400.0)/1e6);
		
		return netTEta;
	}
	
	public Variable cEtaByNetLateralTransport(Variable net){
		if(net.getXCount()!=1||net.getYCount()!=1) throw new IllegalArgumentException("net lateral transport should be only one grid");
		
		int t=net.getTCount();
		
		Variable eta=new Variable("eta",true,new Range(t,1,1,1));
		eta.setComment("eta by net lateral transport (m)");
		
		float[][][][] edata=eta.getData();
		float[][][][] tdata=net.getData();
		
		for(int l=1;l<t;l++)
		edata[l][0][0][0]=(float)(edata[l-1][0][0][0]-(tdata[l][0][0][0]+tdata[l-1][0][0][0])/2.0*(30.0*86400.0)*(1e6/cAreaF()));
		
		return eta;
	}
	
	
	/**
	 * Balancing u at each OB by subtracting correctU (side-effect on uE).
	 * netT > 0 means net outward transport (divergence )
	 * netT < 0 means net  inward transport (convergence)
	 */
	public Variable cNetLateralTransport(Variable uE,Variable vS,Variable uW,Variable vN){
		checkXZDimension(vS,vN);
		checkYZDimension(uE,uW);
		
		Variable transE=cTransE(uE);
		Variable transW=cTransW(uW);
		Variable transN=cTransN(vN);
		Variable transS=cTransS(vS);
		
		Variable netT=transE.plus(transN).minusEq(transW).minusEq(transS); // E-W + N-S
		
		return netT;
	}
	
	/**
	 * compute corrected velocity
	 * 
	 * @param	netT	net transport, >0 outward (divergence), <0 inward (convergence) (m^3/s)
	 * @param	areaOB	areas of all the open boundaries that need to be corrected (m^2)
	 */
	public double cCorrectedVelocity(double netT,double areaOB){ return netT*(1e6/areaOB);}
	
	/**
	 * balancing u at each OB by subtracting correctU (side-effect on uE)
	 */
	public void balanceE(Variable uE,double correctU){
		checkYZDimension(uE);
		
		int t=uE.getTCount();
		
		float undef=uE.getUndef();
		
		float[][][][] udata=uE.getData();
		
		for(int l=0;l<t;l++)
		for(int k=0;k<zgrid;k++)
		for(int j=0;j<ygrid;j++) if(udata[l][k][j][0]!=undef) udata[l][k][j][0]-=correctU*maskW[k][j][xgrid-1];
	}
	
	
	/**
	 * balancing normal flow at each OB (side-effect on each normal flow)
	 */
	public void balancingEYear(Variable uE,Variable vS,Variable uW,Variable vN){
		Variable netT=cNetLateralTransport(uE,vS,uW,vN).anomalizeT();
		
		double areaOB=cAreaE();
		double correct=netT.getData()[0][0][0][0]*1e6/areaOB;
		
		System.out.println("\narea for OBCS is "+areaOB);
		System.out.println("net outward transport is "+netT.getData()[0][0][0][0]+" Sv");
		System.out.println("velocity correction is "+correct+" m/s");
		
		balanceE(uE,correct);
	}
	
	public void balancingToCurve(Variable uE,Variable vS,Variable uW,Variable vN,Variable curve){
		checkXZDimension(vS,vN);
		checkYZDimension(uE,uW);
		
		float undef=uE.getUndef();
		
		Variable netT=cNetLateralTransport(uE,vS,uW,vN);
		Variable netOut=netT.minus(curve); // (E-W + N-S) - curve
		
		double totalArea=cAreaE()+cAreaW()+cAreaN()+cAreaS();
		
		float[][][][] uEdata=uE.getData();
		float[][][][] uWdata=uW.getData();
		float[][][][] vNdata=vN.getData();
		float[][][][] vSdata=vS.getData();
		float[][][][] ntData=netOut.getData();
		
		for(int l=0,L=uE.getTCount();l<L;l++){
			float netTrans=ntData[l][0][0][0];
			
			float velECorr=(float)(-netTrans*(1e6/totalArea));
			float velWCorr=(float)( netTrans*(1e6/totalArea));
			float velNCorr=(float)(-netTrans*(1e6/totalArea));
			float velSCorr=(float)( netTrans*(1e6/totalArea));
			
			System.out.println(String.format(
				"t=%2d, net outward transport is %9.5f Sv, correction velocities (cm/s) are %9.6f(E) %9.6f(W) %9.6f(N) %9.6f(S)",
				l,netTrans,velECorr*1e2,velWCorr*1e2,velNCorr*1e2,velSCorr*1e2
			));
			
			for(int k=0;k<zgrid;k++)
			for(int j=0;j<ygrid;j++){
				if(uEdata[l][k][j][0]!=undef) uEdata[l][k][j][0]+=velECorr*maskW[k][j][xgrid-1];
				if(uWdata[l][k][j][0]!=undef) uWdata[l][k][j][0]+=velWCorr*maskW[k][j][1];
			}
			
			for(int k=0;k<zgrid;k++)
			for(int i=0;i<xgrid;i++){
				if(vNdata[l][k][0][i]!=undef) vNdata[l][k][0][i]+=velNCorr*maskS[k][ygrid-1][i];
				if(vSdata[l][k][0][i]!=undef) vSdata[l][k][0][i]+=velSCorr*maskS[k][1][i];
			}
		}
	}
	
	
	//
	public static void main(String[] args){
		cOBCSFlow();
	}
	
	public static void cOBCSFlow(){
		String tag="SODA226";
		
		ModelPartialGrids grid=new ModelPartialGrids(path+"runCCMP_"+tag+"_ETOPO1/ModelGrids/");
		
		BalancingFlow bf=new BalancingFlow(grid);
		
		Variable uW =DiagnosisFactory.getVariables(path+"OBCS/OBCSu"+tag+"W.ctl","",true,"u"  )[0];
		Variable uE =DiagnosisFactory.getVariables(path+"OBCS/OBCSu"+tag+"E.ctl","",true,"u"  )[0];
		Variable vS =DiagnosisFactory.getVariables(path+"OBCS/OBCSv"+tag+"S.ctl","",true,"v"  )[0];
		Variable vN =DiagnosisFactory.getVariables(path+"OBCS/OBCSv"+tag+"N.ctl","",true,"v"  )[0];
		Variable eta=DiagnosisFactory.getVariables(path+"EXF/EXFssh"+tag+".ctl" ,"",true,"ssh")[0];
		
		Variable etaM =bf.cAreaMean(eta);
		Variable netTE=bf.cNetLateralTransportByEta(etaM);
		Variable etaR =bf.cEtaByNetLateralTransport(netTE); etaR.setName("etar");
		
		bf.balancingToCurve(uE,vS,uW,vN,netTE);
		bf.balancingToCurve(uE,vS,uW,vN,netTE);
		bf.balancingToCurve(uE,vS,uW,vN,netTE);
		
		Variable netTC=bf.cNetLateralTransport(uE,vS,uW,vN); netTC.setName("netTC");
		Variable etaC =bf.cEtaByNetLateralTransport(netTC );  etaC.setName("etac" );
		System.out.println("net outward transport after correction is "+netTC.copy().anomalizeT().getData()[0][0][0][0]);
		
		CtlDataWriteStream cdws=null;
		cdws=new CtlDataWriteStream(path+"OBCS/OBCSu"+tag+"E_ETOPO1_correct.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(uE); cdws.closeFile();
		cdws=new CtlDataWriteStream(path+"OBCS/OBCSu"+tag+"W_ETOPO1_correct.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(uW); cdws.closeFile();
		cdws=new CtlDataWriteStream(path+"OBCS/OBCSv"+tag+"N_ETOPO1_correct.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(vN); cdws.closeFile();
		cdws=new CtlDataWriteStream(path+"OBCS/OBCSv"+tag+"S_ETOPO1_correct.dat",ByteOrder.BIG_ENDIAN); cdws.writeData(vS); cdws.closeFile();
		
		cdws=new CtlDataWriteStream(path+"trans.dat");
		cdws.writeData(netTC,netTE,etaM,etaR,etaC);
		cdws.closeFile();
	}
}
