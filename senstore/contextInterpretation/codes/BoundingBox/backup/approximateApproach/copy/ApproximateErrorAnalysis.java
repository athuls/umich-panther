package approximateApproach.copy;
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import Jama.Matrix;
import WeibullCDF.CDF_Weibull2;

public class ApproximateErrorAnalysis 
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
	double[] bridge_origin, inspector_origin, convert_query, inspector_BFR, F1, F2;
	double[] query_point;	//query point in BFR
	//roll, pitch, yaw determine orientation of inspector w.r.t. GPS;
	//FPD_inspector, NPD_inspector-near and far plane distance of cone in IFR;
	//view_angle-angle of view in IFR
	double roll,pitch,yaw, FPD_inspector, NPD_inspector, view_angle;
	int flag1, flag2, flag3, flag4, flag5;
	double range,discreteStep, lambda;
	int beta, total_samples; //used for error sampling
	Random generator;
	BufferedWriter out;
	
	public ApproximateErrorAnalysis(double[] pt1, double[] pt2, double[] pt3, double[] pt4, double[] pt5, double[] pt6, double[] pt7, double[] pt8) throws IOException
	{
		gps_p1=pt1;
		gps_p2=pt2;
		gps_p3=pt3;
		gps_p4=pt4;
		bfr_p1=pt5;
		bfr_p2=pt6;
		bfr_p3=pt7;
		bfr_p4=pt8;
		
		range=5;	//default is 5
		discreteStep=0.1;	//default=0.1
		lambda=1/1.5;	//default=1/1.5
		beta=2;	//default=2
		total_samples=100;	//default=100
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/athuls89/Desktop/gsra/distance_values"));
		generator=new Random();//initialize random number generator
		//creating 'a' and 'c' matrices
		double[][] temp2={bfr_p1,bfr_p2,bfr_p3,bfr_p4};
		double[][] temp1= {gps_p1, gps_p2, gps_p3, gps_p4};
		c=new Matrix(temp1);
		a=new Matrix(temp2);
		
		// Given a set of 4 points on a bridge
		BG = computeBGMatrix();	//Compute the BG Matrix
		IG=Matrix.identity(3,3);//Initialize the IG matrix to be undefined in the beginning
		IG.set(0, 0, 0.0d/0.0);
		IG.set(1, 1, 0.0d/0.0);
		IG.set(2, 2, 0.0d/0.0);
		gpsScalingFactors = computeScalingFactors(); //Compute the scaling factors for the bridge
		bridge_origin=computeBridgeorigin(); //compute the bridge origin in GPS
		flag1=0;flag2=0;flag3=0;flag4=0;flag5=0;
	}
	
	Matrix computeBGMatrix()
	{
		/*Each row of Matrix a (3X3) is the set of 3 coordinates of a point in Bridge system
		 * Each row of Matrix c (3X3) is the set of 3 coordinates of a point in GPS*/
		/*Matrix (a[1]-a[2]) [BG]=c[1]-c[2]
		 * 3 pairs of such equations to solve for BG*/
		
		Matrix tempBridge=new Matrix(3,3);
		Matrix tempGPS=new Matrix(3,3);
		
		Matrix Bridge=new Matrix(3,3);
		Matrix GPS=new Matrix(3,3);
		for(int i=0;i<3;i++)
		{
			tempBridge.setMatrix(i,i,0,2,a.getMatrix(0,0,0,2).minus(a.getMatrix(i+1,i+1,0,2)));
			tempGPS.setMatrix(i,i,0,2,c.getMatrix(0,0,0,2).minus(c.getMatrix(i+1,i+1,0,2)));
		}		
		for(int i=0;i<3;i++)
		{
			Bridge.setMatrix(i,i,0,2,tempBridge.getMatrix(i,i,0,2));
			GPS.setMatrix(i,i,0,2,tempGPS.getMatrix(i,i,0,2));
		}
		return (Bridge.solve(GPS));
	}
	
	public Matrix getBGMatrix()
	//Returns BG matrix
	{
		return (computeBGMatrix());
	}
	
	double[] computeScalingFactors()
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
	
	public void setOrientation_inspector(double roll1, double pitch1, double yaw1)
	//Set the orientation of the inspector with respect to GPS
	{
		roll=-Math.PI*(roll1/180);
		pitch=-Math.PI*(pitch1/180);
		yaw=-Math.PI*(yaw1/180);
		flag1=1;
	} 
	
	void computeIGMatrix()
	//Compute and return IG matrix
	{
		/*IG=[Angle Matrix (with z(theta1)->alpha(yaw), x(theta2)->beta(pitch), y(theta3)->gamma(roll))] *
		 * [0,0,scaling_factor[0];0,0,scaling_factor[1];0,0,scaling_factor[2]*/
		if(flag1==1)
		{
			double[][] scaling;
			scaling=new double[1][3];
			scaling[0]=computeScalingFactors();
			Matrix scale=new Matrix(scaling);
			Matrix temp_IG=Matrix.identity(3, 3);
			for(int i=0;i<3;i++)
				temp_IG.set(i,i,1/scale.get(0,i));
			Matrix temp_yaw=new Matrix(3,3);
			Matrix temp_pitch=new Matrix(3,3);
			Matrix temp_roll=new Matrix(3,3);
			Matrix Angle=new Matrix(3,3);
			//Setting the roll, pitch and yaw matrices
			temp_roll.set(0, 0, Math.cos(roll));
			temp_roll.set(0, 2, Math.sin(roll));
			temp_roll.set(1, 1, 1);
			temp_roll.set(2, 0, -Math.sin(roll));
			temp_roll.set(2, 2, Math.cos(roll));
			
			temp_pitch.set(0, 0, 1);
			temp_pitch.set(1, 1, Math.cos(pitch));
			temp_pitch.set(1, 2, -Math.sin(pitch));
			temp_pitch.set(2, 1, Math.sin(pitch));
			temp_pitch.set(2, 2, Math.cos(pitch));
			
			temp_yaw.set(0, 0, Math.cos(yaw));
			temp_yaw.set(0, 1, -Math.sin(yaw));
			temp_yaw.set(1, 0, Math.sin(yaw));
			temp_yaw.set(1, 1, Math.cos(yaw));
			temp_yaw.set(2, 2, 1);
	
			Angle=temp_roll.times(temp_pitch).times(temp_yaw);
			IG=Angle.times(temp_IG);
			flag1=0;
		}
		
	}
	
	public Matrix getIGMatrix()
	//Return the IG matrix
	{
		return IG;
	}
	
	void computeBoundingBoxParameters()
	//Compute bounding box parameters i.e. 2 points and angle representing conic region of interest
	{
			double[] point={0,NPD_inspector,0};
			convert_query=point;
			F1=getBFR_co();
			point[0]=0;
			point[1]=FPD_inspector;
			point[2]=0;
			convert_query=point;
			F2=getBFR_co();

			for(int i=0;i<3;i++)	//see what to do about this
				convert_query[i]=0;
			inspector_BFR=getBFR_co();
			
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
		query_point=point;
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
		double[][] temp_P={query_point};	//this is in BFR
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
	
	public double[] getBFR_co()
	//Convert a query point from IFR coordinates to BFR coordinates
	{
			Matrix temp_point, temp_origin, temp_bridge;
			double[][] temp={convert_query};	//query in IFR
			temp_point=new Matrix(temp);
			double[][] temp1={inspector_origin};	//inspector origin in GPS
			temp_origin=new Matrix(temp1);
			double[][] temp2={bridge_origin};	//bridge origin in GPS
			temp_bridge=new Matrix(temp2);
			computeIGMatrix();
			
			return (temp_point.times((BG.times(IG.inverse())).inverse())).plus((temp_origin.minus(temp_bridge)).times(BG.inverse())).getArray()[0];
	}

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

	double[] error_inspectorposition(int count) throws IOException
	{
		double[] scaling={0,0,0}, temp;
		temp=computeScalingFactors();
		for(int i=0;i<3;i++)
			scaling[i]=1/temp[i];
		
		double[] errorGPS={0,0,0};
		double distance_error=error_sampler(count);
	
		System.out.print(count);
		System.out.print(" ");
		System.out.println(distance_error);
		
		double angle=generator.nextDouble()*360.0;
		errorGPS[0]=distance_error*Math.cos(angle)*scaling[0];
		errorGPS[1]=distance_error*Math.sin(angle)*scaling[1];
		errorGPS[2]=0;	//altitude error is 0
		
		System.out.println(errorGPS[0] + " " + errorGPS[1]+ " " + errorGPS[2]);
		return errorGPS;
	}
	
	public static void main(String args[]) throws IOException
	{
		long start1, end1,start2, end2,start3, end3;
		int count_error=0;	//variable to keep track of error f
		String destination="/home/athuls89/Desktop/gsra/error_files/approximate/error_GPS_orient/case5/run10/file";
		String file_write;
		
		//Enter the GPS and bridge coordinates for 4 points on the bridge (used to determine the origin of the bridge
		double[] gpspoint1={-83.346745, 42.018465, 183.2};
		double[] gpspoint2={-83.346513, 42.018149, 183.2};
		double[] gpspoint3={-83.346513, 42.018149, 233.2};
		double[] gpspoint4={-83.345745, 42.018977, 183.2};
		double[] bfrpoint1={10, 0, 0};
		double[] bfrpoint2={50, 0, 0};
		double[] bfrpoint3={50, 0, 50};
		double[] bfrpoint4={0, 100, 0};
		
		double roll_val,pitch_val,yaw_val;
		
		Random gen_angle=new Random();
		
		start1=System.nanoTime();
		ApproximateErrorAnalysis sel=new ApproximateErrorAnalysis(gpspoint1, gpspoint2, gpspoint3, gpspoint4,bfrpoint1, bfrpoint2, bfrpoint3,bfrpoint4);
		end1=System.nanoTime();
		
//		start2=System.nanoTime();
		//Position of the inspector in GPS
		double[] inspector_position_func={0,0,0};
		
		double[] inspector_position={-83.346581, 42.018851, 178.5};	//this is in GPS
		int count=0;
		
		//error values
		double[] error_inspectororientation={0,0,0};
		double[] error_GPS;
		//Set orientation of the inspector 
		
		roll_val=0;pitch_val=6.9157;yaw_val=-208.3507;	//setting orientation values of inspector
		sel.setOrientation_inspector(roll_val,pitch_val,yaw_val);	//roll, pitch, yaw
		
		sel.setIFR_origin(inspector_position);
		
		//Set the region of interest (near and far plane distances) 
		//Compute bounding box parameters
		sel.setNPD_FPD(19.5168,39.0336);
		sel.computeBoundingBoxParameters();
		sel.setViewAngle(30);
		end2=System.nanoTime();
		
		//File handling section 
		while(count_error<sel.total_samples)
		{
			file_write=destination.concat(Integer.toString(count_error));
			
			error_GPS=sel.error_inspectorposition(count_error);
						
			for(int i=0;i<3;i++)
				inspector_position_func[i]=inspector_position[i]+error_GPS[i];
			
			for(int j=0;j<3;j++)
				error_inspectororientation[j]=gen_angle.nextDouble()-0.5;	
			
			System.out.println("hello "+roll_val+" "+pitch_val+" "+yaw_val);
			sel.setOrientation_inspector(roll_val+error_inspectororientation[0],pitch_val+error_inspectororientation[1],yaw_val+error_inspectororientation[2]);	//roll, pitch, yaw
			sel.setIFR_origin(inspector_position_func);
			
			sel.computeBoundingBoxParameters();	//added only for error analysis code
			
			BufferedReader in = new BufferedReader(new FileReader("/home/athuls89/Desktop/gsra/result_file"));
			BufferedWriter out = new BufferedWriter(new FileWriter(file_write));
			String str;
			Integer object_id;
			start3=System.nanoTime();

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
					sel.setQueryPoint(query1);
					if(sel.checkContainment())
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
			end3=System.nanoTime();
			//System.out.println(end3-start3);
			in.close();
			out.close();
			count_error++;
		}
		
	}
}