//
package doublegyre;

import java.util.List;

import diffuse.DiffusionModel;
import miniufo.application.statisticsModel.LagrangianStatisticsByDavis;
import miniufo.concurrent.ConcurrentUtil;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Variable;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;
import miniufo.lagrangian.Particle;


public final class Diffusivity{
	//
	private static final String path="/lustre/home/qianyk/Data/MITgcm/DoubleGyre30/";
	
	private static final String EulerCTL=
		"dset ^ctl.dat\n"+
		"title template\n"+
		"undef -9999\n"+
		"xdef 41 linear 140 1\n"+
		"ydef 31 linear  15 1\n"+
		"zdef  1 linear   1 1\n"+
		"tdef  1 linear 1Jan2001 1dy\n"+
		"vars 1\n"+
		"u 0 99 u\n"+
		"endvars\n";
	
	
	/** test*/
	public static void main(String[] args){
		ConcurrentUtil.initDefaultExecutor(16);
		cLagrangianStatistics("DG30_smth11");
		ConcurrentUtil.shutdown();
	}
	
	
	/**
	 * compute Lagrangian statistics
	 * 
	 * @param	ps		particle list
	 * @param	lon1	start lon for Lagrangian statistics
	 * @param	lat1	start lat for Lagrangian statistics
	 * @param	lon2	end lon for Lagrangian statistics
	 * @param	lat2	end lat for Lagrangian statistics
	 * @param	tRad	maximum time lag
	 * @param	fname	file name stamp
	 */
	static void cLagrangianStatistics(String tag){
		List<Particle> ps=DiffusionModel.readParticleList(path+tag+"ResLD.dat");
		
		DataDescriptor dd=DiagnosisFactory.parseContent(EulerCTL).getDataDescriptor();
		
		/**************** Lagrangian statistics ****************/
		System.out.println("\nLagrangian Statistics...");
		LagrangianStatisticsByDavis lstat=new LagrangianStatisticsByDavis(ps,dd);
		
		int tRad=120;
		int str =6;
		int end =10;
		int minT=2000;
		
		float bRad=2f;
		
		Variable[] stats=lstat.cMeanStatisticsMapByDavisTheory(tRad,bRad,str,end,minT);
		
		DataWrite dw=DataIOFactory.getDataWrite(dd,path+"LSMap1_smth11.dat");
		dw.writeData(dd,stats);	dw.closeFile();
	}
}
