package virtualGps;
public interface InspectorParametersInterface {
	void computeBoundingBoxParameters(String[] splitLine);
	void computeBoundingBoxParameters(double[] splitLine);
	void getBFRCoordinates();
	void computeIGMatrix();
	
	double[] getInspectorOrientation();
	double[] getNearPlane();
	double[] getFarPlane();
	double[] getInspectorBFR();
	double getViewAngle();
}
