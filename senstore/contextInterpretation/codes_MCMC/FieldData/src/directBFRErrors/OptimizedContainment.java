package directBFRErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Collections;

import Jama.Matrix;
import directBFRErrors.ConfigErrorAnalysis;
import approximateApproach.Nodes;

public class OptimizedContainment{ 
	
	private double[] m_queryPoint;
	private List<Nodes> m_nodesList;

	//Parameters used in the containment algorithm, their values can be set in the constructor and then used for every node query point
	private Matrix m_MI, m_nearPlanePoint, m_farPlanePoint, m_planeNormal;
	private double m_distanceToNearPoint, m_distanceToFarPoint, m_viewAngle, m_trueInspectorZPos;	
	private String m_outputNodeFile, m_outputElementFile;	
	
	private Hashtable<Integer, List<Nodes> > m_nodesToScan;
	//static private List<String> nodesAboveDeck;
	//static private List<String> nodesBelowDeck;
	static private List<Nodes> elementsWithNodesAboveDeck;
	static private List<Nodes> elementsWithNodesBelowDeck;
			
	static{
		String str;
		BufferedReader fileNodesAboveDeck;
		BufferedReader fileNodesBelowDeck;
		//nodesAboveDeck=new ArrayList<String> ();
		//nodesBelowDeck=new ArrayList<String> ();
		elementsWithNodesAboveDeck = new ArrayList<Nodes> ();
		elementsWithNodesBelowDeck = new ArrayList<Nodes> ();

		System.out.println("I am in static of ComputeBoundingBox and doing IO");
		try {
			fileNodesAboveDeck = new BufferedReader(new FileReader(ConfigErrorAnalysis.getNodesAboveDeck()));
			fileNodesBelowDeck = new BufferedReader(new FileReader(ConfigErrorAnalysis.getNodesBelowDeck()));
			
			//Iterate over nodes above deck and add to nodesAboveDeck list
			while((str=fileNodesAboveDeck.readLine())!=null){
					
				String[] temp=str.split("\\s+");
				int element_id=Integer.parseInt(temp[temp.length-1]);
				
				//Store the element ID along with (x,y,z) coordinates of all nodes that constitute the element in elementsWithNodesAboveDeck list
				elementsWithNodesAboveDeck.add(new Nodes(Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3]), element_id));
			}
			
