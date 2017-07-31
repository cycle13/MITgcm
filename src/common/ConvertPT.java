package common;

import miniufo.application.basic.ThermoDynamicMethodsInSC;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.SphericalSpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.diagnosis.SpatialModel.LevelType;
import miniufo.io.CtlDataWriteStream;


// extract bathymetry data from ETOPO 1
public final class ConvertPT{
	//
	public static void main(String[] args){
		DiagnosisFactory df=DiagnosisFactory.parseFile("D:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Range r=new Range("",dd);
		
		Variable eta=df.getVariables(new Range("z(1,1)",dd),true,"eta")[0];
		Variable[] vs=df.getVariables(r,true,"u","v","t","s");
		
		//replaceNaN(eta);
		//for(Variable v:vs) replaceNaN(v);
		
		Variable T=vs[2];
		Variable S=vs[3];
		
		SphericalSpatialModel ssm=new SphericalSpatialModel(dd,LevelType.GEOMETRIC);
		ThermoDynamicMethodsInSC tdm=new ThermoDynamicMethodsInSC(ssm);
		
		Variable PT=tdm.convertToTheta(S,T);
		Variable T2=tdm.convertToInSituT(S,PT); T2.setName("T2");
		
		CtlDataWriteStream cdws=new CtlDataWriteStream("D:/Data/HYCOM/HYCOM_SCS_Clim_1993_2003PT.dat");
		cdws.writeData(dd,eta,vs[0],vs[1],T,S); cdws.closeFile();
	}
	
	static void replaceNaN(Variable v){
		float[][][][] vdata=v.getData();
		
		if(v.isTFirst()){
			for(int l=0;l<v.getTCount();l++)
			for(int k=0;k<v.getZCount();k++)
			for(int j=0;j<v.getYCount();j++)
			for(int i=0;i<v.getXCount();i++) if(Float.isNaN(vdata[l][k][j][i])) vdata[l][k][j][i]=-9.99e8f;
			
		}else{
			for(int l=0;l<v.getTCount();l++)
			for(int k=0;k<v.getZCount();k++)
			for(int j=0;j<v.getYCount();j++)
			for(int i=0;i<v.getXCount();i++) if(Float.isNaN(vdata[k][j][i][l])) vdata[k][j][i][l]=-9.99e8f;
		}
	}
}
