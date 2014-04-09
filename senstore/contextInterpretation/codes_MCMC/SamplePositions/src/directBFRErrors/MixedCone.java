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
import Jama.Matrix;

import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

public class MixedCone 
{
	private Matrix[] m_inspectorPositions, m_fpdPoints;	
	private Matrix m_optimumInspectorPos, m_optimumFpdPoint;
	private double m_npdDistance;
	
	public MixedCone(Matrix[] inspectorPositions, Matrix[] fpdPoints, double npdDist) throws IOException
	{
		m_inspectorPositions = inspectorPositions;
		m_fpdPoints = fpdPoints;
		m_npdDistance = npdDist;
		System.out.println("size of inspector matrix array is "+inspectorPositions.length);
	}
	
	private Matrix getNpdPoint()
	{
		//Compute the NPD point from optimum inspector position and FPD point for the inspector
		double farPointDist = m_optimumInspectorPos.minus(m_optimumFpdPoint).normF();
		Matrix npdPoint = (m_optimumFpdPoint.minus(m_optimumInspectorPos).times((double)((double)1/(double)farPointDist)));	
		npdPoint = m_optimumInspectorPos.plus(npdPoint.times(m_npdDistance));
		return npdPoint;
	}
	
	public Matrix getMeanValue(int points)
	{	
		Matrix sumPoint = new Matrix(1,3);
		int numPoints = m_inspectorPositions.length;
		for(int i=0; i < numPoints; i++){
			switch(points){
				case 0:
					sumPoint = sumPoint.plus(m_inspectorPositions[i].times((double)((double)1/(double)numPoints)));
					m_optimumInspectorPos = sumPoint;
					break;
				case 1:
					//As soon as npd point is asked, just call the method to compute it and return the Matrix obtained
					sumPoint = getNpdPoint();
					return sumPoint;
				case 2:
					sumPoint = sumPoint.plus(m_fpdPoints[i].times((double)((double)1/(double)numPoints)));
					m_optimumFpdPoint = sumPoint;
					break;
				default:
					System.out.println("Wrong choice for computing centroid");
					break;
			}
		}	
	
		return sumPoint;
	}	
	
}
