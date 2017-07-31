//
package Preprocess;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

//
public final class GEBCO{
	//
	public static void main(String[] args){
		String ipath="d:/Data/Bathmetry/GEBCO/gebco_08_75_-25_165_45.bin";
		String opath="d:/Data/Bathmetry/GEBCO/gebco_08_75_-25_165_45.dat";
		
		try{
			RandomAccessFile iraf=new RandomAccessFile(ipath,"r");
			RandomAccessFile oraf=new RandomAccessFile(opath,"rw");
			
			FileChannel ifc=iraf.getChannel();
			FileChannel ofc=oraf.getChannel();
			
			ByteBuffer ibuf=ByteBuffer.allocate((int)ifc.size()); ibuf.order(ByteOrder.LITTLE_ENDIAN);
			
			ifc.read(ibuf); ibuf.flip();
			
			ByteBuffer obuf=short2Float(ibuf.asShortBuffer());
			
			ofc.write(obuf);
			
			ifc.close(); iraf.close();
			ofc.close(); oraf.close();
		
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	static ByteBuffer short2Float(ShortBuffer ibuf){
		ByteBuffer obuf=ByteBuffer.allocate(ibuf.capacity()*4);
		obuf.order(ByteOrder.LITTLE_ENDIAN);
		
		FloatBuffer tmp=obuf.asFloatBuffer();
		
		for(int i=0,I=ibuf.capacity();i<I;i++){
			short s=ibuf.get();
			
			tmp.put((float)s);
		}
		
		tmp.flip();
		
		return obuf;
	}
}
