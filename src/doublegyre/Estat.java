//
package doublegyre;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import miniufo.application.statisticsModel.FilterMethods;
import miniufo.application.statisticsModel.StatisticsBasicAnalysisMethods;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;


public final class Estat{
	//
	private static final String path="/lustre/home/qianyk/Data/MITgcm/DoubleGyre30/";
	
	
	/** test*/
	public static void main(String[] args){
		DiagnosisFactory df=DiagnosisFactory.parseFile(path+"DG30.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Range r=new Range("",dd);
		
		Variable[] vars=df.getVariables(r,"u","v");
		
		int mean=11;
		
		FilterMethods.TRunningMean(vars[0],mean);
		FilterMethods.TRunningMean(vars[1],mean);
		
		vars[0].anomalizeT();
		vars[1].anomalizeT();
		
		Variable u=vars[0];
		Variable v=vars[1];
		
		Variable[] ellipse=StatisticsBasicAnalysisMethods.cVarianceEllipse(u,v);
		
		writeDataForMatlab(path+"ellipse_"+mean+".txt",dd,3*10,ellipse);
	}
	
	static void writeDataForMatlab(String path,DataDescriptor dd,int skip,Variable... vs){
		try(BufferedWriter br=new BufferedWriter(new FileWriter(path))){
			int y=dd.getYCount(),x=dd.getXCount();
			
			float[] xdef=dd.getXDef().getSamples();
			float[] ydef=dd.getYDef().getSamples();
			
			if(vs[0].isTFirst())
			for(int j=0;j<y;j+=skip)
			for(int i=0;i<x;i+=skip){
				StringBuilder sb=new StringBuilder();
				
				sb.append(xdef[i]+"  "+ydef[j]+"  ");
				
				for(int m=0,M=vs.length-1;m<M;m++) sb.append(vs[m].getData()[0][0][j][i]+"  ");
				sb.append(vs[vs.length-1].getData()[0][0][j][i]+"\n");
				
				br.write(sb.toString());
			}
			else
			for(int j=0;j<y;j+=skip)
			for(int i=0;i<x;i+=skip){
				StringBuilder sb=new StringBuilder();
				
				sb.append(xdef[i]+"  "+ydef[j]+"  ");
				
				for(int m=0,M=vs.length-1;m<M;m++) sb.append(vs[m].getData()[0][j][i][0]+"  ");
				sb.append(vs[vs.length-1].getData()[0][j][i][0]+"\n");
				
				br.write(sb.toString());
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
}
