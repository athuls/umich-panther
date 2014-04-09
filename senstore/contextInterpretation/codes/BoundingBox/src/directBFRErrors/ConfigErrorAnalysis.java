package directBFRErrors;
import approximateApproach.*;

public class ConfigErrorAnalysis extends Config{
	
	//Input file with all nodes on the bridge (from database) and output file for storing points in bounding box
	private static final String m_allPositions="/mnt/old/opt/senstore/contextInterpretation/bridge_model_sparse_62/inspector_trail";
	private static final String m_boundedNodes="/opt/senstore/contextInterpretation/BFRErrorAnalysis/test_output/output";
	
	private static final String m_uniqueBoundedElements="/mnt/old/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/error_positions/RTKGPS";
	private static final String m_visibleElementsFile="/opt/senstore/contextInterpretation/bridge_model_sparse_62/elementVisibility";
	
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
		
	
}
