package approximateApproach;
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/

import java.io.IOException;

import Jama.Matrix;

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

	public static void main(String args[]) throws IOException
	{
		
		//For file writing
		BufferedReader in = new BufferedReader(new FileReader(Config.getAllPositions()));
		int posCount=0;
		String line;

		//Compute Bridge Parameters
		ApproximateAlgorithm sel=new ApproximateAlgorithm(Config.getGPSPoint1(), Config.getGPSPoint2(), Config.getGPSPoint3(), Config.getGPSPoint4(), Config.getBFRPoint1(), Config.getBFRPoint2(), Config.getBFRPoint3(), Config.getBFRPoint4());
		
		//Loop through the position/orientation file and pass inspector position/orientation to InspectorParameters object
		while((line=in.readLine()) != null)
		{ 
			//Compute Inspector Position Parameters
			InspectorParametersInterface positionParameters=new InspectorParameters(line, sel.BG, sel.gpsScalingFactors, sel.bridge_origin);
			positionParameters.computeBoundingBoxParameters();
				
			//File handling and bounding box computation section 
			ComputeBoundingBoxInterface boundingBox=new ComputeBoundingBox(positionParameters.getInspectorBFR(), positionParameters.getNearPlane(), positionParameters.getFarPlane(), positionParameters.getViewAngle(), posCount);
			boundingBox.captureNodesInBox();
			posCount++;
		}

	}			
	
}
