//
package doublegyre;

import java.nio.ByteOrder;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;

//
public final class BarotropicDB{
	//
	private static final int y=400;
	private static final int x=560;
	
	private static final float depth=3500;
	private static final float tauMax=1f;
	
	private static final String path="/lustre/home/qianyk/MITgcm_c65y/verification/barotropicDG30/input/";
	
	//
	public static void main(String[] args){
		generateBath(path+"BATH/bath.dat");
		generateDye1(path+"DYE/dye1.dat");
		generateDye2(path+"DYE/dye2.dat");
		generateTaux(path+"EXF/taux.dat");
	}
	
	static void generateBath(String fname){
		Variable bath=new Variable("bath",new Range(1,1,y,x));
		
		float[][] bdata=bath.getData()[0][0];
		
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++)
		if(i!=x-1&&j!=y-1) bdata[j][i]=-depth; // set walls
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(fname,ByteOrder.BIG_ENDIAN);
		cdws.writeData(bath); cdws.closeFile();
	}
	
	static void generateDye1(String fname){
		Variable dye=new Variable("dye1",new Range(1,1,y,x));
		
		float max=30f,min=10;
		
		float[][] tdata=dye.getData()[0][0];
		
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++) tdata[j][i]=max-(float)j/(y-1)*(max-min); // set walls
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(fname,ByteOrder.BIG_ENDIAN);
		cdws.writeData(dye); cdws.closeFile();
	}
	
	static void generateDye2(String fname){
		Variable dye=new Variable("dye2",new Range(1,1,y,x));
		
		float[][] tdata=dye.getData()[0][0];
		
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++) tdata[j][i]=(float)(2.0*Math.exp(-Math.hypot(j-y/2f,i-x/6f)/250.0)); // set walls
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(fname,ByteOrder.BIG_ENDIAN);
		cdws.writeData(dye); cdws.closeFile();
	}
	
	static void generateTaux(String fname){
		Variable taux=new Variable("taux",new Range(1,1,y,x));
		
		float[][] tdata=taux.getData()[0][0];
		
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++) tdata[j][i]=(float)(-tauMax*Math.cos(2.0*Math.PI*j/(y-1.0))); // set walls
		//for(int i=0;i<x;i++) tdata[j][i]=(float)(tauMax); // set walls
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(fname,ByteOrder.BIG_ENDIAN);
		cdws.writeData(taux); cdws.closeFile();
	}
}
