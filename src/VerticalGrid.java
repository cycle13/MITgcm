//
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import miniufo.io.TextReader;


//
public final class VerticalGrid{
	//
	private static final String ctlpath="E:/SODA/Climatology/SODATempClim.ctl";
	
	
	//
	public static void main(String[] args){
		//computeDelR();
		//findWetPointDistribution();
		dumpWetPoint();
	}
	
	static void dumpWetPoint(){
		float[][] data=TextReader.readColumnsF("D:/Data/MITgcm/globalRun0.5Deg/STDERR.0000",false,1,2,3);
		
		float[] ii=data[0];
		float[] jj=data[1];
		float[] kk=data[2];
		
		Variable bath=DiagnosisFactory.getVariables(
			"D:/Data/MITgcm/globalRun0.5Deg/BATHY/ETOPO5Bathy.ctl",
			"","bath"
		)[0];
		
		DiagnosisFactory df=DiagnosisFactory.parseFile("D:/Data/MITgcm/globalRun0.5Deg/IC/SODATempIC.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable temp=df.getVariables(new Range("t(1,1)",dd),"temp")[0];
		
		float[]     zdef =dd.getZDef().getSamples();
		float[]    dzdef =dd.getZDef().getIncrements();
		float[][]   bdata=bath.getData()[0][0];
		float[][][] tdata=temp.getData()[0];
		
		for(int i=0;i<4433;i++){
			int itag=Math.round(ii[i])-1;
			int jtag=Math.round(jj[i])-1;
			int ktag=Math.round(kk[i])-1;
			int dztag=(ktag==temp.getZCount()-1)?temp.getZCount()-2:ktag;
			
			System.out.println(
				"i:"+itag+"  j:"+jtag+"  k:"+ktag+
				"  bath:"+bdata[jtag][itag]+"  zdef:"+(-zdef[ktag])+"  b-z:"+(bdata[jtag][itag]+zdef[ktag])+
				"  dz:"+dzdef[dztag]+"  temp:"+tdata[ktag][jtag][itag]
			);
		}
	}
	
	static void findWetPointDistribution(){
		float[][] data=TextReader.readColumnsF("D:/Data/MITgcm/globalRun0.5Deg/STDERR.0000",false,1,2,3);
		
		int z=40,y=262,x=720;
		
		float[] ii=data[0];
		float[] jj=data[1];
		float[] kk=data[2];
		
		Variable v=new Variable("wet",new Range(1,z,y,x));
		
		float[][][] vdata=v.getData()[0];
		
		for(int i=0;i<4433;i++)
		vdata[Math.round(kk[i])-1][Math.round(jj[i])-1][Math.round(ii[i])-1]=1;
		
		CtlDataWriteStream cdws=new CtlDataWriteStream("d:/Data/MITgcm/globalRun0.5Deg/wet.dat");
		cdws.writeData(v);	cdws.closeFile();
	}
	
	static void computeDelR(){
		float[] rC=DiagnosisFactory.parseFile(ctlpath).getDataDescriptor().getZDef().getSamples();
		
		int K=rC.length;
		
		float[] delR=new float[K];
		
		delR[0]=rC[0]*2f;
		
		for(int k=1;k<K;k++){
			delR[k]=((rC[k]-rC[k-1])-delR[k-1]/2f)*2f;
			System.out.print(delR[k]+", ");
			if(k%10==0) System.out.println();
		}
	}
}
