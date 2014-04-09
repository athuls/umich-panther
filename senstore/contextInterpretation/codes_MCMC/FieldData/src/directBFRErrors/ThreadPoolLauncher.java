package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import approximateApproach.ComputeBoundingBox;
import approximateApproach.ComputeBoundingBoxInterface;
import approximateApproach.ApproximateAlgorithm;
import directBFRErrors.ErrorSampler;
import directBFRErrors.PositionContext;

import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

//This class corresponds to every observed position
public class ThreadPoolLauncher
{
	BufferedReader m_inputData;
	int m_numThreads, m_runNum;

	double[] m_observedInspectorBFR, m_observedInspectorOrient;
	ErrorSampler m_newSampler;
	
	public ThreadPoolLauncher(BufferedReader inputData, int numThreads, int runs) throws IOException
	{
		m_numThreads=numThreads;
		m_inputData=inputData;
		m_runNum=runs;
		launchThreads();
	}
	
	public void launchThreads() throws IOException
	{
		ExecutorService threadExecutors = Executors.newFixedThreadPool(m_numThreads);		
		String line;
		int truePosNumObs=0, orientSeedObs=600, truePosNumSim=1200, orientSeedSim=1800;
		double[] trueInspectorPos, trueInspectorOrient, 	
		
		while((line=m_inputData.readLine())!=null) 
		{
			//Two error samplers, one for observed positions and one for simulated positions (so that number of obs and sim positions may vary)
			ErrorSampler obsSampler = new ErrorSampler(ConfigErrorAnalysis.getErrorRange(), ConfigErrorAnalysis.getErrorDiscreteSteps(), ConfigErrorAnalysis.getErrorLambda(), ConfigErrorAnalysis.getErrorBeta(), ConfigErrorAnalysis.getNumObsPos(), truePosNumObs, orientSeedObs);
			ErrorSampler simSampler = new ErrorSampler(ConfigErrorAnalysis.getErrorRange(), ConfigErrorAnalysis.getErrorDiscreteSteps(), ConfigErrorAnalysis.getErrorLambda(), ConfigErrorAnalysis.getErrorBeta(), ConfigErrorAnalysis.getNumSimPos(), truePosNumSim, orientSeedSim);
			
			//Pass on the line as a string to the object	
			threadExecutors.submit(new PositionContext(line, m_runNum, truePosNumObs, obsSampler, simSampler));
			truePosNumObs++; orientSeedObs++; truePosNumSim++; orientSeedSim++;
		}
		
		threadExecutors.shutdown();
		
	}	
	
	
	/*private void launcher()
	{
		for(int sampleNum=0;sampleNum<100;sampleNum++)
		{	
			Simulator simObj=new Simulator(m_observedInspectorBFR, m_observedInspectorOrient, m_newSampler, sampleNum);
			Thread threadObj=new Thread(simObj);	
			threadObj.start();	
		}
	}*/
	
}
