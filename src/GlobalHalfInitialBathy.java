//
import java.nio.ByteOrder;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;


// extract bathymetry data from ETOPO 1
public final class GlobalHalfInitialBathy{
	//
	private static final DiagnosisFactory df=DiagnosisFactory.parseFile(
		"E:/ETOPO/ETOPO5.ctl"
	);
	private static final DataDescriptor   dd=df.getDataDescriptor();
	
	//
	public static void main(String[] args){
		Variable tmp=df.getVariables(new Range("",dd),"bath")[0];
		
		final int ny=262,nx=720;
		
		Variable bath=new Variable("bath",new Range(1,1,ny,nx));
		
		float[][] tdata= tmp.getData()[0][0];
		float[][] bdata=bath.getData()[0][0];
		
		for(int j=0;j<ny;j++){
			float lat=-65.25f+0.5f*j;
			
			for(int i=0;i<nx;i++){
				float lon=0.25f+0.5f*i;
				
				int tagi=dd.getXNum(lon);
				int tagj=dd.getYNum(lat);
				
				bdata[j][i]=tdata[tagj][tagi]>0?0:tdata[tagj][tagi];
			}
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(
			"d:/Data/MITgcm/globalRun0.5Deg/BATHY/ETOPO5Bathy.dat",
			ByteOrder.BIG_ENDIAN
		);
		cdws.writeData(bath);	cdws.closeFile();
	}
}
