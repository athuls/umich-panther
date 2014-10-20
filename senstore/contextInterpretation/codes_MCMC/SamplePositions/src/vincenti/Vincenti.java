
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/
package vincenti;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GeodeticMeasurement;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.gavaghan.geodesy.GlobalPosition;

import java.util.LinkedList;
import java.util.Collections;
//import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.Object;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.io.ByteArrayOutputStream;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import Jama.*;

public class Vincenti 
{
	/*bridge_origin->GPS, inspector_origin->GPS, query_IFR->point in IFR to convert to BFR; 
	* inspector_BFR->inspector position in BFR
	* BFR_roll, BFR_pitch, BFR_yaw -> orientation of BFR w.r.t GPS 
	* F1 & F2->points representing near and far plane distance for inspector*/
	double[] bridge_origin, inspector_origin, query_IFR, inspector_BFR,F1, F2;
	double BFR_roll, BFR_pitch, BFR_yaw, roll_IB, pitch_IB, yaw_IB, BFR_azimuth;
	double[] query_BFR;	//query point in BFR
	//roll, pitch, yaw determine orientation of inspector w.r.t. GPS;
	//FPD_inspector, NPD_inspector-near and far plane distance of cone in IFR;
	//view_angle-angle of view in IFR
	double IFR_roll,IFR_pitch,IFR_yaw, FPD_inspector, NPD_inspector, view_angle;
	int flag1, flag2, flag3, flag4, flag5;

	GeodeticCalculator m_geoCalc;
	Ellipsoid m_ellipsoidReference;	
	GlobalPosition m_GlobalPositionBO;
	
	public Vincenti()
	{
		bridge_origin=new double[3];
		
		// Add latitude/longitude coordinates with sign, beginning with longitude
		bridge_origin[0]=12.48018019999995; //compute the bridge origin in GPS
		bridge_origin[1]=41.8723889;
		bridge_origin[2]=12.6;
		BFR_roll=0;
		BFR_pitch=0;
		BFR_yaw=-61.237;
		BFR_azimuth=-BFR_yaw;
		flag1=0;flag2=0;flag3=0;flag4=0;flag5=0;

		// instantiate the calculator
		m_geoCalc = new GeodeticCalculator();
		// select a reference ellipsoid
		m_ellipsoidReference = Ellipsoid.WGS84;
		// Create global position instance for bridge origin
		m_GlobalPositionBO = new GlobalPosition(bridge_origin[1], bridge_origin[0], bridge_origin[2]);
	}
	
	public void setOrientation_inspector(double roll1, double pitch1, double yaw1)
	//Set the orientation of the inspector with respect to GPS
	{
		IFR_roll=-Math.PI*(roll1/180);
		IFR_pitch=-Math.PI*(pitch1/180);
		IFR_yaw=-Math.PI*(yaw1/180);
		flag1=1;
	} 
	
