
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/
package vincenti;

import org.gavaghan.geodesy.CDF_Weibull2;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GeodeticMeasurement;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.gavaghan.geodesy.GlobalPosition;

import java.util.LinkedList;
import java.util.Collections;
import java.util.Random;
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

public class Vincenti_error_analysis 
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
	double range,discreteStep, lambda;
	int beta, total_samples; //used for error sampling
	Random generator;
	
	public Vincenti_error_analysis()
	{
		bridge_origin=new double[3];
		bridge_origin[0]=-83.346803; //compute the bridge origin in GPS
		bridge_origin[1]=42.018544;
		bridge_origin[2]=183.2;
		BFR_roll=0;
		BFR_pitch=0;
		BFR_yaw=-61.237;
		BFR_azimuth=-BFR_yaw;
		
		range=5;	//default is 5
		discreteStep=0.1;	//default=0.1
		lambda=1/1.5;	//default=1/1.5
		beta=2;	//default=2
		total_samples=100;	//default=100
		generator=new Random();//initialize random number generator

		flag1=0;flag2=0;flag3=0;flag4=0;flag5=0;
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
	
	void computeIO_BFR()
	{
		// instantiate the calculator
		GeodeticCalculator geoCalc = new GeodeticCalculator();
		// select a reference ellipsoid
		Ellipsoid reference = Ellipsoid.WGS84;
		// set Pike's Peak position
		GlobalPosition BO;
		BO = new GlobalPosition(bridge_origin[1], bridge_origin[0], bridge_origin[2]);

		// set Alcatraz Island coordinates
		GlobalPosition IO;
		IO = new GlobalPosition(inspector_origin[1], inspector_origin[0], inspector_origin[2]);

		// calculate the geodetic measurement
		GeodeticMeasurement geoMeasurement;
		double p2pmeters;
		double elevChangeMeters;
		double azimuth;

		geoMeasurement = geoCalc.calculateGeodeticMeasurement(reference, BO, IO);
		
		p2pmeters = geoMeasurement.getPointToPointDistance();
		elevChangeMeters = geoMeasurement.getElevationChange();
		azimuth=geoMeasurement.getAzimuth();

		double angle_p=Math.asin(elevChangeMeters/p2pmeters);

		inspector_BFR=new double[3];
		inspector_BFR[0]=p2pmeters*Math.cos(angle_p)*Math.sin(Math.PI*(azimuth-BFR_azimuth)/180);
		inspector_BFR[1]=p2pmeters*Math.cos(angle_p)*Math.cos(Math.PI*(azimuth-BFR_azimuth)/180);
		inspector_BFR[2]=elevChangeMeters;
			
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
	
	double return_area(int i)
	{
		return Math.floor((total_samples*(CDF_Weibull2.w2cdf(lambda,beta,(discreteStep*(i+1))))));
	}
	
	double error_sampler(int numCall)
	{
		double sample_dist=100;
		double i1, i2;
		for(int i=0;i<range/discreteStep;i++)
		{
			//System.out.print("i value ");
			//System.out.println(i);
			if(i==0)
			{	i1=0;
				i2=return_area(0)-1;
			}
			else
			{	i1=return_area(i-1);
				i2=return_area(i)-1;
			}
			if(i2>i1)
			{
				if(numCall>=i1 && numCall<=i2)
				{
					sample_dist=(i*discreteStep)+(discreteStep/2);
					break;
				}
			}
			else
			{
				if(numCall>=i2 && numCall<=i1)
				{
					sample_dist=(i*discreteStep)+(discreteStep/2);
					break;
				}
			}
		}
		return sample_dist;
	}

	double[] error_inspectorposition(int count, double[] inspector_loc) throws IOException
	{
		GeodeticCalculator geocalc=new GeodeticCalculator();
		Ellipsoid reference = Ellipsoid.WGS84;
		
		GlobalCoordinates GPS1;
		GPS1=new GlobalCoordinates(inspector_loc[1],inspector_loc[0]);
		
		/*double[] scaling={0,0,0};
		double[] temp={1,1,1};
		
		for(int i=0;i<3;i++)
			scaling[i]=1/temp[i];*/
		double[] inspector_witherror={0,0,0};
		double distance_error=error_sampler(count);
		System.out.println(count + " "+distance_error);
		double angle=generator.nextDouble()*360.0;
		/*errorGPS[0]=distance_error*Math.cos(angle)*scaling[0];
		errorGPS[1]=distance_error*Math.sin(angle)*scaling[1];
		errorGPS[2]=0;	//altitude error is 0*/
	    //double[] endBearing = new double[1];
	    GlobalCoordinates dest=geocalc.calculateEndingGlobalCoordinates(reference, GPS1, angle, distance_error);
	    
		inspector_witherror[0]=dest.getLongitude();
		inspector_witherror[1]=dest.getLatitude();
		inspector_witherror[2]=inspector_loc[2];
		return inspector_witherror;
	}
	
	public static void main(String args[]) throws IOException
	{
		long start1, end1, start2, end2;
		int count=0, count_error=0;
		double [] inspector_position={0,0,0};
		double [] inspector_position_func={0,0,0};
			
		String destination="/home/athuls89/Desktop/gsra/error_files/vincenti/error_GPS_orient/case5/run10/file";
		String file_write;
		double roll_val,pitch_val,yaw_val;
		Random gen_angle=new Random();
		
		Vincenti_error_analysis temp1=new Vincenti_error_analysis();
		temp1.inspector_origin=new double[3];

		//Provide inspector position and orientation in w.r.t. GPS frame
		inspector_position[0]=-83.346581;
		inspector_position[1]=42.018851;
		inspector_position[2]=178.5;
		roll_val=0;
		pitch_val=6.9157;
		yaw_val=-208.3507;
		temp1.query_IFR=new double[3];

		//error values
		double[] error_inspectororientation={0,0,0};
		double[] error_GPS;
		
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
		
		//Set the NPD and FPD values, View angle
		temp1.setNPD_FPD(19.5168,39.0336);
		temp1.setViewAngle(30);
		end1=System.nanoTime();
		
		//File handling section 
		while(count_error<temp1.total_samples)
		{
			file_write=destination.concat(Integer.toString(count_error));
			
			//call the error function to get inspector position with error
			inspector_position_func=temp1.error_inspectorposition(count_error,inspector_position);
						
			/*for(int i=0;i<3;i++)
				inspector_position_func[i]=inspector_position[i]+error_GPS[i];*/
			
			for(int j=0;j<3;j++)
				error_inspectororientation[j]=gen_angle.nextDouble()-0.5;	
			
			//Add the error component to orientation of inspector
			temp1.IFR_roll=roll_val+error_inspectororientation[0];
			temp1.IFR_pitch=pitch_val+error_inspectororientation[1];
			temp1.IFR_yaw=yaw_val+error_inspectororientation[2];	//roll, pitch, yaw
			System.out.println("hello"+" "+temp1.IFR_roll+" "+temp1.IFR_pitch+" "+temp1.IFR_yaw);
			//Set IFR origin (with error w.r.t. GPS), and compute the inspector position and
			//orientation in BFR
			temp1.setIFR_origin(inspector_position_func);
			temp1.computeIO_BFR();
			temp1.computeIFRorient_BFR();
			
			//Compute the NPD and FPD points in BFR
			temp1.computeBoundingBoxParameters();	
			
			BufferedReader in = new BufferedReader(new FileReader("/home/athuls89/Desktop/result_file"));
			BufferedWriter out = new BufferedWriter(new FileWriter(file_write));
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
			count_error++;
	//		System.out.println(end1-start1);	
		}
//		System.out.println(end2-start2);
	}
}