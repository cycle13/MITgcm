//
package fltDispersion;

import static fltDispersion.FLTUtils.undef;

//
public final class FltRecord{
	//
	private int zlevs =undef;	// how many vertical levels
	
	private float id  =undef;	// A unique float identifier (1,2,3,...)
	
	private long  time=undef;	// current time of observation (in s) + base time
	private float xpos=undef;	// x position of float (in units of XC)
	private float ypos=undef;	// y position of float (in units of YC)
	private float zpos=undef;	// z position of float (in units of ZC)
	
	private float xidx=undef;	// x-direction grid
	private float yidx=undef;	// y-direction grid
	private float zidx=undef;	// z-direction grid
	
	private float pres=undef;	// pressure (dbar)
	
	private float[] uvel=null;	// u-direction velocity (m/s)
	private float[] vvel=null;	// v-direction velocity (m/s)
	private float[] temp=null;	// temperature (degree)
	private float[] salt=null;	// salinity (psu)
	
	
	/**
	 * constructor
	 */
	public FltRecord(float id,long time,float xpos,float ypos,float zpos,
	float xidx,float yidx,float zidx,float pres,float[] uvel,float[] vvel,float[] temp,float[] salt){
		zlevs=uvel.length;
		
		if(zlevs!=vvel.length||zlevs!=temp.length||zlevs!=salt.length)
		throw new IllegalArgumentException("invalid lengths");
		
		this.id  =id  ; this.time=time;
		this.xpos=xpos; this.ypos=ypos;
		this.zpos=zpos; this.xidx=xidx;
		this.yidx=yidx; this.zidx=zidx;
		this.pres=pres; this.uvel=uvel;
		this.vvel=vvel; this.temp=temp;
		this.salt=salt;
	}
	
	
	/*** getor and setor ***/
	public int getLevels(){ return zlevs;}
	
	public long  getTime(){ return time;}
	
	public float getID  (){ return id  ;}
	
	public float getXPos(){ return xpos;}
	
	public float getYPos(){ return ypos;}
	
	public float getZPos(){ return zpos;}
	
	public float getXIdx(){ return xidx;}
	
	public float getYIdx(){ return yidx;}
	
	public float getZIdx(){ return zidx;}
	
	public float getPres(){ return pres;}
	
	public float[] getUVel(){ return uvel;}
	
	public float[] getVVel(){ return vvel;}
	
	public float[] getTemp(){ return temp;}
	
	public float[] getSalt(){ return salt;}
	
	
	/**
	 * used to print
	 */
	public String toString(){
		return String.format(
			"%10.3f %10.3f %9.3f  %7.2f %7.2f %7.2f  %18s %9.3f %9.3f %9.3f  %9.3f  %9.3f",
			xpos,ypos,zpos,xidx,yidx,zidx,time,uvel[0],vvel[0],temp[0],salt[0],pres
		);
	}
	
	public String toString(boolean llrec){
		if(llrec) return toString();
		else return String.format(
			"%10.3f %10.3f %9.3f  %7.2f %7.2f %7.2f  %18s %9.3f %9.3f %9.3f  %9.3f  %9.3f",
			xpos/1000f,ypos/1000f,zpos,xidx,yidx,zidx,time,uvel[0],vvel[0],temp[0],salt[0],pres
		);
	}
}
