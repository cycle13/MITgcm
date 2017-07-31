//
package Andaman;

import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;
import miniufo.statistics.FilterModel;

//
public final class RemoveTides{
	//
	private static final String path="D:/Data/MITgcm/Andaman/24th/";
	
	
	//
	public static void main(String[] args){
		DiagnosisFactory df=DiagnosisFactory.parseFile(path+"Surf.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		//Range r=new Range("lat(15,15);lon(114,114);t(1,721)",dd);
		//Range r=new Range("y(325,325);x(373,373);t(1,721);z(1,1)",dd);
		Range r=new Range("z(1,1);t(48,720)",dd);
		
		Variable eta=df.getVariables(r,"eta")[0]; eta.anomalizeT();System.out.println(eta);
		Variable ori=eta.copy(); ori.setName("ori");
		
		float[][][] edata=eta.getData()[0];
		float[][][] odata=ori.getData()[0];
		
		for(int j=0;j<ori.getYCount();j++){
			if(j%10==0) System.out.print(".");
			for(int i=0;i<ori.getXCount();i++)
			edata[j][i]=FilterModel.FourierFilter(odata[j][i],
				12.4206f,12f,12.6583f,11.9672f,23.9345f,25.8193f,24.0659f,26.8684f,327.8599f,661.31f
			);
		}
		
		System.out.println();
		
		DataWrite dw=DataIOFactory.getDataWrite(dd,path+"SurfNoTide2.dat");
		dw.writeData(dd,ori,eta); dw.closeFile();
	}
}
