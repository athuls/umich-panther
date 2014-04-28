
/*Given 4 points on the bridge, determine the origin of the bridge. Further given any region of interest
 * by the inspector, return all the nodes within the bounding box/region of interest*/
package approximateApproach;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GeodeticMeasurement;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.gavaghan.geodesy.GlobalPosition;

import java.util.LinkedList;
import java.util.Collections;
//import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.Object;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.io.ByteArrayOutputStream;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import Jama.*;

public class Calibrator 
{
	public static double[] computeIO_BFR(double[] gpsPoint1, double[] gpsPoint2, double azimuth)
	{
		// instantiate the calculator
		GeodeticCalculator geoCalc = new GeodeticCalculator();
		// select a reference ellipsoid
		Ellipsoid reference = Ellipsoid.WGS84;
		// set Pike's Peak position
		GlobalPosition BO;
		BO = new GlobalPosition(gpsPoint1[1], gpsPoint1[0], gpsPoint1[2]);

		// set Alcatraz Island coordinates
		GlobalPosition IO;
		IO = new GlobalPosition(gpsPoint2[1], gpsPoint2[0], gpsPoint2[2]);

		// calculate the geodetic measurement
		GeodeticMeasurement geoMeasurement;
		double p2pmeters;
		double elevChangeMeters;

		geoMeasurement = geoCalc.calculateGeodeticMeasurement(reference, BO, IO);
		
		p2pmeters = geoMeasurement.getPointToPointDistance();
		elevChangeMeters = geoMeasurement.getElevationChange();
		System.out.println(elevChangeMeters);
		azimuth=geoMeasurement.getAzimuth();

		double angle_p=Math.asin(elevChangeMeters/p2pmeters);

		double[] inspector_BFR=new double[3];
		inspector_BFR[0]=p2pmeters*Math.cos(angle_p)*Math.sin(Math.PI*(azimuth)/180);
		System.out.println(inspector_BFR[0]);
		inspector_BFR[1]=p2pmeters*Math.cos(angle_p)*Math.cos(Math.PI*(azimuth)/180);
		System.out.println(inspector_BFR[1]);
		inspector_BFR[2]=elevChangeMeters;
		System.out.println(inspector_BFR[2]);
		
		return inspector_BFR;		
	}
}
