package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Hashtable; 

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import approximateApproach.ComputeBoundingBox;
import approximateApproach.ComputeBoundingBoxInterface;
import approximateApproach.ApproximateAlgorithm;
import approximateApproach.Nodes;
import directBFRErrors.ErrorSampler;
import directBFRErrors.PositionContext;

import java.io.IOException;
import java.util.Random;

import Jama.Matrix;
import WeibullCDF.CDF_Weibull2;

//This class corresponds to every observed position
public class OptimizedThreadLauncher
{
	BufferedReader m_inputData;
	int m_numThreads, m_runNum;

	double m_npdDistance,m_fpdDistance,m_viewAngle, m_errorRange, m_maxAngleError;	
	double[] m_trueInspectorPos, m_trueInspectorOrient;
	String m_trailLine;

	Hashtable<Integer, List<Nodes> > m_nodesList;

	public OptimizedThreadLauncher(BufferedReader inputData, int numThreads, int runs) throws IOException
	{
		m_numThreads=numThreads;
		m_inputData=inputData;
		m_runNum=runs;
		m_npdDistance=ConfigErrorAnalysis.getNpdFpd() [0];
                m_fpdDistance=ConfigErrorAnalysis.getNpdFpd() [1];
                m_viewAngle = ConfigErrorAnalysis.getViewAngle();
		m_maxAngleError = ConfigErrorAnalysis.getMaxAngleError();
		launchThreads();
	}
	
	public void launchThreads() throws IOException
	{
		ExecutorService threadExecutors = Executors.newFixedThreadPool(m_numThreads);		
		int truePosNumObs=0, orientSeedObs=600, truePosNumSim=1200, orientSeedSim=1800;
		
	
		while((m_trailLine=m_inputData.readLine())!=null) 
		{
			//Two error samplers, one for observed positions and one for simulated positions (so that number of obs and sim positions may vary)
			ErrorSampler obsSampler = new ErrorSampler(ConfigErrorAnalysis.getErrorRange(), ConfigErrorAnalysis.getErrorDiscreteSteps(), ConfigErrorAnalysis.getErrorLambda(), ConfigErrorAnalysis.getErrorBeta(), ConfigErrorAnalysis.getNumObsPos(), truePosNumObs, orientSeedObs, m_maxAngleError);
			ErrorSampler simSampler = new ErrorSampler(ConfigErrorAnalysis.getErrorRange(), ConfigErrorAnalysis.getErrorDiscreteSteps(), ConfigErrorAnalysis.getErrorLambda(), ConfigErrorAnalysis.getErrorBeta(), ConfigErrorAnalysis.getNumSimPos(), truePosNumSim, orientSeedSim, m_maxAngleError);
				
			//getNodesInEnclosingCone();
			
			//Extract inspector position/orientation and pass it to PositionContext
			String[] splitLine=m_trailLine.split("\\s+");
	                m_trueInspectorPos = new double[] {Double.parseDouble(splitLine[1]), Double.parseDouble(splitLine[2]), Double.parseDouble(splitLine[3])};
        	        m_trueInspectorOrient = new double[] {Double.parseDouble(splitLine[4]), Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6])};
		
			threadExecutors.submit(new PositionContext(m_trueInspectorPos, m_trueInspectorOrient, m_runNum, truePosNumObs, obsSampler, simSampler));
			truePosNumObs++; orientSeedObs++; truePosNumSim++; orientSeedSim++;
		}
		threadExecutors.shutdown();
	}	
	
	//Extract inspector position/orientation and also the near and far plane points for the true inspector position
	private void getNodesInEnclosingCone() throws IOException
	{
		List<Nodes> nodesInEnclosingCone;
		String[] splitLine=m_trailLine.split("\\s+");
                m_trueInspectorPos = new double[] {Double.parseDouble(splitLine[1]), Double.parseDouble(splitLine[2]), Double.parseDouble(splitLine[3])};
                m_trueInspectorOrient = new double[] {Double.parseDouble(splitLine[4]), Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6])};
		
		//Compute the near and far plane centers for the true inspector position`
		NearAndFarPlanePoints inspectorParameters=new NearAndFarPlanePoints(m_trueInspectorPos,m_trueInspectorOrient);
		double[] npdInBFR = inspectorParameters.getPlanePointInBFR(m_npdDistance);
		double[] fpdInBFR = inspectorParameters.getPlanePointInBFR(m_fpdDistance);
			
		if(m_trueInspectorPos[2] > ConfigErrorAnalysis.getDeckZPosition())	
			m_errorRange = ConfigErrorAnalysis.getErrorRange();
		else 
			m_errorRange = ConfigErrorAnalysis.getErrorRangeBelowBridge();

		EnclosingCone largeCone=new EnclosingCone(m_trueInspectorPos, npdInBFR, fpdInBFR, m_viewAngle, m_errorRange, m_maxAngleError);	
		m_nodesList = largeCone.getNodesLargeCone();
	}
}
