package approximateApproach;
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/

import java.io.IOException;

import Jama.Matrix;

import java.util.*;
//For file reading
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

public class ApproximateAlgorithm
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
	double[] bridge_origin;
	
	public ApproximateAlgorithm(double[] pt1, double[] pt2, double[] pt3, double[] pt4, double[] pt5, double[] pt6, double[] pt7, double[] pt8)
	{
		gps_p1=pt1;
		gps_p2=pt2;
		gps_p3=pt3;
		gps_p4=pt4;
		bfr_p1=pt5;
		bfr_p2=pt6;
		bfr_p3=pt7;
		bfr_p4=pt8;
		
		//creating 'a' and 'c' matrices
		
		a=new Matrix(new double[][] {bfr_p1,bfr_p2,bfr_p3,bfr_p4});
		c=new Matrix(new double[][] {gps_p1, gps_p2, gps_p3, gps_p4});
		
		// Given a set of 4 points on a bridge
		BG = computeBGMatrix();	//Compute the BG Matrix
		IG=Matrix.identity(3,3);//Initialize the IG matrix to be undefined in the beginning
		IG.set(0, 0, 0.0d/0.0);
		IG.set(1, 1, 0.0d/0.0);
		IG.set(2, 2, 0.0d/0.0);
		gpsScalingFactors = computeScalingFactors(); //Compute the scaling factors for the bridge
		bridge_origin=computeBridgeorigin(); //compute the bridge origin in GPS
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
		//Gcap=[inverse(BG)]*Bcap
		/*Gxcap=magnitude of Gcap[1]
		 * Gycap=magnitude of Gcap[2]
		 * Gzcap=magnitude of Gcap[3]*/
		double[] scale;
		scale=new double[3];
		Matrix invBG=BG.inverse();
		for(int i=0;i<3;i++)
		{
			scale[i]=invBG.getMatrix(i,i,0,2).normF();			
		}
		return scale;
	}
	
	double[] computeBridgeorigin()
	{
		/*[BOx,BOy,BOz]+([a1 a2 a3]*[BG]=[c1 c2 c3])*/
		double[] origin=c.getMatrix(1,1,0,2).minus(a.getMatrix(1,1,0,2).times(BG)).getArray()[0];
		return origin;
	}
	
	public double[] getBridgeOrigin()
	{
		return bridge_origin;
	}

	private double getEuclideanDistance(double[] pointCoordinates)
	{
		double distance = 0;
		double[] bridgeOrigin = this.getBridgeOrigin();
		for(int i =0; i < 3; i++)
		{
			distance += Math.pow(pointCoordinates[i],2);
		}
	
		// Converting from feet to miles	
		return (double)(Math.sqrt(distance) * 0.3)/1609.34;
		//return (double)(Math.sqrt(distance) * 0.3); 
	}
	
	public static void main(String args[]) throws IOException
	{
		//Calibrate BFR by computing BFR reference coordinates
		double[] BFRPoint1 = Calibrator.computeIO_BFR(Config.OriginalGPSPoint, Config.getGPSPoint1(), Config.bearing1);
		double[] BFRPoint2 = Calibrator.computeIO_BFR(Config.OriginalGPSPoint, Config.getGPSPoint2(), Config.bearing2);
		double[] BFRPoint3 = Calibrator.computeIO_BFR(Config.OriginalGPSPoint, Config.getGPSPoint3(), Config.bearing3);
		double[] BFRPoint4 = Calibrator.computeIO_BFR(Config.OriginalGPSPoint, Config.getGPSPoint4(), Config.bearing4);
		
		long start1, end1, start2, end2;
		String line;

		//Compute Bridge Parameters
		//ApproximateAlgorithm sel=new ApproximateAlgorithm(Config.getGPSPoint1(), Config.getGPSPoint2(), Config.getGPSPoint3(), Config.getGPSPoint4(), Config.getBFRPoint1(), Config.getBFRPoint2(), Config.getBFRPoint3(), Config.getBFRPoint4());
		ApproximateAlgorithm sel=new ApproximateAlgorithm(Config.getGPSPoint1(), Config.getGPSPoint2(), Config.getGPSPoint3(), Config.getGPSPoint4(), BFRPoint1, BFRPoint2, BFRPoint3, BFRPoint4);
		
		double[] bridgeOrigin = sel.getBridgeOrigin();
		InspectorParametersInterface positionParameters=new InspectorParameters(sel.BG, sel.gpsScalingFactors, sel.bridge_origin);
		double[] inspectorBFRCheck = null;
		//System.out.println("Bridge origin is " + BFRPoint1[0] + " " + BFRPoint1[1] + " " + BFRPoint1[2]);
		ArrayList<Long> durationsList = new ArrayList<Long>(); 
		
		// Performance measurements -> Rerun the same algorithm for specified number of times
		int testCounter  = 0;
		double overallDurationMean = 0, overallDurationVariance = 0;
		ArrayList<Double> overallDurationsList = new ArrayList<Double>();
		while(testCounter < 1)
		{
			//For file writing
			BufferedReader in = new BufferedReader(new FileReader(Config.getAllPositions()));
			long duration = 0;
			int posCount = 0;

			//Loop through the position/orientation file and pass inspector position/orientation to InspectorParameters object
			while((line=in.readLine()) != null)
			{ 
				start1 = System.nanoTime();	
				String[] splitLine = line.split("\\s+");
				//System.out.println(splitLine.length + " is the line length");
			
				// If data is incorrect, like missing altitude, proceed to next point
				if(splitLine.length < 6)
				{
					continue;
				}


				//Compute Inspector Position Parameters
				positionParameters.computeBoundingBoxParameters(splitLine);
					
				inspectorBFRCheck = positionParameters.getInspectorBFR();
				//double[] inspectorOrientCheck = positionParameters.getInspectorOrientation();

				sel.getEuclideanDistance(inspectorBFRCheck);

				//System.out.println(posCount+"\t"+inspectorBFRCheck[0]+"\t"+inspectorBFRCheck[1]+"\t"+inspectorBFRCheck[2]+"\t"+inspectorOrientCheck[0]+"\t"+inspectorOrientCheck[1]+"\t"+inspectorOrientCheck[2]);
				//File handling and bounding box computation section 
				//ComputeBoundingBoxInterface boundingBox=new ComputeBoundingBox(positionParameters.getInspectorBFR(), positionParameters.getNearPlane(), positionParameters.getFarPlane(), positionParameters.getViewAngle(), posCount);
				//boundingBox.captureNodesInBox();
				//if(posCount == 0)
				//{
				//	System.out.println("First coordingates " + inspectorBFRCheck[0] + " " +  inspectorBFRCheck[1] + " " +  inspectorBFRCheck[2]);				
				//}
				end1 = System.nanoTime();
				duration += (end1 - start1);
				durationsList.add(duration);
				posCount++;
			}		

			double durationMean = (double)duration/(double)posCount;
			overallDurationsList.add(durationMean);
			overallDurationMean += durationMean;
			System.out.println("Duration is " + durationMean);
			System.out.println("Count is " + posCount);
			double variance = 0;
			for(int i = 0; i < durationsList.size(); i++)
			{
				variance += Math.pow((durationsList.get(i) - durationMean), 2);
			}

			variance = (double)variance/(double)durationsList.size();
			System.out.println(Math.sqrt(variance) + " is the variance and iteration count is " + testCounter);	
			durationsList.clear();
			testCounter++;
		}
		overallDurationMean = (double)overallDurationMean / (double)testCounter;
		for(int i = 0; i < overallDurationsList.size(); i++)
		{
			overallDurationVariance += Math.pow((overallDurationsList.get(i) - overallDurationMean),2);
		}
		overallDurationVariance = Math.sqrt((double)overallDurationVariance/(double)testCounter);
		System.out.println("Overall duration mean and variance are " + overallDurationMean + " " + overallDurationVariance);
	}			
}
