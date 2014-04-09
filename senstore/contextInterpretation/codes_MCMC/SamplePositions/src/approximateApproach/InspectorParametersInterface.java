package approximateApproach;

public interface InspectorParametersInterface {
	void computeBoundingBoxParameters();
	double[] getBFRCoordinates();
	void computeIGMatrix();
	
	double[] getInspectorOrientation();
	double[] getNearPlane();
	double[] getFarPlane();
	double[] getInspectorBFR();
	double getViewAngle();
}
