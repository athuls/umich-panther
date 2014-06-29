package approximateApproach;

public class Config {

	private static String currentPath = "/mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/";
	
	//Enter the GPS and bridge coordinates for 4 points on the bridge (used to determine the origin of the bridge
	// Note that all altitudes are in metres
	// Also note that the points are within 0.5 miles of origin GPS coordinates
	private static final double[] gpspoint1={116.40445611, 39.89816755, 54};
	private static final double[] gpspoint2={116.4142013, 39.90557888, 53.2};
	private static final double[] gpspoint3={116.4152891, 39.9066358, 55.9};
	private static final double[] gpspoint4={116.41051813, 39.89844792, 53.2};
	
	// Beijing coordinates
	public static final double[] OriginalGPSPoint = {116.40752599999996, 39.90403, 52.3};	

	// Distances are in feet
	public static final double bearing1 = 201.887;
	public static final double bearing2 = 73.169;
	public static final double bearing3 = 66.365;
	public static final double bearing4 = 157.646;
	
	/*private static final double[] bfrpoint1={210, 30, 0};
	private static final double[] bfrpoint2={0, 30, 0};
	private static final double[] bfrpoint3={0, -14, 0};
	private static final double[] bfrpoint4={210, 30, 0};
	*/
	
	//Position of the inspector in GPS
	//private static final double[] inspector_position={-83.346581, 42.018851, 178.5};	//this is in GPS
	
	//Orientation of the inspector in GPS
	//private static final double[] inspector_orientation={0,6.9157,-208.3507};	//roll, pitch, yaw
	
	//Near and Far plane distance for the inspector
	private static final double[] npd_fpd={3,100};	//npd, fpd
	
	//View angle for the inspector - run it for 40, 45, 50, 55
	private static final double viewAngle=30;
	
	//Input file with all nodes on the bridge (from database) and output file for storing points in bounding box
	private static final String m_allNodes = Config.currentPath + "bridge_model_sparse_62/final_merge.txt";
	private static final String m_allPositions = Config.currentPath + "field_inspector_trail/ApproxApproachAcrossPlanet/BeijingLocLinearInput";
	
	private static final String m_nodesAboveDeck=Config.currentPath + "bridge_model_sparse_62/above_deck";
	private static final String m_nodesBelowDeck=Config.currentPath + "bridge_model_sparse_62/below_deck";
	
	private static final String m_boundedNodes=Config.currentPath + "BFRErrorAnalysis/error_files/output";
	
	private static final String m_uniqueBoundedElements=Config.currentPath + "BFRErrorAnalysis/error_files/field_data/run2/";
	private static final String m_visibleElementsFile=Config.currentPath+"bridge_model_sparse_62/elementVisibility";

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
	
	/*
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
	*/	
	
}
