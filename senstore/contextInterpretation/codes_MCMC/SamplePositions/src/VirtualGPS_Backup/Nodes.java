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
import directBFRErrors.ConfigErrorAnalysis;

public class Nodes{
	double[] m_nodeCoord;
        int m_elementId;
        
	public Nodes(double x, double y, double z, int elementId)
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