	double[] computeQuery_BFR()
	//Compute and return IG matrix
	{
		/*IG=[Angle Matrix (with z(theta1)->alpha(yaw), x(theta2)->beta(pitch), y(theta3)->gamma(roll))] *
		 * [0,0,scaling_factor[0];0,0,scaling_factor[1];0,0,scaling_factor[2]*/
		//if(flag1==1)
		//{
			double [][]point_temp = {query_IFR};
			Matrix IFR_point=new Matrix(point_temp);
			double [][]point1_temp={inspector_BFR};
			Matrix BFR_point=new Matrix(point1_temp);
			
			Matrix temp_yaw=new Matrix(3,3);
			Matrix temp_pitch=new Matrix(3,3);
			Matrix temp_roll=new Matrix(3,3);
			Matrix Angle=new Matrix(3,3);
			Matrix Angle1=new Matrix(3,3);
			//Setting the roll, pitch and yaw matrices
			temp_roll.set(0, 0, Math.cos(roll_IB));
			temp_roll.set(0, 2, Math.sin(roll_IB));
			temp_roll.set(1, 1, 1);
			temp_roll.set(2, 0, -Math.sin(roll_IB));
			temp_roll.set(2, 2, Math.cos(roll_IB));
			
			temp_pitch.set(0, 0, 1);
			temp_pitch.set(1, 1, Math.cos(pitch_IB));
			temp_pitch.set(1, 2, -Math.sin(pitch_IB));
			temp_pitch.set(2, 1, Math.sin(pitch_IB));
			temp_pitch.set(2, 2, Math.cos(pitch_IB));
			
			temp_yaw.set(0, 0, Math.cos(yaw_IB));
			temp_yaw.set(0, 1, -Math.sin(yaw_IB));
			temp_yaw.set(1, 0, Math.sin(yaw_IB));
			temp_yaw.set(1, 1, Math.cos(yaw_IB));
			temp_yaw.set(2, 2, 1);
			
			
			//Angle=temp_yaw.times(temp_pitch).times(temp_roll);
			Angle=temp_roll.times(temp_pitch).times(temp_yaw);
		
			
			Matrix final_point_BFR=new Matrix(3,1);
			//final_point_BFR=Angle.times(IFR_point.transpose());
			final_point_BFR=IFR_point.times(Angle);
			
		
			final_point_BFR=final_point_BFR.plus(BFR_point);
			query_BFR=new double[3];

			query_BFR[0]=final_point_BFR.get(0,0);
			query_BFR[1]=final_point_BFR.get(0,1);
			query_BFR[2]=final_point_BFR.get(0,2);
			flag1=0;
		
			return query_BFR;
		//}
		
	}
	
	void computeBoundingBoxParameters()
	//Compute bounding box parameters i.e. 2 points and angle representing conic region of interest
	{
			double[] point={0,NPD_inspector,0};
			query_IFR=point;
			F1=computeQuery_BFR();
			point[0]=0;
			point[1]=FPD_inspector;
			point[2]=0;
			query_IFR=point;
			F2=computeQuery_BFR();
			
	}
	
	double computeIO_BFR()
	{
		// set Alcatraz Island coordinates
		GlobalPosition IO;
		IO = new GlobalPosition(inspector_origin[1], inspector_origin[0], inspector_origin[2]);

		// calculate the geodetic measurement
		GeodeticMeasurement geoMeasurement;
		double p2pmeters = 0;
		double elevChangeMeters;
		double azimuth;

		geoMeasurement = m_geoCalc.calculateGeodeticMeasurement(m_ellipsoidReference, m_GlobalPositionBO, IO);
		
		p2pmeters = geoMeasurement.getPointToPointDistance();
		//elevChangeMeters = geoMeasurement.getElevationChange();
		//azimuth=geoMeasurement.getAzimuth();

		//double angle_p=Math.asin(elevChangeMeters/p2pmeters);

		//inspector_BFR=new double[3];
		//inspector_BFR[0]=p2pmeters*Math.cos(angle_p)*Math.sin(Math.PI*(azimuth-BFR_azimuth)/180);
		//inspector_BFR[1]=p2pmeters*Math.cos(angle_p)*Math.cos(Math.PI*(azimuth-BFR_azimuth)/180);
		//inspector_BFR[2]=elevChangeMeters;
		
		return p2pmeters;		
	}

