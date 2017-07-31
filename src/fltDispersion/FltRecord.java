//
package fltDispersion;


//
public final class FltRecord{
	//
	private int zlevs =-999;	// how many vertical levels
	
	private float id  =-999;	// A unique float identifier (1,2,3,...)
	
	private long  time=-999;	// current time of observation (in s) + base time
	private float xpos=-999;	// x position of float (in units of XC)
	private float ypos=-999;	// y position of float (in units of YC)
	private float zpos=-999;	// z position of float (in units of ZC)
	
	private float xidx=-999;	// x-direction grid
	private float yidx=-999;	// y-direction grid
	private float zidx=-999;	// z-direction grid
	
	private float pres=-999;	// pressure (dbar)
	
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
		return String.format("%10.3f %10.3f %9.3f  %7.2f %7.2f %7.2f  %18s %9.3f %9.3f %9.3f  %9.3f  %9.3f",xpos,ypos,zpos,xidx,yidx,zidx,time,uvel[0],vvel[0],temp[0],salt[0],pres);
	}
}
