//
package common;

import java.io.FileWriter;
import java.io.IOException;
import java.time.temporal.ChronoUnit;

import miniufo.application.basic.DynamicMethodsInSC;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.MDate;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.SphericalSpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.geophysics.Empirical.Scheme;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;


// extract bathymetry data from ETOPO 1
public final class ClimatologyWind{
	//
	public static void main(String[] args){
		//generateClimGS();
		computeStress();
	}
	
	static void computeStress(){
		DiagnosisFactory df=DiagnosisFactory.parseFile("d:/Data/CCMP/CCMPClim.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Range r=new Range("",dd);
		
		Variable[] vs=df.getVariables(r,"uwnd","vwnd");
		
		SphericalSpatialModel ssm=new SphericalSpatialModel(dd);
		DynamicMethodsInSC dm=new DynamicMethodsInSC(ssm);
		
		Variable[] stress1=dm.cWindStress(vs[0],vs[1],Scheme.DO);
		Variable[] stress2=dm.cWindStress(vs[0],vs[1],Scheme.LP);
		Variable[] stress3=dm.cWindStress(vs[0],vs[1],Scheme.LY);
		
		stress1[0].setName("tauxDO");	stress1[1].setName("tauyDO");
		stress2[0].setName("tauxLP");	stress2[1].setName("tauyLP");
		stress3[0].setName("tauxLY");	stress3[1].setName("tauyLY");
		
		DataWrite dw=DataIOFactory.getDataWrite(dd,"d:/Data/CCMP/CCMPStressClim.dat");
		dw.writeData(dd,stress1[0],stress1[1],stress2[0],stress2[1],stress3[0],stress3[1]);
		dw.closeFile();
	}
	
	static void generateClimGS(){
		MDate mstr=new MDate(1988,1,1);
		
		StringBuilder sb=new StringBuilder();
		
		sb.append("'open ccmp.ctl'\n");
		sb.append("'set gxout fwrite'\n");
		sb.append("'set fwrite ./CCMPMonthly.dat'\n\n");
		sb.append("'set x 1 1440'\n\n");
		
		for(int yy=1988;yy<2012;yy++)
		for(int mm=1;mm<=12;mm++){
			int tstr=new MDate(yy,mm,1).getDT(mstr,ChronoUnit.DAYS)*4+1;
			int tend=-1;
			
			if(mm==1||mm==3||mm==5||mm==7||mm==8||mm==10||mm==12)
				tend=new MDate(yy,mm,31).getDT(mstr,ChronoUnit.DAYS)*4+4;
			else if(mm==4||mm==6||mm==9||mm==11)
				tend=new MDate(yy,mm,30).getDT(mstr,ChronoUnit.DAYS)*4+4;
			else if(MDate.isLeapYear(yy))
				tend=new MDate(yy,mm,29).getDT(mstr,ChronoUnit.DAYS)*4+4;
			else
				tend=new MDate(yy,mm,28).getDT(mstr,ChronoUnit.DAYS)*4+4;
			
			sb.append("'d ave(uwnd,t="+tstr+",t="+tend+")'\n");
			sb.append("'d ave(vwnd,t="+tstr+",t="+tend+")'\n\n");
		}
		
		sb.append("'disable fwrite'\n");
		sb.append("'close 1'\n");
		sb.append("'reinit'\n");
		
		try(FileWriter fw=new FileWriter("d:/CCMPMonthly.gs")){ fw.write(sb.toString());}
		catch(IOException err){ err.printStackTrace(); System.exit(0);}
	}
}
