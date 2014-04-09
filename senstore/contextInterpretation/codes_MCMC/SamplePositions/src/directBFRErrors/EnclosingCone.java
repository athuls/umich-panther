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
import approximateApproach.OptimizedContainment;
import directBFRErrors.ErrorSampler;
import directBFRErrors.PositionContext;

import java.io.IOException;
import java.util.Random;

import Jama.Matrix;
import WeibullCDF.CDF_Weibull2;

//This class corresponds to every observed position
public class EnclosingCone
{
	private double[] m_trueInspectorPos, m_npdPoint, m_fpdPoint;
	private double m_viewAngle, m_range, m_angleError;
	Matrix m_MI, m_MnpdPoint, m_MfpdPoint, m_trueInspPlaneNormal;

	public EnclosingCone(double[] trueInspectorPos, double[] npdPoint, double[] fpdPoint, double viewAngle, double errorRange, double angleError) throws IOException
	{
		m_trueInspectorPos = trueInspectorPos;
		m_npdPoint = npdPoint;
		m_fpdPoint = fpdPoint;	
		m_viewAngle = viewAngle;
		m_range = 2*errorRange;
		m_angleError = 2*angleError;
	}
	
	//From the true inspector position, NPD and FPD points, obtain the enclosing inspector position, enclosing FPD and NPD points	
	public Hashtable<Integer, List<Nodes> > getNodesLargeCone()
	{
		m_MI=new Matrix(new double[][] {m_trueInspectorPos});
                m_MnpdPoint=new Matrix(new double[][] {m_npdPoint});
                m_MfpdPoint=new Matrix(new double[][] {m_fpdPoint});
		
		double npdDist = (m_MnpdPoint.minus(m_MI)).normF();
		double fpdDist = (m_MfpdPoint.minus(m_MI)).normF();
		m_trueInspPlaneNormal = (m_MfpdPoint.minus(m_MI)).times((double)((double)1/(double)fpdDist));		

		//Multiply the plane normal of true inspector position by -1 to obtain the reverse matrix	
		Matrix m_trueNormalReverse = m_trueInspPlaneNormal.times(-1);	
		
		double enclosingViewAngle = m_viewAngle + m_angleError;	
		double cosecViewAngle = (double)((double)1/(double)Math.sin(enclosingViewAngle));
		double distTrueEnclosingPos = (m_range*cosecViewAngle);
		Matrix m_enclosingInspPos = m_MI.plus(m_trueNormalReverse.times(distTrueEnclosingPos));

		//Temporary distance variable to compute the near and far plane points
		double tempDist = distTrueEnclosingPos + m_range + fpdDist;
		Matrix enclosingInspFPD = m_enclosingInspPos.plus(m_trueInspPlaneNormal.times(tempDist));
	
		tempDist = (distTrueEnclosingPos - m_range) + npdDist;	
		Matrix enclosingInspNPD = m_enclosingInspPos.plus(m_trueInspPlaneNormal.times(tempDist));
		double enclosingFpdDist = (enclosingInspFPD.minus(m_enclosingInspPos)).normF();
		Matrix enclosingInspPlaneNormal = (enclosingInspFPD.minus(m_enclosingInspPos)).times((double)((double)1/(double)enclosingFpdDist));
	
		OptimizedContainment containmentObj = new OptimizedContainment(m_enclosingInspPos, enclosingInspNPD, enclosingInspFPD, enclosingInspPlaneNormal, m_MI.get(0,2), enclosingViewAngle);
		return containmentObj.getContainedNodes();
				 	
	}		
}
