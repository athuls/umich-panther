package directBFRErrors;

import java.lang.Thread;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import approximateApproach.ComputeBoundingBox;
import approximateApproach.ComputeBoundingBoxInterface;
import approximateApproach.ApproximateAlgorithm;

import java.io.IOException;
import java.util.Random;

import WeibullCDF.CDF_Weibull2;

public class ErrorSampler
{
	double m_range, m_errorRangeBelowBridge, m_discreteStep, m_lambda, m_maxAngleError;
	int m_beta, m_totalSamples, m_numCall;
	Random m_positionGenerator, m_orientGenerator, m_intGenerator;
	
	public ErrorSampler(double range, double discreteStep, double lambda, int beta, int total_samples, int seedOrient, double maxAngleError) throws IOException
	{
		m_range=range;	//default is 5 metres or 0.15 metres	//Change this to 9 m & 0.15 m (i.e. 3/lambda)
		m_discreteStep=discreteStep;	//default=0.1 m and used 0.001 for small distances like range of 0.15m 
		m_lambda=lambda;	//default=1/1.5		//Change this to 3 m & 0.0375 m for next set of runs  
		m_beta=beta;	//default=2
		m_totalSamples=total_samples;	//default=100
		m_numCall=0;
		m_maxAngleError=maxAngleError;
		m_errorRangeBelowBridge = ConfigErrorAnalysis.getErrorRangeBelowBridge();
		m_positionGenerator=new Random();//initialize random number generator
		m_orientGenerator=new Random(seedOrient);
	}

	
	public ErrorSampler(double range, double discreteStep, double lambda, int beta, int total_samples, int seedPosition, int seedOrient, double maxAngleError) throws IOException
	{
		m_range=range;	//default is 5 metres or 0.15 metres	//Change this to 9 m & 0.15 m (i.e. 3/lambda)
		m_discreteStep=discreteStep;	//default=0.1 m and used 0.001 for small distances like range of 0.15m 
		m_lambda=lambda;	//default=1/1.5		//Change this to 3 m & 0.0375 m for next set of runs  
		m_beta=beta;	//default=2
		m_totalSamples=total_samples;	//default=100
		m_numCall=0;
		m_maxAngleError=maxAngleError;
		m_errorRangeBelowBridge = ConfigErrorAnalysis.getErrorRangeBelowBridge();
		m_positionGenerator=new Random(seedPosition);//initialize random number generator
		m_orientGenerator=new Random(seedOrient);
		
		m_intGenerator = new Random(seedPosition);
	}
	
	public void resetNumCall()
	{
		m_numCall=0;
	}
	
	public double return_area(int i)
	{
		return Math.floor((m_totalSamples*(CDF_Weibull2.w2cdf(m_lambda,m_beta,(m_discreteStep*(i+1))))));
	}
	
	public double error_sampler(int numCall)
	{
		double sample_dist=100; 
		double i1, i2;
		for(int i=0;i<m_range/m_discreteStep;i++)
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
					sample_dist=(i*m_discreteStep)+(m_discreteStep/2);
					break;
				}
			}
			else
			{
				if(numCall>=i2 && numCall<=i1)
				{
					sample_dist=(i*m_discreteStep)+(m_discreteStep/2);
					break;
				}
			}
		}
		return sample_dist;
	}

	public double[] errorInspectorPosition(double zValue) throws IOException
	{
	
		double[] errorGPS={0,0,0};
		
		double distance_error;
	
		if(zValue<ConfigErrorAnalysis.getDeckZPosition())
			distance_error=m_errorRangeBelowBridge;	//50 cm in feet
		else	
			distance_error=error_sampler(m_numCall);
		
		System.out.println("error is "+distance_error);
		
		double angle=m_positionGenerator.nextDouble()*360.0;
	
		errorGPS[0]=distance_error*Math.cos(angle);
		errorGPS[1]=distance_error*Math.sin(angle);
		errorGPS[2]=0;	//altitude error is 0
		
		//convert the error distances to metres from feet
		for(int i=0;i<3;i++)
			errorGPS[i]=(double)((double)errorGPS[i]/(double)0.3);	
		
		m_numCall++;
		
		return errorGPS;
	}
		
	public double[] errorInspectorPosition(int numCall, double zValue) throws IOException
	{
	
		double[] errorGPS={0,0,0};
		
		double distance_error;
	
		if(zValue<ConfigErrorAnalysis.getDeckZPosition())
			distance_error=m_errorRangeBelowBridge;	//50 cm in feet
		else	
			distance_error=error_sampler(numCall);
	
		System.out.println("error is "+distance_error);
		double angle=m_positionGenerator.nextDouble()*360.0;
	
		errorGPS[0]=distance_error*Math.cos(angle);
		errorGPS[1]=distance_error*Math.sin(angle);
		errorGPS[2]=0;	//altitude error is 0
		
		//convert the error distances to metres from feet
		for(int i=0;i<3;i++)
			errorGPS[i]=(double)((double)errorGPS[i]/(double)0.3);	
		
		return errorGPS;
	}

	public double[] errorInspectorOrient()
	{
		double[] retOrientError={0,0,0};
		
		for(int i=0;i<3;i++)	
			retOrientError[i]=m_orientGenerator.nextDouble()-m_maxAngleError;

		return retOrientError;		
	}
	
	public int generateRandomInt(int start, int end)
	{
		
		long range = end-start+1;
		long fraction = (long)(range * m_intGenerator.nextDouble());
		int randomNumber = (int) (fraction+start);
		return randomNumber;
	}
}	
