package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import approximateApproach.ComputeBoundingBox;
import approximateApproach.ComputeBoundingBoxInterface;
import approximateApproach.ApproximateAlgorithm;
import directBFRErrors.ErrorSampler;

import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

public class MonteCarloSim 
{
	double[] m_observedInspectorPos, m_observedInspectorOrient;
	ErrorSampler m_newSampler;
	int m_simSampleNum, m_observedPositionNum, m_truePositionNum, m_runNum;
	double m_viewAngle, m_npdDist, m_fpdDist;
		
	public MonteCarloSim(double[] observedInspectorPos, double[] observedInspectorOrient, int runs, int truePositionNum, int observedPosNum, int simSampleNum, double npdDist, double fpdDist, double viewAngle, ErrorSampler newSampler) throws IOException
	{
		m_observedInspectorPos = observedInspectorPos;
		m_observedInspectorOrient=observedInspectorOrient;
		m_observedPositionNum=observedPosNum;	
		m_simSampleNum=simSampleNum;
		m_truePositionNum=truePositionNum;
		m_viewAngle=viewAngle;
		m_npdDist=npdDist;
		m_fpdDist=fpdDist;
		m_newSampler=newSampler;
		m_runNum=runs;
		simulatePositions();
	}
	
	//For the given observed position/orientation of the inspector, generate simulated positions and get contextual element lists for all simulated points
	public void simulatePositions()
	{
		int samples=0;
		double[] m_sampledInspectorPos={0,0,0}, m_sampledInspectorOrient={0,0,0};

		while(samples<m_simSampleNum)
		{
			double[] errorInspectorPos={0,0,0};
			double[] errorInspectorOrient={0,0,0};	
	
			errorInspectorPos=m_newSampler.errorInspectorPosition(samples, m_observedInspectorPos[2]);
			errorInspectorOrient=m_newSampler.errorInspectorOrient();
				
			for(int i=0;i<3;i++){
				m_sampledInspectorPos[i]=m_observedInspectorPos[i] + errorInspectorPos[i];
				m_sampledInspectorOrient[i]=m_observedInspectorOrient[i]+errorInspectorOrient[i];	
			}
			
			//Get Near and Far Plane centers using position and orientation of inspector
                	NearAndFarPlanePoints inspectorParameters=new NearAndFarPlanePoints(m_sampledInspectorPos, m_sampledInspectorOrient);
                	double[] npdInBFR=inspectorParameters.getPlanePointInBFR(m_npdDist);
                	double[] fpdInBFR=inspectorParameters.getPlanePointInBFR(m_fpdDist);

                	//File handling and bounding box computation section 
                	ComputeBoundingBoxInterface boundingBox=new ComputeBoundingBox(m_sampledInspectorPos, npdInBFR, fpdInBFR, m_viewAngle, m_truePositionNum, m_observedPositionNum, samples, m_runNum);
                	boundingBox.captureNodesInBox();
			samples++;
		}
	}
		
}
