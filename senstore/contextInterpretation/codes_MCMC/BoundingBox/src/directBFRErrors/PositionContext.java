package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import directBFRErrors.MonteCarloSim;

import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

public class PositionContext implements Runnable	
{
	double[] m_trueInspectorPos, m_trueInspectorOrient, m_observedInspectorPos, m_observedInspectorOrient;
	int m_numObservedSamples, m_numSimulatedSamples, m_truePositionNum, m_runNum;
	ErrorSampler m_newSampler;
		
	public PositionContext(String line, int run, int position, ErrorSampler sampler)
	{
		//Get the inspectors position and orientation
		String[] splitLine=line.split("\\s+");
		m_trueInspectorPos = new double[] {Double.parseDouble(splitLine[1]), Double.parseDouble(splitLine[2]), Double.parseDouble(splitLine[3])};
		m_trueInspectorOrient = new double[] {Double.parseDouble(splitLine[4]), Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6])};
		m_truePositionNum=position;
		m_newSampler=sampler;
		m_runNum=run;
		//Get the number of samples for observed and simulated positions
		//Read these values here only so that each thread has to obtain these values only once
		m_numObservedSamples = ConfigErrorAnalysis.getNumObsPos();
		m_numSimulatedSamples = ConfigErrorAnalysis.getNumSimPos();
	}
	
	@Overridden
	public void run() throws IOException
	{
		startProcessing();
	}

	//For the given observed position/orientation of the inspector, generate simulated positions and get contextual element lists for all simulated points
	private void startProcessing() throws IOException
	{
		int countObserved=0;
		double npdDistance=ConfigErrorAnalysis.getNpdFpd() [0];
		double fpdDistance=ConfigErrorAnalysis.getNpdFpd() [1];
		double viewAngle = ConfigErrorAnalysis.getViewAngle();
		
		System.out.println("Position "+m_truePositionNum+" being processed.");
			
		while(countObserved < m_numObservedSamples)
		{
			double[] errorInspectorPos={0,0,0};
			double[] errorInspectorOrient={0,0,0};	
	
			errorInspectorPos=m_newSampler.errorInspectorPosition(countObserved, m_trueInspectorPos[2]);
			errorInspectorOrient=m_newSampler.errorInspectorOrient();
				
			for(int i=0;i<3;i++){
				m_observedInspectorPos[i]=m_trueInspectorPos[i] + errorInspectorPos[i];
				m_observedInspectorOrient[i]=m_trueInspectorOrient[i]+errorInspectorOrient[i];	
			}
			
			MonteCarloSim simObject=new MonteCarloSim(m_observedInspectorPos, m_observedInspectorOrient, m_runNum, m_truePositionNum, countObserved, m_numSimulatedSamples, npdDistance, fpdDistance, viewAngle, m_newSampler);		
		
/*			//Get Near and Far Plane centers using position and orientation of inspector
                	NearAndFarPlanePoints inspectorParameters=new NearAndFarPlanePoints(m_observedInspectorPos, m_observedInspectorOrient);
                	double[] npdInBFR=inspectorParameters.getPlanePointInBFR(npdDistance);
                	double[] fpdInBFR=inspectorParameters.getPlanePointInBFR(fpdDistance);

                	//File handling and bounding box computation section 
                	ComputeBoundingBoxInterface boundingBox=new ComputeBoundingBox(observedInspectorBFR, npdInBFR, fpdInBFR, ConfigErrorAnalysis.getViewAngle(), countError, position, runs);
                	boundingBox.captureNodesInBox();
*/
			countObserved++;
		}	
	}
		
}
