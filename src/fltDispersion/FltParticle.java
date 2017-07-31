//
package fltDispersion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


//
public final class FltParticle{
	//
	public static final int nFieldRecord=13;
	
	public int id  =-999;	// A unique float identifier (1,2,3,...)
	
	public List<FltRecord> recs=null;
	
	
	//
	public FltParticle(int id,List<FltRecord> recs){
		this.id=id;
		this.recs=recs;
	}
	
	
	public void toTrajectoryFile(String path){
		StringBuilder buf=new StringBuilder();
		
		buf.append("* "+recs.size()+" id: "+id+"\n");
		
		for(FltRecord r:recs){
			buf.append(r);
			buf.append("\n");
		}
		
		try(FileWriter fw=new FileWriter(new File(path+id+".txt"))){
			fw.write(buf.toString());
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	
	/**
	 * used to print
	 */
	public String toString(){
		StringBuilder buf=new StringBuilder();
		
		buf.append(getClass().getSimpleName()+" id ("+id+") "+recs.size()+" records:\n");
		buf.append("  lons(deg)  lats(deg)   zlev(m)   x-idx   y-idx   z-idx time(YYYYMMDDHHMMSS) uspd(m/s) vspd(m/s) temp(deg)  salt(psu) pres(dbar)\n");
		
		for(FltRecord r:recs){
			buf.append(r);
			buf.append("\n");
		}
		
		return buf.toString();
	}
}
