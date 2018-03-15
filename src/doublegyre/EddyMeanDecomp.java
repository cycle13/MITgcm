//
package doublegyre;

import java.util.List;
import diffuse.DiffusionModel;
import miniufo.application.statisticsModel.BinningStatistics;
import miniufo.application.statisticsModel.EulerianStatistics;
import miniufo.basic.ArrayUtil;
import miniufo.concurrent.ConcurrentUtil;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Variable;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;
import miniufo.lagrangian.GDPDrifter;
import miniufo.lagrangian.Particle;


public final class EddyMeanDecomp{
	//
	private static boolean cEulerianStatistics=true;
	
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
		ConcurrentUtil.initDefaultExecutor(1);
		
		eddyMeanFlowDecomposition("DG30_smth11");
		
		ConcurrentUtil.shutdown();
	}
	
	
	/**
	 * compute Eulerian and Lagrangian statistics
	 * 
	 * @param	ps		particle list
	 * @param	lon1	start lon for Lagrangian statistics
	 * @param	lat1	start lat for Lagrangian statistics
	 * @param	lon2	end lon for Lagrangian statistics
	 * @param	lat2	end lat for Lagrangian statistics
	 * @param	tRad	maximum time lag
	 * @param	fname	file name stamp
	 */
	static void eddyMeanFlowDecomposition(String tag){
		List<Particle> ps=DiffusionModel.readParticleList(path+tag+"LD.dat");
		
		DataDescriptor dd=DiagnosisFactory.parseContent(EulerCTL).getDataDescriptor();
		
		/**************** Eulerian statistics ****************/
		System.out.println("\nEulerian Statistics...");
		
		System.out.println(" using binning ("+dd.getDXDef()[0]+"-deg) method...");
		
		EulerianStatistics estat=new EulerianStatistics(ps,dd,false);
		
		if(cEulerianStatistics){
			Variable[] count=new Variable[]{new BinningStatistics(dd).binningCount(ps)};
			Variable[] mean=estat.cMeansOfBins();
			Variable[][] ssnl=estat.cSeasonalMeans(DiffusionModel.season2,GDPDrifter.UVEL,GDPDrifter.VVEL);
			Variable[] bias=estat.cSeasonalSamplingBias();
			
			DataWrite dw=DataIOFactory.getDataWrite(dd,path+"Estat"+tag+".dat");
			dw.writeData(dd,ArrayUtil.concatAll(Variable.class,
				count,mean,bias,ArrayUtil.concatAll(Variable.class,ssnl)
			));
			dw.closeFile();
		}
		
		estat.removeMeansOfBins();
		
		DiffusionModel.writeParticleList(path+tag+"ResLD.dat",ps);
		
		System.out.println("finish decomposition");
	}
}
