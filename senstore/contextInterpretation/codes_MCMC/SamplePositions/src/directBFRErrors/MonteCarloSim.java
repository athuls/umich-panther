package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import approximateApproach.ComputeBoundingBox;
import approximateApproach.ComputeBoundingBoxInterface;
import approximateApproach.ApproximateAlgorithm;
import approximateApproach.Nodes;
import approximateApproach.OptimizedContainment;
import directBFRErrors.ErrorSampler;
import java.util.List;
import java.util.Hashtable;
import java.util.Collections;

import Jama.Matrix;
import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

public class MonteCarloSim 
{
	Hashtable<Integer, List<Nodes> > m_nodesList;
	double[] m_observedInspectorPos, m_observedInspectorOrient;
	ErrorSampler m_simSampler;
	int m_simSampleNum, m_observedPositionNum, m_truePositionNum, m_runNum;
	double m_viewAngle, m_npdDist, m_fpdDist;
		
	public MonteCarloSim(Hashtable<Integer, List<Nodes> > nodesList, double[] observedInspectorPos, double[] observedInspectorOrient, int runs, int truePositionNum, int observedPosNum, int simSampleNum, double npdDist, double fpdDist, double viewAngle, ErrorSampler simSampler) throws IOException
	{
		m_nodesList = nodesList;
		m_observedInspectorPos = observedInspectorPos;
		m_observedInspectorOrient=observedInspectorOrient;
		m_observedPositionNum=observedPosNum;	
		m_simSampleNum=simSampleNum;
		m_truePositionNum=truePositionNum;
		m_viewAngle=viewAngle;
		m_npdDist=npdDist;
		m_fpdDist=fpdDist;
		m_simSampler=simSampler;
		m_runNum=runs;
		simulatePositions();
	}
	
	//For mixed cone approach
	public MonteCarloSim(double[] observedInspectorPos, double[] observedInspectorOrient, int runs, int truePositionNum, int observedPosNum, int simSampleNum, double npdDist, double fpdDist, double viewAngle, ErrorSampler simSampler) throws IOException
	{
		m_observedInspectorPos = observedInspectorPos;
		m_observedInspectorOrient=observedInspectorOrient;
		m_observedPositionNum=observedPosNum;	
		m_simSampleNum=simSampleNum;
		m_truePositionNum=truePositionNum;
		m_viewAngle=viewAngle;
		m_npdDist=npdDist;
		m_fpdDist=fpdDist;
		m_simSampler=simSampler;
		m_runNum=runs;
		simulatePositions();
	}

	//For the given observed position/orientation of the inspector, generate simulated positions and get contextual element lists for all simulated points
	public void simulatePositions() throws IOException
	{
		int samples=0;
		double startTime,endTime;

		
		Matrix[] inspectorPositions = new Matrix[m_simSampleNum];
		Matrix[] fpdPoints = new Matrix[m_simSampleNum];
		
		while(samples<m_simSampleNum)
		{
			double[] errorInspectorPos={0,0,0};
			double[] errorInspectorOrient={0,0,0};	
			double[] m_sampledInspectorPos={0,0,0}, m_sampledInspectorOrient={0,0,0};
			
			//For picking 3 (or whatever number of samples for Mixed cone approach) samples, we pick random number from 1 to 100 and pass that as value, but num_calls is 100		
			int randomNumber = m_simSampler.generateRandomInt(0,100);
			errorInspectorPos=m_simSampler.errorInspectorPosition(randomNumber, m_observedInspectorPos[2]);
			errorInspectorOrient=m_simSampler.errorInspectorOrient();
				
			for(int i=0;i<3;i++){
				m_sampledInspectorPos[i]=m_observedInspectorPos[i] + errorInspectorPos[i];
				m_sampledInspectorOrient[i]=m_observedInspectorOrient[i] + errorInspectorOrient[i];	
			}
			
			//Get Near and Far Plane centers using position and orientation of inspector
                	NearAndFarPlanePoints inspectorParameters=new NearAndFarPlanePoints(m_sampledInspectorPos, m_sampledInspectorOrient);
                	double[] fpdInBFR=inspectorParameters.getPlanePointInBFR(m_fpdDist);
			
			inspectorPositions[samples] = new Matrix(new double[][] {m_sampledInspectorPos});
			fpdPoints[samples] = new Matrix(new double[][]{fpdInBFR});
				
			samples++;
		}
		
		//Object to determine the mixed/optimum cone for the given cones of observed positions	
		MixedCone meanCone = new MixedCone(inspectorPositions, fpdPoints, m_npdDist);	
		//File handling and bounding box computation section 
		try{
                	OptimizedContainment boundingBox=new OptimizedContainment(meanCone.getMeanValue(0), meanCone.getMeanValue(2), meanCone.getMeanValue(1), m_viewAngle, m_runNum, m_truePositionNum, m_observedPositionNum);
	                boundingBox.getContainedNodesInMeanCone();
		}
		catch(Exception e){
			System.out.println("Exception occurred");
			System.exit(1);
		}
	}
}
