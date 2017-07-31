//
package doublegyre;

import java.util.concurrent.TimeUnit;
import miniufo.application.statisticsModel.StatisticsBasicAnalysisMethods;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;
import miniufo.util.TicToc;

//
public final class EulerianAnalysis{
	//
	//private static final int y=400;
	//private static final int x=560;
	
	private static final String path="D:/Data/MITgcm/barotropicDG/BetaCartRL/";
	
	//
	public static void main(String[] args){
		DiagnosisFactory df=DiagnosisFactory.parseFile(path+"Stat.ctl");
		
		DataDescriptor dd=df.getDataDescriptor();
		TicToc.tic("start reading");
		Variable[] vs=df.getVariables(new Range("t(300,720)",dd),false,"eta","u","v","sf","vor");
		TicToc.toc(TimeUnit.SECONDS);
		
		Variable eta=vs[0];
		Variable u  =vs[1];
		Variable v  =vs[2];
		Variable sf =vs[3];
		Variable vor=vs[4];
		
		TicToc.tic("start averaging");
		Variable etam=eta.anomalizeT();
		Variable   um=  u.anomalizeT();
		Variable   vm=  v.anomalizeT();
		Variable  sfm= sf.anomalizeT();
		Variable vorm=vor.anomalizeT();
		TicToc.toc(TimeUnit.SECONDS);
		
		TicToc.tic("start std");
		Variable etaStd=StatisticsBasicAnalysisMethods.cTStandardDeviation(eta); etaStd.setName("etaStd"); etaStd.setComment("etaStd");
		Variable   uStd=StatisticsBasicAnalysisMethods.cTStandardDeviation(  u);   uStd.setName(  "uStd");   uStd.setComment(  "uStd");
		Variable   vStd=StatisticsBasicAnalysisMethods.cTStandardDeviation(  v);   vStd.setName(  "vStd");   vStd.setComment(  "vStd");
		Variable  sfStd=StatisticsBasicAnalysisMethods.cTStandardDeviation( sf);  sfStd.setName( "sfStd");  sfStd.setComment( "sfStd");
		Variable vorStd=StatisticsBasicAnalysisMethods.cTStandardDeviation(vor); vorStd.setName("vorStd"); vorStd.setComment("vorStd");
		TicToc.toc(TimeUnit.SECONDS);
		
		TicToc.tic("start EKE");
		Variable EKE=u.powEq(2f).plusEq(v.powEq(2f)).divideEq(2).anomalizeT();  EKE.setName("EKE");  EKE.setComment("EKE");
		TicToc.toc(TimeUnit.SECONDS);
		
		DataWrite dw=DataIOFactory.getDataWrite(dd,path+"EulerianAnalysis.dat");
		dw.writeData(dd,etam,um,vm,sfm,vorm,etaStd,uStd,vStd,sfStd,vorStd,EKE); dw.closeFile();
	}
}
