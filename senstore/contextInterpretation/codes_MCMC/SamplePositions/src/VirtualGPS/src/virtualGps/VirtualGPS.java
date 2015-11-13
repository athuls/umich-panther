package virtualGps;
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/

import java.io.IOException;

import Jama.Matrix;

import java.util.*;
//For file reading
import java.io.BufferedReader;
import java.io.FileReader;

public class VirtualGPS
{
	//4 points in GPS and Bridge frame of reference
	double[] gps_p1, gps_p2, gps_p3, gps_p4, bfr_p1, bfr_p2, bfr_p3, bfr_p4; 
	Matrix a,c;	//Matrices 'a' and 'c' denote the points in bridge and GPS frames respectively	
	//For GPS to Bridge transform-> Bcap=[BG] Gcap & Inspector frame to GPS transform-> Icap=[IG] Gcap
	Matrix BG,IG;  
	//1/|Gx|, 1/|Gy|, 1/|Gz| i.e. scaling factors at the location of bridge
	double[] gpsScalingFactors;
	/*bridge_origin->GPS, inspector_origin->GPS, convert_query->IFR point to convert to BFR; 
	* F1 & F2->points representing near and far plane distance for inspector*/
	double[] lfr_origin;
	double feetToMilesConversionFactor = (double)1/(double)1609.34;
		
	public VirtualGPS(double[] pt1, double[] pt2, double[] pt3, double[] pt4)
	{
		gps_p1=pt1;
		gps_p2=pt2;
		gps_p3=pt3;
		gps_p4=pt4;
		bfr_p1 = new double[3];
		bfr_p2 = new double[3];
		bfr_p3 = new double[3];
		bfr_p4 = new double[3];
		
		// Calibration: Define corresponding linear frame of reference
		Arrays.fill(bfr_p1, 0);
		bfr_p2=Calibrator.computeIO_BFR(gps_p1, gps_p2);
		bfr_p3=Calibrator.computeIO_BFR(gps_p1, gps_p3);
		bfr_p4=Calibrator.computeIO_BFR(gps_p1, gps_p4);
		
		//creating 'a' and 'c' matrices3
		a = new Matrix(new double[][] {bfr_p1,bfr_p2,bfr_p3,bfr_p4});
		c = new Matrix(new double[][] {gps_p1, gps_p2, gps_p3, gps_p4});
		
		// Given a set of 4 points on a bridge
		BG = computeBGMatrix();	//Compute the BG Matrix
		gpsScalingFactors = computeScalingFactors(); //Compute the scaling factors for the bridge
		lfr_origin=computeLfrorigin(); //compute the bridge origin in GPS
	}
	
	Matrix computeBGMatrix()
	{
		/*Each row of Matrix a (3X3) is the set of 3 coordinates of a point in Bridge system
		 * Each row of Matrix c (3X3) is the set of 3 coordinates of a point in GPS*/
		/*Matrix (a[1]-a[2]) [BG]=c[1]-c[2]
		 * 3 pairs of such equations to solve for BG*/
		
		Matrix Bridge=new Matrix(3,3);
		Matrix GPS=new Matrix(3,3);
		for(int i=0;i<3;i++)
		{
			Bridge.setMatrix(i,i,0,2,a.getMatrix(0,0,0,2).minus(a.getMatrix(i+1,i+1,0,2)));
			GPS.setMatrix(i,i,0,2,c.getMatrix(0,0,0,2).minus(c.getMatrix(i+1,i+1,0,2)));
		}
		return (Bridge.solve(GPS));
	}
	
	public Matrix getBGMatrix()
	//Returns BG matrix
	{
		return (computeBGMatrix());
	}
	
	protected double[] computeScalingFactors()
	{
		double[] scale;
		scale=new double[3];
		Matrix invBG=BG.inverse();
		for(int i=0;i<3;i++)
		{
			scale[i]=invBG.getMatrix(i,i,0,2).normF();			
		}
		return scale;
	}
	
