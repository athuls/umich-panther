package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
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
		
	/*public ThreadPoolLauncher(double[] observedInspectorBFR, double[] observedInspectorOrient, bool samePosition) throws IOException
	{
		m_observedInspectorBFR = observedInspectorBFR;
		m_observedInspectorOrient=observedInspectorOrient;
		
		//For every observed position, initialize the error sampler with the same pair of seeds, so that every observed sample for a true position, 
		//would be using the same set of random angles for determining position orientation
		m_newSampler=new ErrorSampler(ConfigErrorAnalysis.getErrorRange(), ConfigErrorAnalysis.getErrorDiscreteStep(), ConfigErrorAnalysis.getErrorLambda(), ConfigErrorAnalysis.getErrorBeta(), ConfigErrorAnalysis.getErrorTotalSamples());

		//Create 100 threads
		//Pass the thread number to each thread
		//SO basically each created thread should start at run
		//It will generate an error value and add to observed
		launcher();	
	}*/

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
		int truePosNum=0, orientSeed=600;
		while((line=m_inputData.readLine())!=null) 
		{
			orientSeed++;
			ErrorSampler newSampler = new ErrorSampler(ConfigErrorAnalysis.getErrorRange(), ConfigErrorAnalysis.getErrorDiscreteSteps(), ConfigErrorAnalysis.getErrorLambda(), ConfigErrorAnalysis.getErrorBeta(), ConfigErrorAnalysis.getErrorTotalSamples(), truePosNum, orientSeed);
			//Pass on the line as a string to the object	
			threadExecutors.submit(new PositionContext(line, m_runNum, truePosNum, newSampler));
			truePosNum++;
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
