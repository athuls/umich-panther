package approximateApproach;

public interface InspectorParametersInterface {
	void computeBoundingBoxParameters(String[] splitLine);
	void getBFRCoordinates();
	void computeIGMatrix();
	
	double[] getInspectorOrientation();
	double[] getNearPlane();
	double[] getFarPlane();
	double[] getInspectorBFR();
	double getViewAngle();
}
