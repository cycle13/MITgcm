//
import java.nio.ByteOrder;

import miniufo.basic.InterpolationModel.Type;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import miniufo.util.DataInterpolation;

//
public final class GlobalHalfExfQNet{
	//
	public static void main(String[] args){
		//interpolate();
		shift();
	}
	
	// interpolate to 0.5-deg
	public static void interpolate(){
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/Flux/NCEP/Qnet.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		DataInterpolation di=new DataInterpolation(dd);
		di.GaussianToEvenGridInterp("d:/Data/Flux/NCEP/QnetInterp.dat",Type.PERIODIC_CUBIC_P,361,720);
	}
	
	// shift 0.25-deg to start from 0.25°„E
	public static void shift(){
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/Flux/NCEP/QnetInterp.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		int xc=720,yc=262;
		int ystr=dd.getYNum(-65.5f);
		Variable v=df.getVariables(new Range("",dd),true,"qnet")[0];
		
		float[][][][] vdata=v.getData();
		
		for(int l=0;l<v.getTCount();l++){
			float[][] buf=new float[v.getYCount()][v.getXCount()];
			
			for(int j=0;j<v.getYCount();j++)
			for(int i=0;i<v.getXCount();i++) buf[j][i]=vdata[l][0][j][i];
			
			for(int j=0;j<v.getYCount();j++){
				for(int i=0;i<v.getXCount()-1;i++) vdata[l][0][j][i]=(buf[j][i]+buf[j][i+1])/2f;
				
				vdata[l][0][j][v.getXCount()-1]=(buf[j][v.getXCount()-1]+buf[j][0])/2f;
			}
		}
		
		Variable re=new Variable("qnet",true,new Range(12,1,yc,xc));
		
		float[][][][] rdata=re.getData();
		
		for(int l=0;l<v.getTCount();l++)
		for(int i=0;i<xc;i++)
		for(int j=0;j<yc;j++) rdata[l][0][j][i]=(vdata[l][0][ystr+j][i]+vdata[l][0][ystr+j+1][i])/2f;
		
		// maskout the land
		Variable mask=DiagnosisFactory.getVariables("E:/SODA/Climatology/sst.ctl","t(1,1)","sst")[0];
		float[][] mdata=mask.getData()[0][0];
		
		for(int l=0;l<re.getTCount();l++)
		for(int j=0;j<re.getYCount();j++)
		for(int i=0;i<re.getXCount();i++) if(mdata[j][i]==0f) rdata[l][0][j][i]=0f;
		
		CtlDataWriteStream cdws=new CtlDataWriteStream("D:/Data/Flux/NCEP/QnetClim.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(re);	cdws.closeFile();
	}
}
