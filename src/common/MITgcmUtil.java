//
package common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static miniufo.diagnosis.SpatialModel.EARTH_RADIUS;
import static miniufo.diagnosis.SpatialModel.EARTH_ROTATE_SPEED;


// extract bathymetry data from ETOPO 1
public final class MITgcmUtil{
	//
	public enum DataPrec{float32,float64}
	
	
	public static float[] readFloatBE(String fname,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int fileLen=getIntFileLength(fname);
		int gc=fileLen/recLen;	// grid count
		
		float[] data=new float[gc];
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"r")){
			FileChannel fc=raf.getChannel();
			
			ByteBuffer buf=ByteBuffer.allocate(fileLen);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			fc.read(buf); buf.clear();
			
			for(int i=0;i<gc;i++) data[i]=buf.getFloat();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return data;
	}
	
	public static float[][] readFloatBE(String fname,int x,int y,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int fileLen=getIntFileLength(fname);
		
		if(fileLen!=(long)x*y*recLen) throw new IllegalArgumentException("file length "+fileLen+" != slice "+(x*y*recLen));
		
		float[][] data=new float[y][x];
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"r")){
			FileChannel fc=raf.getChannel();
			
			ByteBuffer buf=ByteBuffer.allocate(fileLen);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			fc.read(buf); buf.clear();
			
			for(int j=0;j<y;j++) 
			for(int i=0;i<x;i++) data[j][i]=buf.getFloat();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return data;
	}
	
	public static float[][][] readFloatBE(String fname,int x,int y,int z,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int fileLen=getIntFileLength(fname);
		int slice =x*y*recLen;
		
		if(fileLen%slice!=0) throw new IllegalArgumentException("inconsistent grids");
		
		float[][][] data=new float[z][y][x];
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"r")){
			FileChannel fc=raf.getChannel();
			
			ByteBuffer buf=ByteBuffer.allocate(slice);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			for(int k=0;k<z;k++){
				fc.read(buf); buf.clear();
				
				for(int j=0;j<y;j++) 
				for(int i=0;i<x;i++) data[k][j][i]=buf.getFloat();
				
				buf.clear();
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return data;
	}
	
	
	public static double fToLatitude(double f){
		return toDegrees(asin(f/(2.0*EARTH_ROTATE_SPEED)));
	}
	
	public static double betaToLatitude(double beta){
		return toDegrees(acos(beta*EARTH_RADIUS/(2.0*EARTH_ROTATE_SPEED)));
	}
	
	public static double latToF(double lat){
		return 2.0*EARTH_ROTATE_SPEED*sin(toRadians(lat));
	}
	
	public static double latToBeta(double lat){
		return 2.0*EARTH_ROTATE_SPEED*cos(toRadians(lat))/EARTH_RADIUS;
	}
	
	
	/*** helper methods ***/
	private static int getIntFileLength(String fname){
		File f=new File(fname);
		
		if(!f.exists()) throw new IllegalArgumentException("file not found: "+fname);
		
		int fileLen=(int)f.length();
		
		if(fileLen!=f.length()) throw new IllegalArgumentException("file length overflow for integer");
		
		return fileLen;
	}
	
	
	/*** test ***/
	public static void main(String[] args){
		System.out.println(latToF(15));
		System.out.println(latToBeta(15));
	}
}
