
//
import java.nio.ByteOrder;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;

//
public final class GlobalHalfInitialSODABathy{
	//
	private static final String ipath="E:/SODA/Climatology/";
	private static final String opath="d:/Data/MITgcm/globalRun0.5Deg/BATHY/";
	
	
	public static void main(String[] args){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath+"SODASaltClim.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable v=df.getVariables(new Range("lat(-65.25,65.25);t(1,1)",dd),"salt")[0];
		v.replaceUndefData(0);
		
		int z=v.getZCount(),y=v.getYCount(),x=v.getXCount();
		float undef=0;
		
		Variable bath=new Variable("bath",new Range(1,1,y,x));
		bath.setUndef(0);	bath.setCommentAndUnit("bathymetry");
		bath.getRange().setYRange(v.getRange());
		
		float[]      zdef=dd.getZDef().getSamples();
		float[][]   bdata=bath.getData()[0][0];
		float[][][] tdata=v.getData()[0];
		
		for(int j=0;j<y;j++)
	cc: for(int i=0;i<x;i++){
			if(tdata[0][j][i]==undef) continue cc;
			
			for(int k=1;k<z;k++)
			if(tdata[k][j][i]==undef){
				bdata[j][i]=-zdef[k]+5;
				continue cc;
			}
			
			if(tdata[z-1][j][i]!=undef) bdata[j][i]=-6100;
		}
		
		int count=0;
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++){
			for(int k=0;k<z;k++)
			if(bdata[j][i]<-zdef[k]&&tdata[k][j][i]==undef) count++;
		}
		
		System.out.println(count);
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath+"SODABathy.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(dd,bath);	cdws.closeFile();
	}
}
