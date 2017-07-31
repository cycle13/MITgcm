//
package fltDispersion;

import java.nio.ByteOrder;
import miniufo.application.basic.VelocityFieldInSC;
import miniufo.application.statisticsModel.FilterMethods;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.SphericalSpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import static java.lang.Math.PI;
import static java.lang.Math.cos;

//
public final class saddleField{
	//
	private static final String path="D:/Data/MITgcm/flt/";
	
	//
	public static void main(String[] args){
		//genTracerField(path+"tracer.dat");
		//genSaddleField(path+"saddle.dat"); System.exit(0);
		
		DiagnosisFactory df=DiagnosisFactory.parseFile(path+"saddle.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		SphericalSpatialModel ssm=new SphericalSpatialModel(dd);
		VelocityFieldInSC vf=new VelocityFieldInSC(ssm);
		
		Variable sf=df.getVariables(new Range("",dd),false,"sf")[0];
		
		Variable[] vel=vf.cRotationalVelocity(sf);
		
		float[][][] udata=vel[0].getData()[0];
		float[][][] vdata=vel[1].getData()[0];
		
		for(int l=0;l<sf.getTCount();l++)
		for(int j=0;j<sf.getYCount();j++)
		for(int i=0;i<sf.getXCount();i++){
			if(udata[j][i][l]==0) udata[j][i][l]=1e-15f;
			if(vdata[j][i][l]==0) vdata[j][i][l]=1e-15f;
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(path+"advU.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(dd,vel[0]); cdws.closeFile();
		
		cdws=new CtlDataWriteStream(path+"advV.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(dd,vel[1]); cdws.closeFile();
		
		cdws=new CtlDataWriteStream(path+"SaddleTest.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(dd,sf,vel[0],vel[1]); cdws.closeFile();
	}
	
	static void genSaddleField(String fout){
		int x=801,t=1,y=x;
		
		Variable sf=new Variable("sf",false,new Range(t,1,y,x));
		
		double inc=2.0/(x-1);
		
		float[][][] sdata=sf.getData()[0];
		
		for(int l=0;l<t;l++)
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++){
			double X=-1.0+inc*i;
			double Y=-1.0+inc*j;
			sdata[j][i][l]=(float)(-50000.0*cos((X-Y)*PI/2.0)+50000.0*cos((X+Y)*PI/2.0));
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(fout,ByteOrder.BIG_ENDIAN);
		cdws.writeData(sf); cdws.closeFile();
	}
	
	static void genTracerField(String fout){
		int x=801,t=1,y=x;
		
		Variable sf=new Variable("tr",false,new Range(t,1,y,x));
		
		//double inc=2.0/(x-1);
		
		float[][][] sdata=sf.getData()[0];
		
		for(int l=0;l<t;l++)
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++){
			double X=120.0+0.02*i;
			double Y=-8.0+0.02*j;
			
			sdata[j][i][l]=0.1f;
			
			if(Math.abs(X-128)<=1&&Math.abs(Y)<=7) sdata[j][i][l]=1.1f;
		}
		
		for(int i=0;i<100;i++) FilterMethods.smooth9(sf);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(fout,ByteOrder.BIG_ENDIAN);
		cdws.writeData(sf); cdws.closeFile();
	}
}
