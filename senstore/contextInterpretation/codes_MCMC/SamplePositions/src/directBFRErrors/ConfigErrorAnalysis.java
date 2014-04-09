package directBFRErrors;
import approximateApproach.*;

public class ConfigErrorAnalysis extends Config{
	
	//Input file with all nodes on the bridge (from database) and output file for storing points in bounding box
	private static final String m_allPositions="/opt/senstore/contextInterpretation/bridge_model_sparse_62/inspector_trail";
	private static final String m_boundedNodes="/opt/senstore/contextInterpretation/BFRErrorAnalysis/test_output/output";
	
	private static final String m_uniqueBoundedElements="/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/field_data/run";
	private static final String m_visibleElementsFile="/opt/senstore/contextInterpretation/bridge_model_sparse_62/elementVisibility";

	private static final double m_range = 5;
	private static final double m_discreteStep = 0.1;
	private static final double m_lambda = (double)1/(double)1.5;
	private static final int m_beta = 2;
	//Gives the maximum orientation error of the inspector (in one direction)
	private static final double m_orientErrorMax = 0.5;
		
	//Number of samples for observed positions and their simulated positions
	private static final int m_obsPos = 100;	
	private static final int m_simPos = 10;	
	
	private static final double m_errorRangeBelowBridge = 0.5;

	public static double getErrorRangeBelowBridge(){
		return m_errorRangeBelowBridge;
	}
	
	public static double getMaxAngleError()
	{
		return m_orientErrorMax;		
	}
			
	public static String getAllPositions(){
		return m_allPositions;
	}
	
	public static String getBoundedNodes(){
		return m_boundedNodes;
	}
	
	public static String getUniqueBoundedElements(){
		return m_uniqueBoundedElements;
	}
	
	public static String getVisibleElementsFile(){
		return m_visibleElementsFile;
	}
		
	public static double getErrorRange(){
		return m_range;
	}
		
	public static double getErrorDiscreteSteps(){
		return m_discreteStep;
	}
		
	public static double getErrorLambda(){
		return m_lambda;
	}
		
	public static int getErrorBeta(){
		return m_beta;
	}

	public static int getNumObsPos(){
		return m_obsPos;
	}

	public static int getNumSimPos(){
		return m_simPos;
	}
}
	
