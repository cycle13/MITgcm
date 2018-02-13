//
package fltDispersion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import static fltDispersion.FLTUtils.undef;


//
public final class FltParticle implements Serializable{
	//
	private static final long serialVersionUID = 3655927615984381690L;
	
	public static final int nFieldRecord=13;
	
	public int id  =undef;	// A unique float identifier (1,2,3,...)
	
	public boolean llpos=true;	// whether the x-pos/y-pos is lat/lon (degree) or not (m)
	
	public List<FltRecord> recs=null;
	
	
	//
	public FltParticle(int id,List<FltRecord> recs){
		this.id=id;
		this.recs=recs;
		
		for(FltRecord r:recs) if(r.getXPos()>720&&Math.abs(r.getYPos())>360) llpos=false;
	}
	
	
	public void toTrajectoryFile(String path){
		StringBuilder buf=new StringBuilder();
		
		buf.append("* "+recs.size()+" id: "+id+"\n");
		
		for(FltRecord r:recs){
			buf.append(r.toString(llpos));
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
		if(llpos)
			buf.append("  lons(deg)  lats(deg)   zlev(m)   x-idx   y-idx   z-idx time(YYYYMMDDHHMMSS) uspd(m/s) vspd(m/s) temp(deg)  salt(psu) pres(dbar)\n");
		else
			buf.append("  xpos (km)  ypos (km)   zlev(m)   x-idx   y-idx   z-idx time(YYYYMMDDHHMMSS) uspd(m/s) vspd(m/s) temp(deg)  salt(psu) pres(dbar)\n");
		
		for(FltRecord r:recs){
			buf.append(r.toString(llpos));
			buf.append("\n");
		}
		
		return buf.toString();
	}
}
