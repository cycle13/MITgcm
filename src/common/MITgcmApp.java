//
package common;

import miniufo.diagnosis.Variable;
import SCS12.ModelPartialGrids;


//
public class MITgcmApp{
	//
	protected int xgrid=0;
	protected int ygrid=0;
	protected int zgrid=0;
	
	protected float[]       drF=null;
	protected float[][]     dyG=null;
	protected float[][]     dxG=null;
	protected float[][]     RAC=null;
	protected float[][] maskInC=null;
	protected float[][] maskInW=null;
	protected float[][] maskInS=null;
	protected float[][][] maskC=null;
	protected float[][][] maskW=null;
	protected float[][][] maskS=null;
	protected float[][][] hFacW=null;
	protected float[][][] hFacS=null;
	
	protected ModelPartialGrids grid=null;
	
	
	/**
	 * constructor
	 */
	public MITgcmApp(ModelPartialGrids grid){
		this.grid=grid;
		
		xgrid=grid.maskS[0][0].length;
		ygrid=grid.maskS[0].length;
		zgrid=grid.maskS.length;
		
		this.drF    =grid.drF;
		this.dyG    =grid.dyG;
		this.dxG    =grid.dxG;
		this.RAC    =grid.RAC;
		this.maskC  =grid.maskC;
		this.maskS  =grid.maskS;
		this.maskW  =grid.maskW;
		this.hFacS  =grid.hFacS;
		this.hFacW  =grid.hFacW;
		this.maskInC=grid.maskInC;
		this.maskInW=grid.maskInW;
		this.maskInS=grid.maskInS;
	}
	
	
	/**
	 * check dimensions of given variables
	 */
	public void checkXYZDimension(Variable... vs){
		int vc=vs.length,tgrid=vs[0].getTCount();
		
		if(vc<1) throw new IllegalArgumentException("length of vs ("+vc+") should be at least 1");
		
		for(Variable v:vs){
			int t=v.getTCount(),z=v.getZCount(),y=v.getYCount(),x=v.getXCount();
			
			if(t!=tgrid) throw new IllegalArgumentException("t-dims are not the same");
			if(z!=zgrid) throw new IllegalArgumentException("z-dims are not the same");
			if(y!=ygrid) throw new IllegalArgumentException("y-dims are not the same");
			if(x!=xgrid) throw new IllegalArgumentException("x-dims are not the same");
		}
	}
	
	public void checkXYDimension(Variable... vs){
		int vc=vs.length,tgrid=vs[0].getTCount();
		
		if(vc<1) throw new IllegalArgumentException("length of vs ("+vc+") should be at least 1");
		
		for(Variable v:vs){
			int t=v.getTCount(),z=v.getZCount(),y=v.getYCount(),x=v.getXCount();
			
			if(t!=tgrid) throw new IllegalArgumentException("t-dims are not the same");
			if(z!=1    ) throw new IllegalArgumentException("z-dim is not 1");
			if(y!=ygrid) throw new IllegalArgumentException("y-dims are not the same");
			if(x!=xgrid) throw new IllegalArgumentException("x-dims are not the same");
		}
	}
	
	public void checkXZDimension(Variable... vs){
		int vc=vs.length,tgrid=vs[0].getTCount();
		
		if(vc<1) throw new IllegalArgumentException("length of vs ("+vc+") should be at least 1");
		
		for(Variable v:vs){
			int t=v.getTCount(),z=v.getZCount(),y=v.getYCount(),x=v.getXCount();
			
			if(t!=tgrid) throw new IllegalArgumentException("t-dims are not the same");
			if(z!=zgrid) throw new IllegalArgumentException("z-dims are not the same");
			if(y!=1    ) throw new IllegalArgumentException("y-dim is not 1");
			if(x!=xgrid) throw new IllegalArgumentException("x-dims are not the same");
		}
	}
	
	public void checkYZDimension(Variable... vs){
		int vc=vs.length,tgrid=vs[0].getTCount();
		
		if(vc<1) throw new IllegalArgumentException("length of vs ("+vc+") should be at least 1");
		
		for(Variable v:vs){
			int t=v.getTCount(),z=v.getZCount(),y=v.getYCount(),x=v.getXCount();
			
			if(t!=tgrid) throw new IllegalArgumentException("t-dims are not the same");
			if(z!=zgrid) throw new IllegalArgumentException("z-dims are not the same");
			if(y!=ygrid) throw new IllegalArgumentException("y-dims are not the same");
			if(x!=1    ) throw new IllegalArgumentException("x-dim is not 1"         );
		}
	}
	
	
	/**
	 * compute areas for each open boundaries
	 */
	public double cAreaE(){
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int j=0;j<ygrid;j++)
		sum+=dyG[j][xgrid-1]*maskInW[j][xgrid-1]*drF[k]*hFacW[k][j][xgrid-1];
		
		return sum;
	}
	
	public double cAreaW(){
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int j=0;j<ygrid;j++)
		sum+=dyG[j][1]*drF[k]*hFacW[k][j][1]*maskInW[j][1];
		
		return sum;
	}
	
	public double cAreaS(){
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int i=0;i<xgrid;i++)
		sum+=dxG[1][i]*drF[k]*hFacS[k][1][i]*maskInS[1][i];
		
		return sum;
	}
	
	public double cAreaN(){
		double sum=0;
		
		for(int k=0;k<zgrid;k++)
		for(int i=0;i<xgrid;i++)
		sum+=dxG[ygrid-1][i]*drF[k]*hFacS[k][ygrid-1][i]*maskInS[ygrid-1][i];
		
		return sum;
	}
	
	public double cAreaF(){
		double sum=0;
		
		for(int j=0;j<ygrid;j++)
		for(int i=0;i<xgrid;i++) sum+=RAC[j][i]*maskInC[j][i];
		
		return sum;
	}
	
	
	/*** getor and setor ***/
	public ModelPartialGrids getGrids(){ return grid;}
}
