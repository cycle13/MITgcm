//
package common;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import miniufo.io.IOUtil;
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
		int fileLen=IOUtil.getFileLength(fname);
		int gc=fileLen/recLen;	// grid count
		
		float[] data=new float[gc];
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"r")){
			FileChannel fc=raf.getChannel();
			
			ByteBuffer buf=ByteBuffer.allocate(fileLen);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			fc.read(buf); buf.clear();
			
			if(prec==DataPrec.float32)
				for(int i=0;i<gc;i++) data[i]=buf.getFloat();
			else
				for(int i=0;i<gc;i++) data[i]=(float)buf.getDouble();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return data;
	}
	
	public static float[][] readFloatBE(String fname,int x,int y,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int fileLen=IOUtil.getFileLength(fname);
		
		if(fileLen!=(long)x*y*recLen) throw new IllegalArgumentException("file length "+fileLen+" != slice "+(x*y*recLen));
		
		float[][] data=new float[y][x];
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"r")){
			FileChannel fc=raf.getChannel();
			
			ByteBuffer buf=ByteBuffer.allocate(fileLen);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			fc.read(buf); buf.clear();
			
			if(prec==DataPrec.float32)
				for(int j=0;j<y;j++) 
				for(int i=0;i<x;i++) data[j][i]=buf.getFloat();
			else
				for(int j=0;j<y;j++) 
				for(int i=0;i<x;i++) data[j][i]=(float)buf.getDouble();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return data;
	}
	
	public static float[][][] readFloatBE(String fname,int x,int y,int z,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int fileLen=IOUtil.getFileLength(fname);
		int slice =x*y*recLen;
		
		if(fileLen%slice!=0) throw new IllegalArgumentException("inconsistent grids");
		
		float[][][] data=new float[z][y][x];
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"r")){
			FileChannel fc=raf.getChannel();
			
			ByteBuffer buf=ByteBuffer.allocate(slice);
			buf.order(ByteOrder.BIG_ENDIAN);
			
			for(int k=0;k<z;k++){
				fc.read(buf); buf.clear();
				
				if(prec==DataPrec.float32)
					for(int j=0;j<y;j++) 
					for(int i=0;i<x;i++) data[k][j][i]=buf.getFloat();
				else
					for(int j=0;j<y;j++) 
					for(int i=0;i<x;i++) data[k][j][i]=(float)buf.getDouble();
				
				buf.clear();
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return data;
	}
	
	
	public static float[] readFloatBE(FileChannel fc,int x,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		
		float[] data=new float[x];
		
		ByteBuffer buf=ByteBuffer.allocate(x*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try{ fc.read(buf);}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		buf.clear();
		
		if(prec==DataPrec.float32)
			for(int i=0;i<x;i++) data[i]=buf.getFloat();
		else
			for(int i=0;i<x;i++) data[i]=(float)buf.getDouble();
		
		return data;
	}
	
	public static float[][] readFloatBE(FileChannel fc,int x,int y,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		
		float[][] data=new float[y][x];
		
		ByteBuffer buf=ByteBuffer.allocate(x*y*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try{ fc.read(buf);}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		buf.clear();
		
		if(prec==DataPrec.float32)
			for(int j=0;j<y;j++) 
			for(int i=0;i<x;i++) data[j][i]=buf.getFloat();
		else
			for(int j=0;j<y;j++) 
			for(int i=0;i<x;i++) data[j][i]=(float)buf.getDouble();
		
		buf.clear();
		
		return data;
	}
	
	public static float[][][] readFloatBE(FileChannel fc,int x,int y,int z,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		
		float[][][] data=new float[z][y][x];
		
		ByteBuffer buf=ByteBuffer.allocate(x*y*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try{
			for(int k=0;k<z;k++){
				fc.read(buf); buf.clear();
				
				if(prec==DataPrec.float32)
					for(int j=0;j<y;j++) 
					for(int i=0;i<x;i++) data[k][j][i]=buf.getFloat();
				else
					for(int j=0;j<y;j++) 
					for(int i=0;i<x;i++) data[k][j][i]=(float)buf.getDouble();
				
				buf.clear();
			}
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return data;
	}
	
	
	public static void writeFloatBE(String fname,float[] data,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int len=data.length;
		
		ByteBuffer buf=ByteBuffer.allocate(len*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"rw")){
			FileChannel fc=raf.getChannel();
			
			if(prec==DataPrec.float32)
				for(int i=0;i<len;i++) buf.putFloat(data[i]);
			else
				for(int i=0;i<len;i++) buf.putDouble(data[i]);
			
			buf.clear(); fc.write(buf); buf.clear();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	public static void writeFloatBE(String fname,float[][] data,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int y=data.length,x=data[0].length;
		
		ByteBuffer buf=ByteBuffer.allocate(x*y*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"rw")){
			FileChannel fc=raf.getChannel();
			
			if(prec==DataPrec.float32)
				for(int j=0;j<y;j++)
				for(int i=0;i<x;i++) buf.putFloat(data[j][i]);
			else
				for(int j=0;j<y;j++)
				for(int i=0;i<x;i++) buf.putDouble(data[j][i]);
			
			buf.clear(); fc.write(buf); buf.clear();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	public static void writeFloatBE(String fname,float[][][] data,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int z=data.length,y=data[0].length,x=data[0][0].length;
		
		ByteBuffer buf=ByteBuffer.allocate(x*y*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try(RandomAccessFile raf=new RandomAccessFile(fname,"rw")){
			FileChannel fc=raf.getChannel();
			
			for(int k=0;k<z;k++){
				if(prec==DataPrec.float32)
					for(int j=0;j<y;j++)
					for(int i=0;i<x;i++) buf.putFloat(data[k][j][i]);
				else
					for(int j=0;j<y;j++)
					for(int i=0;i<x;i++) buf.putDouble(data[k][j][i]);
				
				buf.clear(); fc.write(buf); buf.clear();
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	
	public static void writeFloatBE(FileChannel fc,float[] data,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int len=data.length;
		
		ByteBuffer buf=ByteBuffer.allocate(len*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try{
			if(prec==DataPrec.float32)
				for(int i=0;i<len;i++) buf.putFloat(data[i]);
			else
				for(int i=0;i<len;i++) buf.putDouble(data[i]);
			
			buf.clear(); fc.write(buf); buf.clear();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	public static void writeFloatBE(FileChannel fc,float[][] data,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int y=data.length,x=data[0].length;
		
		ByteBuffer buf=ByteBuffer.allocate(x*y*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try{
			if(prec==DataPrec.float32)
				for(int j=0;j<y;j++)
				for(int i=0;i<x;i++) buf.putFloat(data[j][i]);
			else
				for(int j=0;j<y;j++)
				for(int i=0;i<x;i++) buf.putDouble(data[j][i]);
			
			buf.clear(); fc.write(buf); buf.clear();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	public static void writeFloatBE(FileChannel fc,float[][][] data,DataPrec prec){
		int recLen=(prec==DataPrec.float32?4:8);
		int z=data.length,y=data[0].length,x=data[0][0].length;
		
		ByteBuffer buf=ByteBuffer.allocate(x*y*recLen);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		try{
			for(int k=0;k<z;k++){
				if(prec==DataPrec.float32)
					for(int j=0;j<y;j++)
					for(int i=0;i<x;i++) buf.putFloat(data[k][j][i]);
				else
					for(int j=0;j<y;j++)
					for(int i=0;i<x;i++) buf.putDouble(data[k][j][i]);
				
				buf.clear(); fc.write(buf); buf.clear();
			}
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
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
	
	
	/*** test ***/
	public static void main(String[] args){
		System.out.println(latToF(20));
		System.out.println(latToBeta(20));
	}
}
