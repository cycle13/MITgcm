//
package fltDispersion;

import java.util.List;
import java.util.function.Predicate;
import diffuse.DiffusionModel;
import miniufo.application.statisticsModel.LagrangianStatisticsByDavis;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.lagrangian.Particle;
import miniufo.lagrangian.Record;
import miniufo.util.Region2D;


public final class Diffusivity{
	//
	private static final String path="D:/Data/MITgcm/flt/";
	
	private static final String EulerCTL=
		"dset ^ctl.dat\n"+
		"title template\n"+
		"undef -9999\n"+
		"xdef 31 linear 140 2\n"+
		"ydef 11 linear -10 2\n"+
		"zdef  1 linear   1 1\n"+
		"tdef  1 linear 1Jan2001 1dy\n"+
		"vars 1\n"+
		"u 0 99 u\n"+
		"endvars\n";
	
	
	/** test*/
	public static void main(String[] args){
		Region2D region=new Region2D(120f,-8,136,8);
		cLagrangianStatistics(region,158,"SaddleTest");
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
	static void cLagrangianStatistics(Region2D region,int tRad,String tag){
		List<Particle> ps=DiffusionModel.readParticleList(path+tag+"LD2.dat");
		
		DataDescriptor dd=DiagnosisFactory.parseContent(EulerCTL).getDataDescriptor();
		
		/**************** Lagrangian statistics ****************/
		System.out.println("\nLagrangian Statistics...");
		LagrangianStatisticsByDavis lstat=new LagrangianStatisticsByDavis(ps,dd);
		
		Predicate<Record> cond=r->region.inRange(r.getLon(),r.getLat());
		//Predicate<Record> cond=r->r.getTime()==20000101000000L;
		
		lstat.cStatisticsByDavisTheory1(cond,tRad).toFile(path+"Diff/Lstat"+tag+"1.txt");
		lstat.cStatisticsByDavisTheory2(cond,tRad).toFile(path+"Diff/Lstat"+tag+"2.txt");
		lstat.cStatisticsByDavisTheory3(cond,tRad).toFile(path+"Diff/Lstat"+tag+"3.txt");
	}
}
