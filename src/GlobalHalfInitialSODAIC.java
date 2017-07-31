//
import java.nio.ByteOrder;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;

//
public final class GlobalHalfInitialSODAIC{
	//
	private static final float undef=0;
	
	private static final String ipath="E:/SODA/Climatology/";
	private static final String opath="d:/Data/MITgcm/globalRun0.5Deg/";
	
	private static final DiagnosisFactory df=DiagnosisFactory.parseFile(opath+"BATHY/ETOPO5Bathy.ctl");
	
	private static final DataDescriptor bdd=df.getDataDescriptor();
	
	private static final Variable bath=df.getVariables(new Range("",bdd),"bath")[0];
	
	
	public static void main(String[] args){
		process("Temp");
		process("Salt");
		
		// Exf
		process("Taux");
		process("Tauy");
		processSurface("sst");
		processSurface("sss");
	}
	
	static void process(String vname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath+"SODA"+vname+"Clim.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable v=df.getVariables(new Range("lat(-65.25,65.25)",dd),true,vname)[0];
		v.replaceUndefData(undef);
		
		float[] zdef=dd.getZDef().getSamples();
		float[] dzdef=new float[zdef.length];
		float[][] bdata=bath.getData()[0][0];
		float[][][][] vdata=v.getData();
		
		int t=v.getTCount(),z=v.getZCount(),y=v.getYCount(),x=v.getXCount();
		
		dzdef[0]=0;
		if(z>1)
		System.arraycopy(dd.getZDef().getIncrements(),0,dzdef,1,zdef.length-1);
		
		for(int l=0;l<t;l++)
		for(int k=0;k<z;k++)
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++){
			if(bdata[j][i]+zdef[k]<dzdef[k]/2){
				// in ocean
				if(vdata[l][k][j][i]==undef) vdata[l][k][j][i]=findNear(k,j,i,vdata[l]);
			}else{
				// above sea surface or under ground
				vdata[l][k][j][i]=undef;
			}
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath+"IC/SODA"+vname+"IC.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(dd,v);	cdws.closeFile();
	}
	
	static void processSurface(String vname){
		String name="Temp";
		
		if("sss".equals(vname)) name="Salt";
		
		DiagnosisFactory df=DiagnosisFactory.parseFile(ipath+"SODA"+name+"Clim.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Variable v=df.getVariables(new Range("lat(-65.25,65.25);z(1,1)",dd),true,name)[0];
		v.replaceUndefData(undef);
		
		float[][] bdata=bath.getData()[0][0];
		float[][][][] vdata=v.getData();
		
		int t=v.getTCount(),z=v.getZCount(),y=v.getYCount(),x=v.getXCount();
		
		for(int l=0;l<t;l++)
		for(int k=0;k<z;k++)
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++){
			if(bdata[j][i]!=undef){
				if(vdata[l][k][j][i]==undef) vdata[l][k][j][i]=findNear(k,j,i,vdata[l]);
				
			}else vdata[l][k][j][i]=undef;
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(opath+"IC/SODA"+vname+"EXF.dat",ByteOrder.BIG_ENDIAN);
		cdws.writeData(dd,v);	cdws.closeFile();
	}
	
	static float findNear(int k,int j,int i,float[][][] data){
		final int rad=20;
		int y=data[0].length;
		int x=data[0][0].length;
		
		float re=undef;
		
		for(int r=1;r<=rad;r++){
			int count=0;
			float tmp=0;
			
			for(int jj=j-r;jj<=j+r;jj++)
			for(int ii=i-r;ii<=i+r;ii++)
			if(ii>=0&&ii<x&&jj>=0&&jj<y&&data[k][jj][ii]!=undef){
				tmp+=data[k][jj][ii];
				count++;
			}
			
			if(count!=0){
				re=tmp/count;
				break;
			}
		}
		
		if(re==undef) re=data[k-1][j][i];
		
		if(re==undef)
		throw new IllegalArgumentException("(TFirst) no interpolation for point ("+k+", "+j+", "+i+")");
		
		return re;
	}
}
