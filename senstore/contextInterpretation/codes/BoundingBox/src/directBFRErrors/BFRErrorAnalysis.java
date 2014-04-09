package directBFRErrors;
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import approximateApproach.ComputeBoundingBox;
import approximateApproach.ComputeBoundingBoxInterface;
import approximateApproach.ApproximateAlgorithm;

import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

public class BFRErrorAnalysis extends ApproximateAlgorithm 
{
	double range,discreteStep, lambda;
	int beta, total_samples; //used for error sampling
	Random generator;
	BufferedWriter out;
	
	public BFRErrorAnalysis(double[] pt1, double[] pt2, double[] pt3, double[] pt4, double[] pt5, double[] pt6, double[] pt7, double[] pt8) throws IOException
	{
		
		super(pt1,pt2,pt3,pt4, pt5, pt6, pt7, pt8);
		
		range=5;	//default is 5 metres	//Change this to 9 m & 0.15 m (i.e. 3/lambda)
		discreteStep=0.1;	//default=0.1 m and used 0.001 for small distances like range of 0.15m 
		lambda=(double)1/(double)1.5;	//default=1/1.5		//Change this to 3 m & 0.0375 m for next set of runs  
		beta=2;	//default=2
		total_samples=100;	//default=100
		generator=new Random();//initialize random number generator
	}
	
	
	double return_area(int i)
	{
		return Math.floor((total_samples*(CDF_Weibull2.w2cdf(lambda,beta,(discreteStep*(i+1))))));
	}
	
	double error_sampler(int numCall)
	{
		double sample_dist=100; //was initially 100 m, converted to feet
		double i1, i2;
		for(int i=0;i<range/discreteStep;i++)
		{
	
			if(i==0)
			{	i1=0;
				i2=return_area(0)-1;
			}
			else
			{	
				i1=return_area(i-1);
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

	double[] errorInspectorPosition(int count, double zValue) throws IOException
	{
	
		double[] errorGPS={0,0,0};
		
		double distance_error;
	
		if(zValue<ConfigErrorAnalysis.getDeckZPosition())
			distance_error=0.5;	//50 cm in feet
		else	
			distance_error=error_sampler(count);
	
		System.out.print(count);
		System.out.print(" ");
		System.out.println(distance_error);
		
		double angle=generator.nextDouble()*360.0;
	
		errorGPS[0]=distance_error*Math.cos(angle);
		errorGPS[1]=distance_error*Math.sin(angle);
		errorGPS[2]=0;	//altitude error is 0
		
		return errorGPS;
	}
	
	public static void main(String args[]) throws IOException
	{
		double start_time, end_time;
		
		//Convert everything to feet
		int countError, position, runs;	
		String line;
		double[] npdInBFR, fpdInBFR;
			
		BFRErrorAnalysis sel=new BFRErrorAnalysis(ConfigErrorAnalysis.getGPSPoint1(), ConfigErrorAnalysis.getGPSPoint2(), ConfigErrorAnalysis.getGPSPoint3(), ConfigErrorAnalysis.getGPSPoint4(), ConfigErrorAnalysis.getBFRPoint1(), ConfigErrorAnalysis.getBFRPoint2(), ConfigErrorAnalysis.getBFRPoint3(), ConfigErrorAnalysis.getBFRPoint4());

		//error values
		double[] errorInspectorOrientation={0,0,0};
		double[] newInspectorOrientation={0,0,0};

		double[] errorBFR;

		double[] inspectorBFR={0,0,0};

		//File handling section 
		//For every position of inspection along trail on bridge
		
		//NOTE:CHECK IF RANDOM NUMBER SEED SHOULD BE RESET TO THE SAME VALUE FOR EACH RUN (FOR gps ERROR)
		for(runs=50;runs<51;runs++){
			
			System.out.println("This is run "+runs);
			//Open file for reading true inspector position/orientation
			BufferedReader in = new BufferedReader(new FileReader(ConfigErrorAnalysis.getAllPositions()));
	
			position=0;
			
			while((line=in.readLine())!=null){	//iterate over all the 508 positions and orientations
				
				start_time=System.nanoTime();
				
				Random genAngle=new Random(5);
			
				countError=0;
				String[] temp=line.split("\\s+");
			
				//Open file to write all the error positions/orientations for 508 positions
				BufferedWriter error_positions=new BufferedWriter(new FileWriter("/mnt/old/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/error_positions/WAASGPS/position"+position));
		
				//Get all the inspector parameters in BFR from the file we are reading (like position in BFR, NPD and FPD in BFR)
				//Then call capturesNodesInBox to get all bounding box points
				double[] originalInspectorBFR=new double[] {Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3])};

				while(countError<sel.total_samples){
			
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
					//Get error in inspector position and orientation
					errorBFR=sel.errorInspectorPosition(countError, originalInspectorBFR[2]);			
					/*errorBFR=new double[3];
					errorBFR[0]=0;
					errorBFR[1]=0;
					errorBFR[2]=0;*/
			
					for(int i=0;i<originalInspectorBFR.length;i++)
						inspectorBFR[i]=originalInspectorBFR[i]+(errorBFR[i]/0.3);
					
					for(int j=0;j<3;j++)
					{
						errorInspectorOrientation[j]=genAngle.nextDouble()-0.5;
						newInspectorOrientation[j]=Double.parseDouble(temp[j+4])+errorInspectorOrientation[j];
					}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
				
					//Near plane distance = 3 feet, Far Plane distance =100 feet
					//Get Near and Far Plane centers using position and orientation of inspector
				//	NearAndFarPlanePoints inspectorParameters=new NearAndFarPlanePoints(inspectorBFR, Double.parseDouble(temp[4])+errorInspectorOrientation[0], Double.parseDouble(temp[5])+errorInspectorOrientation[1], Double.parseDouble(temp[6])+errorInspectorOrientation[2]);
				//	npdInBFR=inspectorParameters.getPlanePointInBFR(ConfigErrorAnalysis.getNpdFpd()[0]);
				//	fpdInBFR=inspectorParameters.getPlanePointInBFR(ConfigErrorAnalysis.getNpdFpd()[1]);
					
						//File handling and bounding box computation section 
				//		ComputeBoundingBoxInterface boundingBox=new ComputeBoundingBox(inspectorBFR, npdInBFR, fpdInBFR, ConfigErrorAnalysis.getViewAngle(), countError, position, runs);
				//		boundingBox.captureNodesInBox();
					 error_positions.write(inspectorBFR[0]+" "+inspectorBFR[1]+" "+inspectorBFR[2]+" "+newInspectorOrientation[0]+" "+newInspectorOrientation[1]+" "+newInspectorOrientation[2]+"\n");	
					 countError++;
				}	
				error_positions.close();
				position++;
				end_time=System.nanoTime();
				System.out.println("time taken is "+(start_time-end_time)+"\n");
			}
		}
	}
}
