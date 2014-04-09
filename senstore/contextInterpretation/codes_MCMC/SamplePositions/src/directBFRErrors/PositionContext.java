package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import directBFRErrors.MonteCarloSim;
import approximateApproach.Nodes;

import java.io.IOException;
import java.util.Random;
import java.util.List;
import java.util.Hashtable;

import WeibullCDF.CDF_Weibull2;

public class PositionContext implements Runnable	
{
	Hashtable<Integer, List<Nodes> > m_nodesList; 
	double[] m_trueInspectorPos, m_trueInspectorOrient;
	int m_numObservedSamples, m_numSimulatedSamples, m_truePositionNum, m_runNum;
	ErrorSampler m_obsSampler, m_simSampler;
		
	public PositionContext(Hashtable<Integer, List<Nodes> > nodeList, double[] trueInspectorPos, double[] trueInspectorOrient, int run, int position, ErrorSampler obsSampler, ErrorSampler simSampler)
	{
		//Get the inspectors position and orientation
		m_trueInspectorPos = trueInspectorPos;
		m_trueInspectorOrient = trueInspectorOrient; 
		m_nodesList = nodeList;
		m_truePositionNum=position;
		m_obsSampler=obsSampler;
		m_simSampler=simSampler;
		m_runNum=run;
		//Get the number of samples for observed and simulated positions
		//Read these values here only so that each thread has to obtain these values only once
		m_numObservedSamples = ConfigErrorAnalysis.getNumObsPos();
		m_numSimulatedSamples = ConfigErrorAnalysis.getNumSimPos();
	}
	
	public PositionContext(double[] trueInspectorPos, double[] trueInspectorOrient, int run, int position, ErrorSampler obsSampler, ErrorSampler simSampler)
	{
		//Get the inspectors position and orientation
		m_trueInspectorPos = trueInspectorPos;
		m_trueInspectorOrient = trueInspectorOrient; 
		m_truePositionNum=position;
		m_obsSampler=obsSampler;
		m_simSampler=simSampler;
		m_runNum=run;
		//Get the number of samples for observed and simulated positions
		//Read these values here only so that each thread has to obtain these values only once
		m_numObservedSamples = ConfigErrorAnalysis.getNumObsPos();
		m_numSimulatedSamples = ConfigErrorAnalysis.getNumSimPos();
	}

	@Override
	public void run()
	{
		try {
			startProcessing();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//For the given observed position/orientation of the inspector, generate simulated positions and get contextual element lists for all simulated points
	private void startProcessing() throws IOException
	{
		int countObserved=0;
		double npdDistance=ConfigErrorAnalysis.getNpdFpd() [0];
		double fpdDistance=ConfigErrorAnalysis.getNpdFpd() [1];
		double viewAngle = ConfigErrorAnalysis.getViewAngle();
		
		System.out.println("position  "+m_truePositionNum);	
	
		while(countObserved < m_numObservedSamples)
		{
			
			//For mixed cone, we would not need to create an observed position separately. Just pass all of it to the MonteCarloSim to generate multiple observed positions
			/*double[] errorInspectorPos={0,0,0};
			double[] errorInspectorOrient={0,0,0};	
			double[] observedInspectorPos = {0,0,0};
			double[] observedInspectorOrient = {0,0,0};
	
			errorInspectorPos=m_obsSampler.errorInspectorPosition(countObserved, m_trueInspectorPos[2]);
			errorInspectorOrient=m_obsSampler.errorInspectorOrient();
				
			for(int i=0;i<3;i++){
				observedInspectorPos[i]=m_trueInspectorPos[i] + errorInspectorPos[i];
				observedInspectorOrient[i]=m_trueInspectorOrient[i]+errorInspectorOrient[i];	
			}
			MonteCarloSim simObject=new MonteCarloSim(m_nodesList, observedInspectorPos, observedInspectorOrient, m_runNum, m_truePositionNum, countObserved, m_numSimulatedSamples, npdDistance, fpdDistance, viewAngle, m_simSampler);		*/
			MonteCarloSim simObject=new MonteCarloSim(m_trueInspectorPos, m_trueInspectorOrient, m_runNum, m_truePositionNum, countObserved, m_numSimulatedSamples, npdDistance, fpdDistance, viewAngle, m_obsSampler);		
		
			countObserved++;
		}	
	}
}
