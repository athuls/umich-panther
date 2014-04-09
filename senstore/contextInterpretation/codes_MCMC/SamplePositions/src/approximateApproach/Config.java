package approximateApproach;

public class Config {

	//Enter the GPS and bridge coordinates for 4 points on the bridge (used to determine the origin of the bridge
	private static final double[] gpspoint1={-83.346354, 42.01812367, 627};
	private static final double[] gpspoint2={-83.3467475, 42.0186215, 627};
	private static final double[] gpspoint3={-83.3468875, 42.01856267, 627};
	private static final double[] gpspoint4={-83.346354, 42.01812367, 633};
	private static final double[] bfrpoint1={210, 30, 27};
	private static final double[] bfrpoint2={0, 30, 27};
	private static final double[] bfrpoint3={0, -14, 27};
	private static final double[] bfrpoint4={210, 30, 33};
	
	//Position of the inspector in GPS
	//private static final double[] inspector_position={-83.346581, 42.018851, 178.5};	//this is in GPS
	
	//Orientation of the inspector in GPS
	//private static final double[] inspector_orientation={0,6.9157,-208.3507};	//roll, pitch, yaw
	
	//Near and Far plane distance for the inspector
	private static final double[] npd_fpd={3,100};	//npd, fpd
	
	//View angle for the inspector - run it for 40, 45, 50, 55
	private static final double viewAngle=30;
	
	//Input file with all nodes on the bridge (from database) and output file for storing points in bounding box
	private static final String m_allNodes="/mnt/sde/oldsystem/opt/senstore/contextInterpretation/bridge_model_sparse_62/final_merge.txt";
	private static final String m_allPositions="/mnt/sde/oldsystem/opt/senstore/contextInterpretation/field_inspector_trail/RTKGPS";
	
	private static final String m_nodesAboveDeck="/mnt/sde/oldsystem/opt/senstore/contextInterpretation/bridge_model_sparse_62/above_deck";
	private static final String m_nodesBelowDeck="/mnt/sde/oldsystem/opt/senstore/contextInterpretation/bridge_model_sparse_62/below_deck";
	
	private static final String m_boundedNodes="/mnt/sde/oldsystem/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/output";
	
	private static final String m_uniqueBoundedElements="/mnt/sde/oldsystem/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/field_data/run1/";
	private static final String m_visibleElementsFile="/mnt/sde/oldsystem/opt/senstore/contextInterpretation/bridge_model_sparse_62/elementVisibility";

	private static final double m_deckZPosition=19;	//19 
	
	public static double getDeckZPosition(){
		return m_deckZPosition;
	}	
	
	//Converts the view angle into radians and returns the angle in radians
	public static double getViewAngle(){
		return (Math.PI*(viewAngle/180));
	}
	
	public static String getAllNodes(){
		return m_allNodes;
	}
	
	//This is for reading in inspector field data
	public static String getAllPositions()
	{
		return m_allPositions;
	}
	
	public static String getNodesAboveDeck(){
		return m_nodesAboveDeck;
	}
	
	public static String getNodesBelowDeck(){
		return m_nodesBelowDeck;
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

	public static double[] getNpdFpd(){
		return npd_fpd;
	}	
	
	//We do not need these now because we are processing real field data which is in list form
	/*public static double[] getInspectorOrientation(){
		return inspector_orientation;
	}
	
	public static double[] getInspectorPosition(){
		return inspector_position;
	}*/
	
	public static double[] getGPSPoint1(){
		return gpspoint1;
	}
	
	public static double[] getGPSPoint2(){
		return gpspoint2;
	}
	
	public static double[] getGPSPoint3(){
		return gpspoint3;
	}
	
	public static double[] getGPSPoint4(){
		return gpspoint4;
	}
	
	public static double[] getBFRPoint1(){
		return bfrpoint1;
	}
	
	public static double[] getBFRPoint2(){
		return bfrpoint2;
	}
	
	public static double[] getBFRPoint3(){
		return bfrpoint3;
	}
	
	public static double[] getBFRPoint4(){
		return bfrpoint4;
	}
		
	
}
