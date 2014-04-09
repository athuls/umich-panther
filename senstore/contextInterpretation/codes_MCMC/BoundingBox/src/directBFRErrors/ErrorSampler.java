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
	double m_range, m_discreteStep, m_lambda;
	int m_beta, m_totalSamples, m_numCall;
	Random m_positionGenerator, m_orientGenerator;
	
	public ErrorSampler(double range, double discreteStep, double lambda, int beta, int total_samples, int seedOrient) throws IOException
	{
		m_range=range;	//default is 5 metres or 0.15 metres	//Change this to 9 m & 0.15 m (i.e. 3/lambda)
		m_discreteStep=discreteStep;	//default=0.1 m and used 0.001 for small distances like range of 0.15m 
		m_lambda=lambda;	//default=1/1.5		//Change this to 3 m & 0.0375 m for next set of runs  
		m_beta=beta;	//default=2
		m_totalSamples=total_samples;	//default=100
		m_numCall=0;
		m_positionGenerator=new Random();//initialize random number generator
		m_orientGenerator=new Random(seedOrient);
	}

	
	public ErrorSampler(double range, double discreteStep, double lambda, int beta, int total_samples, int seedPosition, int seedOrient) throws IOException
	{
		m_range=range;	//default is 5 metres or 0.15 metres	//Change this to 9 m & 0.15 m (i.e. 3/lambda)
		m_discreteStep=discreteStep;	//default=0.1 m and used 0.001 for small distances like range of 0.15m 
		m_lambda=lambda;	//default=1/1.5		//Change this to 3 m & 0.0375 m for next set of runs  
		m_beta=beta;	//default=2
		m_totalSamples=total_samples;	//default=100
		m_numCall=0;
		m_positionGenerator=new Random(seedPosition);//initialize random number generator
		m_orientGenerator=new Random(seedOrient);
	}
	
	public ErrorSampler(double range, double discreteStep, double lambda, int beta, int total_samples) throws IOException, InterruptedException
	{
		m_range=range;	//default is 5 metres or 0.15 metres	//Change this to 9 m & 0.15 m (i.e. 3/lambda)
		m_discreteStep=discreteStep;	//default=0.1 m and used 0.001 for small distances like range of 0.15m 
		m_lambda=lambda;	//default=1/1.5		//Change this to 3 m & 0.0375 m for next set of runs  
		m_beta=beta;	//default=2
		m_totalSamples=total_samples;	//default=100
		m_numCall=0;
		Thread.currentThread().sleep(5);
		m_positionGenerator=new Random();//initialize random number generator
		Thread.currentThread().sleep(5);
		m_orientGenerator=new Random();
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
		double sample_dist=100; //was initially 100 m, converted to feet
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
			distance_error=0.5;	//50 cm in feet
		else	
			distance_error=error_sampler(m_numCall);
	
		System.out.print(m_numCall);
		System.out.print(" ");
		System.out.println(distance_error);
		
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
			distance_error=0.5;	//50 cm in feet
		else	
			distance_error=error_sampler(numCall);
	
		System.out.print(numCall);
		System.out.print(" ");
		System.out.println(distance_error);
		
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
			retOrientError[i]=m_orientGenerator.nextDouble()-0.5;

		return retOrientError;		
	}
}