	double[] computeLfrorigin()
	{
		/*[BOx,BOy,BOz]+([a1 a2 a3]*[BG]=[c1 c2 c3])*/
		double[] origin=c.getMatrix(1,1,0,2).minus(a.getMatrix(1,1,0,2).times(BG)).getArray()[0];
		return origin;
	}
	
	public double[] getBridgeOrigin()
	{
		return lfr_origin;
	}
	
	double getEuclideanDistance(double[] point1, double[] point2)
	{
		double distance = 0;
		for(int i = 0; i < 3; i++)
		{
			distance += Math.pow((point2[i] - point1[i]), 2);
		}
		
		distance = Math.sqrt(distance);
		return distance;
	}
	
	public static void main(String args[]) throws IOException
	{
		//Compute Bridge Parameters
		VirtualGPS sel=new VirtualGPS(Config_Beijing.getGPSPoint1(), Config_Beijing.getGPSPoint2(), Config_Beijing.getGPSPoint3(), Config_Beijing.getGPSPoint4());
		
		// Compute direction of user motion (this comes as input)
		// double[] gpsSample1 = new double[3], gpsSample2 = new double[3];
		// Arrays.fill(gpsSample1, 0);
		// Arrays.fill(gpsSample2, 0);
		double[] gpsSample1 = {116.2607755,39.78757457,55.8};
		double[] gpsSample2 = {116.277328,39.835829,55.2};
		double[] finalGpsSample = {116.29390396,39.88408063,54.6};
		
		// 39.87983938,116.47704638,43 
		// 
		// 116.56116562, 40.74543747, 517.3
		// 
		// 116.45729538, 39.84762516, 42.5
		
		// Project the GPS samples to linear frame of reference
		InspectorParametersInterface positionParameters = new InspectorParameters(sel.BG, sel.gpsScalingFactors, sel.lfr_origin);
		positionParameters.computeBoundingBoxParameters(gpsSample1);
		double[] userLfrPos1 = positionParameters.getInspectorBFR();
		positionParameters.computeBoundingBoxParameters(gpsSample2);
		double[] userLfrPos2 = positionParameters.getInspectorBFR();
		double distance = Calibrator.getDistanceBtwPoints(gpsSample2, finalGpsSample);
		
		// Compute unit vector in direction of user
		Matrix gpsDirSample1 = new Matrix(new double[][] {userLfrPos1});
		Matrix gpsDirSample2 = new Matrix(new double[][] {userLfrPos2});
		Matrix gpsDirDiff = gpsDirSample2.minus(gpsDirSample1);
		double gpsDirDiffMagnitude = gpsDirDiff.normF();
		if(gpsDirDiffMagnitude != 0)
		{
			Matrix userDir = gpsDirDiff.times((double)1 / gpsDirDiffMagnitude);
		
		
			// Initial GPS point (take the latest GPS point so as to minimize accumulated dead-reckon error)
			Matrix initialGpsPoint = new Matrix(new double[][] { userLfrPos2 });
		
			// 116.42223, 39.861681, 43
			// Dummy dead reckoning distance value in meters 
			// Matrix distanceMatrix = new Matrix(new double[][]{sel.gpsScalingFactors}).times(deadReckonDist);
			// Matrix distVector = distanceMatrix.transpose().times(userDir);
			 Matrix newGpsPoint = initialGpsPoint.plus(userDir.times(distance));
			// Matrix newGpsPoint = initialGpsPoint.plus(distVector.transpose());
			
			// Convert newGpsPoint to GPS coordinates
			double[] finalGpsCoord = positionParameters.getCorrespondingGPS(newGpsPoint);
			double gpsErrorDist = Calibrator.getDistanceBtwPoints(finalGpsCoord, finalGpsSample);
					
			for(int i = 0; i < 3; i++)
			{
				System.out.println("Coordinate is " + newGpsPoint.get(i,i));
			}
		}
	}			
}