	double computeIO_BFR_Android()
	{
		double lat1 = bridge_origin[1];
		double lon1 = bridge_origin[0];
		double lat2 = inspector_origin[1];
		double lon2 = inspector_origin[0];
		// Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        	// using the "Inverse Formula" (section 4)

        	int MAXITERS = 20;
        	// Convert lat/long to radians
        	lat1 *= Math.PI / 180.0;
        	lat2 *= Math.PI / 180.0;
        	lon1 *= Math.PI / 180.0;
        	lon2 *= Math.PI / 180.0;

        	double a = 6378137.0; // WGS84 major axis
        	double b = 6356752.3142; // WGS84 semi-major axis
        	double f = (a - b) / a;
        	double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        	double L = lon2 - lon1;
        	double A = 0.0;
        	double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        	double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        	double cosU1 = Math.cos(U1);
        	double cosU2 = Math.cos(U2);
        	double sinU1 = Math.sin(U1);
        	double sinU2 = Math.sin(U2);
        	double cosU1cosU2 = cosU1 * cosU2;
        	double sinU1sinU2 = sinU1 * sinU2;

        	double sigma = 0.0;
        	double deltaSigma = 0.0;
        	double cosSqAlpha = 0.0;
        	double cos2SM = 0.0;
        	double cosSigma = 0.0;
        	double sinSigma = 0.0;
        	double cosLambda = 0.0;
        	double sinLambda = 0.0;

        	double lambda = L; // initial guess
        	for (int iter = 0; iter < MAXITERS; iter++) {
        	    double lambdaOrig = lambda;
        	    cosLambda = Math.cos(lambda);
        	    sinLambda = Math.sin(lambda);
        	    double t1 = cosU2 * sinLambda;
        	    double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
        	    double sinSqSigma = t1 * t1 + t2 * t2; // (14)
        	    sinSigma = Math.sqrt(sinSqSigma);
        	    cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
        	    sigma = Math.atan2(sinSigma, cosSigma); // (16)
        	    double sinAlpha = (sinSigma == 0) ? 0.0 :
        	        cosU1cosU2 * sinLambda / sinSigma; // (17)
        	    cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
        	    cos2SM = (cosSqAlpha == 0) ? 0.0 :
        	        cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

        	    double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
        	    A = 1 + (uSquared / 16384.0) * // (3)
        	        (4096.0 + uSquared *
        	         (-768 + uSquared * (320.0 - 175.0 * uSquared)));
        	    double B = (uSquared / 1024.0) * // (4)
        	        (256.0 + uSquared *
        	         (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
        	    double C = (f / 16.0) *
        	        cosSqAlpha *
        	        (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
        	    double cos2SMSq = cos2SM * cos2SM;
        	    deltaSigma = B * sinSigma * // (6)
        	        (cos2SM + (B / 4.0) *
        	         (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
        	          (B / 6.0) * cos2SM *
        	          (-3.0 + 4.0 * sinSigma * sinSigma) *
        	          (-3.0 + 4.0 * cos2SMSq)));

        	    lambda = L +
        	        (1.0 - C) * f * sinAlpha *
        	        (sigma + C * sinSigma *
        	         (cos2SM + C * cosSigma *
        	          (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

        	    double delta = (lambda - lambdaOrig) / lambda;
        	    if (Math.abs(delta) < 1.0e-12) {
        	        break;
        	    }
        	}

        	float distance = (float) (b * A * (sigma - deltaSigma));
        	return distance;
		/*
        	if (results.length > 1) {
        	    float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
        	        cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
        	    initialBearing *= 180.0 / Math.PI;
        	    results[1] = initialBearing;
        	    if (results.length > 2) {
        	        float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
        	            -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
        	        finalBearing *= 180.0 / Math.PI;
        	        results[2] = finalBearing;
        	    }
        	}*/
	}
	
	void computeIFRorient_BFR()
	{
		roll_IB=-(Math.PI*(IFR_roll-BFR_roll))/180;
		pitch_IB=-(Math.PI*(IFR_pitch-BFR_pitch))/180;
		yaw_IB=-(Math.PI*(IFR_yaw-BFR_yaw))/180;
		
	}
	
	public void setIFR_origin(double[] origin)
	//Inspector origin in GPS
	{
		inspector_origin=origin;
		flag2=1;
	}
	
	public void setQueryPoint(double[] point)
	//set the query point, to determine if it exists within the bounding box region
	{
		query_BFR=point;
		//query_BFR=computeQuery_BFR();
	}
	
	public void setNPD_FPD(double d1, double d2)
	//set the FPD and NPD values i.e. Far plane distance and near plane distance, of the inspector
	{
		NPD_inspector=d1;
		FPD_inspector=d2;	
		flag4=1;
	}
	
	public void setViewAngle(double angle)
	//set the view angle of the cone of interest, for the inspector
	{
		view_angle=Math.PI*(angle/180);
		flag5=1;
	}
	
	public boolean checkContainment()
	//check for containment of query_point in the bounding box
	{
		double[][] temp_I={inspector_BFR};	
		double phi;
		double[][] temp_P={query_BFR};	//this is in BFR
		double[][] temp_F1={F1};	//this is in BFR
		double[][] temp_F2={F2};	//this is in BFR
		Matrix M_I=new Matrix(temp_I);
		Matrix M_P=new Matrix(temp_P);
		Matrix M_F1=new Matrix(temp_F1);
		Matrix M_F2=new Matrix(temp_F2);
		//phi=(M_P.minus(M_I).transpose().times(M_F1.minus(M_I)).get(0,0))/((M_P.minus(M_I)).normF()*(M_F1.minus(M_I)).normF());
		phi=(((M_P.minus(M_I).get(0,0))*(M_F1.minus(M_I).get(0,0))) + ((M_P.minus(M_I).get(0,1))*(M_F1.minus(M_I).get(0,1))) + ((M_P.minus(M_I).get(0,2))*(M_F1.minus(M_I).get(0,2))))/((M_P.minus(M_I)).normF()*(M_F1.minus(M_I)).normF());
		if(M_I.minus(M_F1).normF()<=(((M_I.minus(M_P)).normF())*Math.abs(phi)) && ((M_I.minus(M_P).normF())*Math.abs(phi))<=M_I.minus(M_F2).normF() && Math.acos(phi)<=view_angle)
			return true;
		else
			return false;
	}
	
	/*public double[] getBFR_co()
	//Convert a query point from IFR coordinates to BFR coordinates
	{
			Matrix temp_point, temp_origin, temp_bridge;
			double[][] temp={query_IFR};	//query in IFR
			temp_point=new Matrix(temp);
			double[][] temp1={inspector_origin};	//origin in GPS
			temp_origin=new Matrix(temp1);
			double[][] temp2={bridge_origin};	//origin in GPS
			temp_bridge=new Matrix(temp2);
			computeIGMatrix();
			
			return (temp_point.times((BG.times(IG.inverse())).inverse())).plus((temp_origin.minus(temp_bridge)).times(BG.inverse())).getArray()[0];
	}*/
	
	public static void main(String args[]) throws IOException
	{
		long start1 = 0, end1 = 0, start2, end2, duration=0, countLoop = 0;
		
		Vincenti temp1=new Vincenti();
		temp1.inspector_origin=new double[3];

		/*
		temp1.inspector_origin[0]=-83.346581;
		temp1.inspector_origin[1]=42.018851;
		temp1.inspector_origin[2]=178.5;
		temp1.IFR_roll=0;
		temp1.IFR_pitch=6.9157;
		temp1.IFR_yaw=-208.3507;
		
		temp1.inspector_origin[0]=-83.3464666667;
		temp1.inspector_origin[1]=42.018018;
		temp1.inspector_origin[2]=627;
		temp1.IFR_roll=0;
		temp1.IFR_pitch=-10;
		temp1.IFR_yaw=-142;
		temp1.query_IFR=new double[3];
		double linearDistance = temp1.computeIO_BFR();
		temp1.computeIFRorient_BFR();
		*/
		String city = "Rome";
			
		BufferedReader in = new BufferedReader(new FileReader("/mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/" + city + "LocLinearInput"));
		BufferedWriter out = new BufferedWriter(new FileWriter("/mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/VincentyDistances/output_"+city+"VincentyDistancesTest"));
		String line;
		
		ArrayList<Double[]> inputSamplePositions = new ArrayList<Double[]>();
		while((line = in.readLine()) != null)
		{
			String[] splitLine = line.split("\\s+");			
			
			if(splitLine.length < 6)
			{
				continue;
			}

			Double[] inspectorPosition = new Double[3];
			for(int i = 0; i < 3; i++)
			{
				inspectorPosition[i] = Double.parseDouble(splitLine[i]);
			}

			inputSamplePositions.add(inspectorPosition);
		}
		
		double durationMean = 0;
		ArrayList<Long> overallDurations = new ArrayList<Long>();
		for(Double[] splitLine : inputSamplePositions)
		{
			start1=System.nanoTime();
			for(int i = 0; i < 3; i++)
			{
				temp1.inspector_origin[i]=splitLine[i];
			}

			//double distance = temp1.computeIO_BFR();
			double distance = temp1.computeIO_BFR_Android();
			end1=System.nanoTime();
			Long durationEntry = (end1 - start1);
			overallDurations.add(durationEntry);
			countLoop++;
			//out.write((distance * 0.000621371) + "\n");	
		}

		double durationVariance = 0;
		for(Long durationEntry : overallDurations)
		{
			System.out.println(durationEntry);
			durationMean += durationEntry;
		}

		durationMean = (double)durationMean/(double)countLoop;
		for(Long durationEntry : overallDurations)
		{
			durationVariance += Math.pow(durationEntry - durationMean, 2);
		}
		durationVariance = (double)durationVariance/(double)countLoop;
		durationVariance = Math.sqrt(durationVariance);
		System.out.println("Actual mean is " + durationMean);
		System.out.println("Actual variance is " + durationVariance);

		//double durationMean = (double)currentDuration/(double)countLoop;
		out.close();
		in.close();
		//temp1.computeQuery_BFR();
		/*
		//Enter the GPS and bridge coordinates for 4 points on the bridge (used to determine the origin of the bridge
		double[] gpspoint1={-83.346745, 42.018465, 183.2};
		double[] gpspoint2={-83.346513, 42.018149, 183.2};
		double[] gpspoint3={-83.346513, 42.018149, 233.2};
		double[] gpspoint4={-83.345745, 42.018977, 183.2};
		double[] bfrpoint1={10, 0, 0};
		double[] bfrpoint2={50, 0, 0};
		double[] bfrpoint3={50, 0, 50};
		double[] bfrpoint4={0, 100, 0};
		
		vincenti sel=new vincenti(gpspoint1, gpspoint2, gpspoint3, gpspoint4,bfrpoint1, bfrpoint2, bfrpoint3,bfrpoint4);
		
		//Position of the inspector in GPS
		double[] inspector_position={-83.345299, 42.01711, 178.5};	//this is in GPS
		int count=0;
		
		//Set orientation of the inspector 
		sel.setOrientation_inspector(0,1.3312,-321.97866);	//roll, pitch, yaw
		sel.setIFR_origin(inspector_position);*/
		
		//Set the region of interest (near and far plane distances) 
		//Compute bounding box parameters
		/*temp1.setNPD_FPD(19.5168,39.0336);
		temp1.computeBoundingBoxParameters();
		temp1.setViewAngle(30);
		end1=System.nanoTime();
		int count=0;*/
		//File handling section 

/*
			BufferedReader in = new BufferedReader(new FileReader(""));
			BufferedWriter out = new BufferedWriter(new FileWriter("/home/athuls89/Desktop/gsra/result_vincenti_test_8"));
			String str;
			Integer object_id;
			start2=System.nanoTime();
			while((str=in.readLine()) != null)
			{		
					count++;
					String temp[];
					double query1[]={0,0,0};
					temp=str.split("\\s+");
					object_id=Integer.parseInt(temp[0]);
					for(int i=1;i<temp.length-1;i++)
					{
						query1[i-1]=Double.parseDouble(temp[i]);									
					}
					temp1.setQueryPoint(query1);
					if(temp1.checkContainment())
					{
							out.write(Integer.toString(object_id));
							out.write(" ");
							for(int i=0;i<3;i++)
							{	out.write(Double.toString(query1[i]));
								out.write(" ");
							}	
								out.write(" ;\n");
					}
			}
			end2=System.nanoTime();
			in.close();
			out.close();
			System.out.println(end1-start1);			
		System.out.println(end2-start2);*/
	}
}
