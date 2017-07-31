//
import java.nio.ByteOrder;
import miniufo.basic.InterpolationModel;
import miniufo.basic.InterpolationModel.Type;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import miniufo.util.DataInterpolation;

//
public final class GlobalHalfExfFreshWater{
	//
	public static void main(String[] args){
		//interpolateGauss("prate");
		//interpolateGauss("runof");
		//interpolate("evapr");
		//shift0("prate");
		shift5("evapr");
		System.exit(0);
		
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/NCEP/FreshWaterFlux/prateShift.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable prate=df.getVariables(new Range("",dd),"prate")[0];
		
		Variable evapr=DiagnosisFactory.getVariables("d:/Data/WHOI/Evapr/evaprShift.ctl","","evapr")[0];
		
		CtlDataWriteStream cdws=new CtlDataWriteStream("d:/Data/NCEP/FreshWaterFlux/EmP.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(dd,evapr.minusEq(prate));	cdws.closeFile();
	}
	
	
	// interpolate to 0.5-deg
	public static void interpolateGauss(String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/NCEP/FreshWaterFlux/"+vname+"Clim.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		DataInterpolation di=new DataInterpolation(dd);
		di.GaussianToEvenGridInterp("d:/Data/NCEP/FreshWaterFlux/"+vname+"Interp.dat",Type.PERIODIC_CUBIC_P,361,720);
	}
	
	// interpolate to 0.5-deg
	public static void interpolate(String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/WHOI/Evapr/"+vname+"Clim.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		DataInterpolation di=new DataInterpolation(dd);
		di.horizontalInterp("d:/Data/WHOI/Evapr/"+vname+"Interp.dat",Type.PERIODIC_CUBIC_P,Type.CUBIC_P,359,720);
	}
	
	// right shift 0.25бу from 0бу to start from 0.25буE
	public static void shift0(String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/NCEP/FreshWaterFlux/"+vname+"Interp.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		int xc=720,yc=262;
		int ystr=dd.getYNum(-65.5f);
		Variable v=df.getVariables(new Range("",dd),true,vname)[0];
		
		float[][][][] vdata=v.getData();
		
		// shift x
		for(int l=0;l<v.getTCount();l++){
			float[][] buf=new float[v.getYCount()][v.getXCount()];
			
			for(int j=0;j<v.getYCount();j++)
			for(int i=0;i<v.getXCount();i++) buf[j][i]=vdata[l][0][j][i];
			
			for(int j=0;j<v.getYCount();j++){
				for(int i=0;i<v.getXCount()-1;i++) vdata[l][0][j][i]=(buf[j][i]+buf[j][i+1])/2f;
				
				vdata[l][0][j][v.getXCount()-1]=(buf[j][v.getXCount()-1]+buf[j][0])/2f;
			}
		}
		
		Variable re=new Variable(vname,true,new Range(12,1,yc,xc));
		
		float[][][][] rdata=re.getData();
		
		// shift y
		for(int l=0;l<v.getTCount();l++)
		for(int i=0;i<xc;i++)
		for(int j=0;j<yc;j++) rdata[l][0][j][i]=(vdata[l][0][ystr+j][i]+vdata[l][0][ystr+j+1][i])/2f;
		
		// maskout the land
		Variable mask=DiagnosisFactory.getVariables("E:/SODA/Climatology/sst.ctl","t(1,1)","sst")[0];
		float[][] mdata=mask.getData()[0][0];
		
		for(int l=0;l<re.getTCount();l++)
		for(int j=0;j<re.getYCount();j++)
		for(int i=0;i<re.getXCount();i++) if(mdata[j][i]==0f) rdata[l][0][j][i]=0f;
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(
			"D:/Data/NCEP/FreshWaterFlux/"+vname+"Shift.dat",ByteOrder.BIG_ENDIAN
		);
		cdws.writeData(re);	cdws.closeFile();
	}
	
	// left shift 0.25бу from 0.5бу to start from 0.25буE
	public static void shift5(String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/WHOI/Evapr/"+vname+"Interp.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		int xc=720,yc=262;
		int ystr=dd.getYNum(-65.5f);
		
		Variable v=df.getVariables(new Range("",dd),true,vname)[0];
		
		zonalFillUndef(v);
		
		float[][][][] vdata=v.getData();
		
		// shift x
		for(int l=0;l<v.getTCount();l++){
			float[][] buf=new float[v.getYCount()][v.getXCount()];
			
			for(int j=0;j<v.getYCount();j++)
			for(int i=0;i<v.getXCount();i++) buf[j][i]=vdata[l][0][j][i];
			
			for(int j=0;j<v.getYCount();j++){
				vdata[l][0][j][0]=(buf[j][v.getXCount()-1]+buf[j][0])/2f;
				
				for(int i=1;i<v.getXCount();i++) vdata[l][0][j][i]=(buf[j][i]+buf[j][i-1])/2f;
			}
		}
		
		Variable re=new Variable(vname,true,new Range(12,1,yc,xc));
		re.setUndef(0);
		
		float[][][][] rdata=re.getData();
		
		// shift y
		for(int l=0;l<v.getTCount();l++)
		for(int i=0;i<xc;i++)
		for(int j=0;j<yc;j++) rdata[l][0][j][i]=(vdata[l][0][ystr+j][i]+vdata[l][0][ystr+j+1][i])/2f;
		
		// maskout the land
		Variable mask=DiagnosisFactory.getVariables("E:/SODA/Climatology/sst.ctl","t(1,1)","sst")[0];
		float[][] mdata=mask.getData()[0][0];
		
		for(int l=0;l<re.getTCount();l++)
		for(int j=0;j<re.getYCount();j++)
		for(int i=0;i<re.getXCount();i++) if(mdata[j][i]==0f) rdata[l][0][j][i]=0f;
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(
			"D:/Data/WHOI/Evapr/"+vname+"Shift.dat",ByteOrder.BIG_ENDIAN
		);
		cdws.writeData(re);	cdws.closeFile();
	}
	
	
	private static void zonalFillUndef(Variable v){
		int y=v.getYCount(),t=v.getTCount(),z=v.getZCount();
		
		float[][][][] vdata=v.getData();
		
		for(int l=0;l<t;l++)
		for(int k=0;k<z;k++)
		for(int j=0;j<y;j++)
		InterpolationModel.fillUndefInPeriodicData(vdata[l][0][j],Type.PERIODIC_LINEAR,v.getUndef());
	}
}