			//Iterate over nodes below deck and add to nodesBelowDeck list
			while((str=fileNodesBelowDeck.readLine())!=null){
				
				String[] temp=str.split("\\s+");
				int element_id=Integer.parseInt(temp[temp.length-1]);
					
				//Store the element ID along with (x,y,z) coordinates of all nodes that constitute the element in elementsWithNodesBelowDeck list
				elementsWithNodesBelowDeck.add(new Nodes(Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3]), element_id));
			}
			
			fileNodesAboveDeck.close();
			fileNodesBelowDeck.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//This is constructor invoked if there are 100 simulated positions and we need to determine containment for each of them (that's why we pass nodesList)
	public OptimizedContainment(Hashtable<Integer, List<Nodes> > nodesList, double[] inspectorPos, double[] nearPlane, double[] farPlane, double viewAngle, int truePos, int observedPos, int sampleNum, int runNum) {
		m_nodesToScan = nodesList;
		m_MI=new Matrix(new double[][] {inspectorPos});
		m_nearPlanePoint=new Matrix(new double[][] {nearPlane});
		m_farPlanePoint=new Matrix(new double[][] {farPlane});
		m_viewAngle = viewAngle;	

		m_distanceToNearPoint=m_MI.minus(m_nearPlanePoint).normF();
		m_distanceToFarPoint=m_MI.minus(m_farPlanePoint).normF();
		m_planeNormal=(m_farPlanePoint.minus(m_MI)).times((double)((double)1/(double)m_distanceToFarPoint));
		
		m_outputElementFile = ConfigErrorAnalysis.getUniqueBoundedElements().concat(Integer.toString(runNum)+"/position"+Integer.toString(truePos)+"/observedPos"+Integer.toString(observedPos)+"/"+Integer.toString(sampleNum));

	}

	//This is for determining containment in enclosing cone for a given true position (optimization for simulation)
	public OptimizedContainment(Matrix inspectorPos, Matrix nearPlane, Matrix farPlane, Matrix inspPlaneNormal, double trueInspZPos, double viewAngle) {

		m_MI=inspectorPos;
		m_nearPlanePoint=nearPlane;
		m_farPlanePoint=farPlane;
		m_viewAngle = viewAngle;	
		m_planeNormal=inspPlaneNormal;
		m_trueInspectorZPos = trueInspZPos;

		m_distanceToNearPoint=m_MI.minus(m_nearPlanePoint).normF();
		m_distanceToFarPoint=m_MI.minus(m_farPlanePoint).normF();
	}

	//This is for determining containment using a mixed cone representative of true position
	public OptimizedContainment(Matrix inspectorPos, Matrix farPlane, Matrix nearPlane, double viewAngle, int runNum, int truePosNum, int observedPosNum) {

		m_MI=inspectorPos;
		m_nearPlanePoint=nearPlane;
		m_farPlanePoint=farPlane;
		m_viewAngle = viewAngle;	
		m_trueInspectorZPos = m_MI.get(0,2);
		m_distanceToNearPoint=m_MI.minus(m_nearPlanePoint).normF();
		m_distanceToFarPoint=m_MI.minus(m_farPlanePoint).normF();
		m_planeNormal = (m_farPlanePoint.minus(m_MI)).times((double)((double)1/(double)m_distanceToFarPoint));
		m_outputElementFile = ConfigErrorAnalysis.getUniqueBoundedElements().concat(Integer.toString(runNum)+"/position"+Integer.toString(truePosNum)+"/0");
	}
		
	
	public void getContainedNodesInMeanCone() throws IOException
	{
		int element_id;

		List<Integer> elementsIdentified = new ArrayList<Integer>();
	
		//Prepare file for writing contextual elements
		BufferedWriter out_unique_elements=new BufferedWriter(new FileWriter(m_outputElementFile));

                if(m_trueInspectorZPos > (ConfigErrorAnalysis.getDeckZPosition()))
                        m_nodesList=elementsWithNodesAboveDeck;
                else
                        m_nodesList=elementsWithNodesBelowDeck;

                for(int i=0;i<m_nodesList.size();i++)
                {
                                element_id=m_nodesList.get(i).getElementId();
                                m_queryPoint=m_nodesList.get(i).getCoordinates();
					
				//If the element with element_id has been identified as contextual then continue
				if(Collections.binarySearch(elementsIdentified,element_id) >= 0)
					continue;
				
                                //Just store the elements in context
                                if(checkContainment())
                                {     
					out_unique_elements.write(element_id+"\n"); 
					elementsIdentified.add(element_id);
					Collections.sort(elementsIdentified);
				}
                }
		out_unique_elements.close();
	}

	public void getContainedNodes() throws IOException
	{
		int element_id;

                //hashElements was used to store number of nodes in context for each element
                //We want to just store the number of elemnets in context
		Hashtable<Integer, Boolean> contextualElements = new Hashtable<Integer, Boolean>();		
		BufferedWriter out_unique_elements = new BufferedWriter(new FileWriter(m_outputElementFile));
	
                if(m_trueInspectorZPos > (ConfigErrorAnalysis.getDeckZPosition()))
                {        m_nodesList=elementsWithNodesAboveDeck;
			System.out.println("I am above deck");
		}
                else
                {        m_nodesList=elementsWithNodesBelowDeck;
			System.out.println("I am above deck");
		}

                for(int i=0;i<m_nodesList.size();i++)
                {
                                element_id=m_nodesList.get(i).getElementId();
                                m_queryPoint=m_nodesList.get(i).getCoordinates();

                                //This snippet was used to store elements with number of nodes in context
                                /*if(hashElements.containsKey(element_id) || checkContainment())
                                {
                                                if(hashElements.containsKey(element_id))
                                                        hashElements.put(element_id, (hashElements.get(element_id)+1));         
                                                else
                                                        hashElements.put(element_id, 1);
                                                
                                }*/
                                //Just store the elements in context
				if(contextualElements.containsKey(element_id))
					continue; 
                                if(checkContainment())
                                {      
					System.out.println("Element found is "+element_id);
					contextualElements.put(element_id, true);
					out_unique_elements.write(element_id + "\n");
				}
                }
		out_unique_elements.close();
	}
	
	//Iterate through the elements of m_nodesToScan, and for each element, go through the nodes of the element. If we find a node that falls in context, we move onto check the next element
	public void captureNodesInBox() throws IOException {
			
		BufferedWriter out_unique_elements=new BufferedWriter(new FileWriter(m_outputElementFile));

		for(Enumeration<Integer> e=m_nodesToScan.keys();e.hasMoreElements();){
                        Integer currElement=e.nextElement();
		
			//Now iterate through the node list for currElement element	
			List<Nodes> currNodeList = new ArrayList<Nodes> ((ArrayList<Nodes>)m_nodesToScan.get(currElement));
			for(int i=0;i<currNodeList.size();i++)
			{
				m_queryPoint = currNodeList.get(i).getCoordinates();	
				if(checkContainment())
				{
                        		out_unique_elements.write(currElement+"\n");
					break;
				}
			}
                }
		out_unique_elements.close();
	}

	public boolean checkContainment() {
		
		//check for containment of query_point in the bounding box
			double phi, phiAngle;
			
			//All points are now in BFR
			Matrix M_P=new Matrix(new double[][] {m_queryPoint});
			
			//This condition checks for the query node being in front of the inspector plane (plane that is defined by the inspectors position and line of sight)	
			if((M_P.minus(m_MI).get(0,0)*m_planeNormal.get(0,0)+M_P.minus(m_MI).get(0,1)*m_planeNormal.get(0,1)+M_P.minus(m_MI).get(0,2)*m_planeNormal.get(0,2)) > 0)
			{
				phi=(((M_P.minus(m_MI).get(0,0))*(m_nearPlanePoint.minus(m_MI).get(0,0))) + ((M_P.minus(m_MI).get(0,1))*(m_nearPlanePoint.minus(m_MI).get(0,1))) + ((M_P.minus(m_MI).get(0,2))*(m_nearPlanePoint.minus(m_MI).get(0,2))))/((M_P.minus(m_MI)).normF()*m_distanceToNearPoint);
				
				phiAngle=Math.acos(phi);
	
				//Potentially superfluous condition!!!
				if(phiAngle>(Math.PI/2))
					phiAngle=Math.PI-phiAngle;
				
				double cosOfPhi=Math.cos(phiAngle);
					
				if(phiAngle<=m_viewAngle && m_distanceToNearPoint<=(((m_MI.minus(M_P)).normF())*cosOfPhi) && ((m_MI.minus(M_P).normF())*cosOfPhi)<=m_distanceToFarPoint)
					return true;
				else
					return false;
			}
			else
				return false;
	}	
	
}
