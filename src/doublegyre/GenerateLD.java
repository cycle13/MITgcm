//
package doublegyre;

import java.util.List;
import java.util.function.Function;
import diffuse.DiffusionModel;
import miniufo.concurrent.ConcurrentUtil;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.lagrangian.LSM0th;
import miniufo.lagrangian.Particle;
import miniufo.lagrangian.Record;
import miniufo.lagrangian.StochasticModel;
import miniufo.lagrangian.StochasticModel.BCType;
import miniufo.lagrangian.StochasticParams;
import miniufo.util.Region2D;


public final class GenerateLD{
	// domain parameters
	//private static final float olon=160;		// center longitude
	//private static final float olat=30;			// center latitude
	
	// tracking parameters
	private static final int  intLen=252;		// length of integration
	private static final float kappa=0;			// m^2/s
	
	private static final float[][] diff=new float[][]{{kappa,0},{0,kappa}};
	
	// general parameters
	private static final boolean writeTraj=true;
	
	private static final String path="/lustre/home/qianyk/Data/MITgcm/DoubleGyre30/";
	
	
	/** test*/
	public static void main(String[] args){
		ConcurrentUtil.initDefaultExecutor(1);
		
		generateLagrangianData("DG30_smth11");
		
		ConcurrentUtil.shutdown();
	}
	
	/**
	 * generate synthetic Lagrangian drifter data
	 * 
	 * @param ctlname	mean flow data
	 * @param ensemble	ensemble number
	 * @param intLen	length of integration (steps)
	 * 
	 * @return	ps		simulated particles
	 */
	static void generateLagrangianData(String tag){
		DiagnosisFactory df=DiagnosisFactory.parseFile(path+tag+".ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Function<Record,StochasticParams> f1=r->{ return new StochasticParams(dd.getDTDef()[0],diff);};
		
		StochasticModel sm=new LSM0th(10,false,dd,BCType.Landing,BCType.Landing,f1);
		sm.setVelocityBuffer("u","v",1);	// set initial velocity buffer
		
		Region2D region=new Region2D(140.1f,15.1f,179.9f,44.9f);
		
		List<Particle> ps=sm.deployPatch(region,0.2f,1,intLen);
		
		sm.simulateParticles(ps,"u","v",intLen);
		
		if(writeTraj) DiffusionModel.writeTrajAndGS(ps,path+"TXT/",region);
		
		System.out.println("Test of "+tag+" has "+ps.size()+" particles");
		
		DiffusionModel.writeParticleList(path+tag+"LD.dat",ps);
	}
}
