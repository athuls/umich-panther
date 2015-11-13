package virtualGps;
import Jama.Matrix;
public interface InspectorParametersInterface {
	void computeBoundingBoxParameters(String[] splitLine);
	void computeBoundingBoxParameters(double[] splitLine);
	void getBFRCoordinates();
	void computeIGMatrix();
	double[] getCorrespondingGPS(Matrix m);
	
	double[] getInspectorOrientation();
	double[] getNearPlane();
	double[] getFarPlane();
	double[] getInspectorBFR();
	double getViewAngle();
}
