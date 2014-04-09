package approximateApproach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import Jama.Matrix;

public class ComputeBoundingBox implements ComputeBoundingBoxInterface{
	
	static private class Node{
		double[] m_nodeCoord;
		int m_elementId;
		public Node(double x, double y, double z, int elementId)
		{	
			m_nodeCoord=new double[3];	
			m_nodeCoord[0]=x;
			m_nodeCoord[1]=y;
			m_nodeCoord[2]=z;
			m_elementId=elementId;
		}
		
		//Class member access functions
		public double[] getCoordinates()
		{
			return m_nodeCoord;
		}
		public int getElementId()
		{
			return m_elementId;
		}
	}
	
	private double[] m_queryPoint;
	private double[] m_inspectorBFR;
	private double[] m_nearPlane;
	private double[] m_farPlane;
	private double m_viewAngle;
	private String m_outputNodeFile, m_outputElementFile;
	private List<Node> nodesList;

	//Parameters used in the containment algorithm, their values can be set in the constructor and then used for every node query point
	private Matrix M_I, M_F1, M_F2, planeNormal;
	private double distanceToNearPoint, distanceToFarPoint;	
		
	static private Hashtable<Integer, Integer> elementNodeCount;
	static private List<Node> elementsWithNodesAboveDeck;
	static private List<Node> elementsWithNodesBelowDeck;
			
	static{
		String str;
		BufferedReader fileNodesAboveDeck;
		BufferedReader fileNodesBelowDeck;
		BufferedReader fileVisibleEl;
		//nodesAboveDeck=new ArrayList<String> ();
		//nodesBelowDeck=new ArrayList<String> ();
		elementsWithNodesAboveDeck = new ArrayList<Node> ();
		elementsWithNodesBelowDeck = new ArrayList<Node> ();

		elementNodeCount=new Hashtable<Integer, Integer>();
		
		System.out.println("I am in static of ComputeBoundingBox and doing IO");
		try {
			fileNodesAboveDeck = new BufferedReader(new FileReader(Config.getNodesAboveDeck()));
			fileNodesBelowDeck = new BufferedReader(new FileReader(Config.getNodesBelowDeck()));
			fileVisibleEl = new BufferedReader(new FileReader(Config.getVisibleElementsFile()));
			
			//Iterate over nodes above deck and add to nodesAboveDeck list
			while((str=fileNodesAboveDeck.readLine())!=null){
					
				String[] temp=str.split("\\s+");
				int element_id=Integer.parseInt(temp[temp.length-1]);
				
				//Store the element ID along with (x,y,z) coordinates of all nodes that constitute the element in elementsWithNodesAboveDeck list
				elementsWithNodesAboveDeck.add(new Node(Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3]), element_id));
	
				if(elementNodeCount.containsKey(element_id))
					elementNodeCount.put(element_id, (elementNodeCount.get(element_id)+1));
				else
					elementNodeCount.put(element_id, 1);
			}
			
