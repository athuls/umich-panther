package directBFRErrors;
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import approximateApproach.ComputeBoundingBox;
import approximateApproach.ComputeBoundingBoxInterface;
import approximateApproach.ApproximateAlgorithm;

import directBFRErrors.OptimizedThreadLauncher;
import directBFRErrors.PositionContext;
import directBFRErrors.MonteCarloSim;
import directBFRErrors.ErrorSampler;

import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

public class BFRErrorAnalysis 
{
	double range,discreteStep, lambda;
	int beta, total_samples; //used for error sampling
	Random generator;
	BufferedWriter out;
	
	public static void main(String args[]) throws IOException
	{
		//Convert everything to feet
		int countError, position, runNum;	
		String line;
		double[] npdInBFR, fpdInBFR;
		//error values
		double[] errorInspectorOrientation={0,0,0};
		double[] errorBFR;

		double[] observedInspectorBFR={0,0,0};
		double[] observedInspectorOrient={0,0,0};
	
		double[] simulatedInspectorBFR={0,0,0};
		double[] simulatedInspectorOrient={0,0,0};

		//File handling section 
		//For every position of inspection along trail on bridge
		
		//NOTE:CHECK IF RANDOM NUMBER SEED SHOULD BE RESET TO THE SAME VALUE FOR EACH RUN (FOR gps ERROR)
		for(runNum=53;runNum<54;runNum++){
			
			//Open file for reading
			BufferedReader in = new BufferedReader(new FileReader(ConfigErrorAnalysis.getAllPositions()));
			position=0;
			
			
			//while((line=in.readLine())!=null){	//iterate over all the 508 positions and orientations
				
				//Get all the inspector parameters in BFR from the file we are reading (like position in BFR, NPD and FPD in BFR)
				//Then call capturesNodesInBox to get all bounding box points
				//double[] trueInspectorBFR=new double[] {Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3])};
				//countError=0;
				//newSampler.resetNumCall();
				//PositionContext processPosition = new PositionContext(line,position,runNum); 
					
				//while(countError<sel.total_samples){
			
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
					//Get error in inspector position and orientation
					//errorBFR=newSampler.errorInspectorPosition(trueInspectorBFR[2]);			
					//errorInspectorOrientation=newSampler.errorInspectorOrient();
					/*errorBFR=new double[3];
					errorBFR[0]=0;
					errorBFR[1]=0;
					errorBFR[2]=0;*/

					//Determine the observed position/orientation of the inspector
					/*for(int i=0;i<trueInspectorBFR.length;i++){
						observedInspectorBFR[i]=trueInspectorBFR[i]+errorBFR[i];
						observedInspectorOrient[i]=Double.parseDouble(temp[i+4])+errorInspectorOrientation[i];					
					}*/
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
				//Call thread pool launcher to launch 'n' number of threads, each processing data for a true position.
				OptimizedThreadLauncher findPositionContext = new OptimizedThreadLauncher(in, 25, runNum);
				
				System.out.println("system time is "+System.currentTimeMillis());
					//Get Near and Far Plane centers using position and orientation of inspector
					/*NearAndFarPlanePoints inspectorParameters=new NearAndFarPlanePoints(observedInspectorBFR, observedInspectorOrient[0], observedInspectorOrient[1], observedInspectorOrient[2]);
					npdInBFR=inspectorParameters.getPlanePointInBFR(ConfigErrorAnalysis.getNpdFpd()[0]);
					fpdInBFR=inspectorParameters.getPlanePointInBFR(ConfigErrorAnalysis.getNpdFpd()[1]);
					
						//File handling and bounding box computation section 
						ComputeBoundingBoxInterface boundingBox=new ComputeBoundingBox(observedInspectorBFR, npdInBFR, fpdInBFR, ConfigErrorAnalysis.getViewAngle(), countError, position, runs);
						boundingBox.captureNodesInBox();
						countError++;
				}	*/
			//	position++;
			//	end_time=System.nanoTime();
			//	System.out.println("time taken is "+(start_time-end_time)+"\n");
			//}
		}
	}
}