			//Iterate over nodes below deck and add to nodesBelowDeck list
			while((str=fileNodesBelowDeck.readLine())!=null){
				
				String[] temp=str.split("\\s+");
				int element_id=Integer.parseInt(temp[temp.length-1]);
					
				//Store the element ID along with (x,y,z) coordinates of all nodes that constitute the element in elementsWithNodesBelowDeck list
				elementsWithNodesBelowDeck.add(new Node(Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3]), element_id));

				if(elementNodeCount.containsKey(element_id))
					elementNodeCount.put(element_id, (elementNodeCount.get(element_id)+1));
				else
					elementNodeCount.put(element_id, 1);
			}
			
			fileNodesAboveDeck.close();
			fileNodesBelowDeck.close();
			fileVisibleEl.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//This constructor was last used for real field data
	public ComputeBoundingBox(double[] inspectorBFR, double[] nearPlane, double[] farPlane, double viewAngle, int position) {
		m_inspectorBFR=inspectorBFR;
		m_nearPlane=nearPlane;
		m_farPlane=farPlane;
		m_viewAngle=viewAngle;

		M_I=new Matrix(new double[][] {m_inspectorBFR});
		M_F1=new Matrix(new double[][] {m_nearPlane});
		M_F2=new Matrix(new double[][] {m_farPlane});	
		planeNormal=new Matrix(1,3);
                for(int i=0;i<3;i++)
                	planeNormal.set(0,i,((M_F2.minus(M_I).get(0,i))/((M_F2.minus(M_I)).normF())));
		distanceToNearPoint=M_I.minus(M_F1).normF();
		distanceToFarPoint=M_I.minus(M_F2).normF();

		m_outputNodeFile=Config.getBoundedNodes();
		m_outputElementFile=Config.getUniqueBoundedElements().concat("position"+Integer.toString(position)+"/0");
	}
		
	public ComputeBoundingBox(double[] inspectorBFR, double[] nearPlane, double[] farPlane, double viewAngle, int truePosition, int observedPosition, int sampleNum,int runCount) {
		
		m_inspectorBFR=inspectorBFR;
		m_nearPlane=nearPlane;
		m_farPlane=farPlane;
		m_viewAngle=viewAngle;
		
		M_I=new Matrix(new double[][] {m_inspectorBFR});
		M_F1=new Matrix(new double[][] {m_nearPlane});
		M_F2=new Matrix(new double[][] {m_farPlane});	
		planeNormal=new Matrix(1,3);
                for(int i=0;i<3;i++)
                	planeNormal.set(0,i,((M_F2.minus(M_I).get(0,i))/((M_F2.minus(M_I)).normF())));
		distanceToNearPoint=M_I.minus(M_F1).normF();
		distanceToFarPoint=M_I.minus(M_F2).normF();
		
		m_outputNodeFile=Config.getBoundedNodes().concat(Integer.toString(runCount)+"/position"+Integer.toString(truePosition)+"/observedPos"+Integer.toString(observedPosition)+"/"+Integer.toString(sampleNum));
		m_outputElementFile=Config.getUniqueBoundedElements().concat(Integer.toString(runCount)+"/position"+Integer.toString(truePosition)+"/observedPos"+Integer.toString(observedPosition)+"/"+Integer.toString(sampleNum));
	}
	
	@Override
	public void captureNodesInBox() throws IOException {
		int element_id;
		
		//hashElements was used to store number of nodes in context for each element
		//Hashtable<Integer, Integer> hashElements=new Hashtable<Integer, Integer>();
		//We want to just store the number of elemnets in context
		Hashtable<Integer,Boolean> contextualElements=new Hashtable<Integer,Boolean>();
		
		BufferedWriter out_unique_elements=new BufferedWriter(new FileWriter(m_outputElementFile));
		
		if(m_inspectorBFR[2] > (Config.getDeckZPosition()))
			nodesList=elementsWithNodesAboveDeck;
		else
			nodesList=elementsWithNodesBelowDeck;
			
		for(int i=0;i<nodesList.size();i++)
		{		
				element_id=nodesList.get(i).getElementId();			
				m_queryPoint=nodesList.get(i).getCoordinates();	
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
				else if(checkContainment())
				{	contextualElements.put(element_id, true);
					out_unique_elements.write(element_id+"\n");
				}
			
		}
		
		//Loop to write compute fraction of nodes for each contextual element and write it to file	
		/*	
		for(Enumeration<Integer> e=hashElements.keys();e.hasMoreElements();){
			Integer tempKey=e.nextElement();
			out_unique_elements.write(tempKey+" "+hashElements.get(tempKey)+" "+(double)((double)hashElements.get(tempKey)/(double)elementNodeCount.get(tempKey))+"\n");
		}
		*/
		out_unique_elements.close();
	}

	@Override
	public boolean checkContainment() {
		
		//check for containment of query_point in the bounding box
			double phi, phiAngle;
			
			//All points are now in BFR
			Matrix M_P=new Matrix(new double[][] {m_queryPoint});
			
			//This condition checks for the query node being in front of the inspector plane (plane that is defined by the inspectors position and line of sight)	
			if((M_P.minus(M_I).get(0,0)*planeNormal.get(0,0)+M_P.minus(M_I).get(0,1)*planeNormal.get(0,1)+M_P.minus(M_I).get(0,2)*planeNormal.get(0,2)) > 0)
			{
				phi=(((M_P.minus(M_I).get(0,0))*(M_F1.minus(M_I).get(0,0))) + ((M_P.minus(M_I).get(0,1))*(M_F1.minus(M_I).get(0,1))) + ((M_P.minus(M_I).get(0,2))*(M_F1.minus(M_I).get(0,2))))/((M_P.minus(M_I)).normF()*distanceToNearPoint);
				
				phiAngle=Math.acos(phi);
	
				//Potentially superfluous condition!!!
				if(phiAngle>(Math.PI/2))
					phiAngle=Math.PI-phiAngle;
				
				double cosOfPhi=Math.cos(phiAngle);
					
				if(phiAngle<=m_viewAngle && distanceToNearPoint<=(((M_I.minus(M_P)).normF())*cosOfPhi) && ((M_I.minus(M_P).normF())*cosOfPhi)<=distanceToFarPoint)
					return true;
				else
					return false;
			}
			else
				return false;
	}	
	
}
