package approximateApproach;

import Jama.Matrix;

public class InspectorParameters implements InspectorParametersInterface{
	
	private Matrix m_IG;
	private Matrix m_BG;
	//Get the line read from inspector position/orientation file, and then process it to extract inspector position/orientation
	private String m_line;
	private double[] m_query;
	private double m_nearPlane;
	private double m_farPlane;
	private double m_roll, m_pitch, m_yaw;
	
	private double[] m_scalingFactors;
	private double[] m_inspectorOriginGPS;
	private double[] m_bridgeOriginGPS;
	
	public double[] m_NPD;
	public double[] m_FPD;
	public double[] m_inspectorBFR;
	
	
  	public InspectorParameters(String line, Matrix BG, double[] scalingFactors, double[] bridgeOriginGPS) {
		m_BG=BG; 
		m_scalingFactors=scalingFactors;
		m_bridgeOriginGPS=bridgeOriginGPS;	
		m_line = line;
		determineInspectorParameters();
	}
 	
  	public InspectorParameters(String line, Matrix BG, double[] scalingFactors, double[] bridgeOriginGPS, double[] inspectorPositionError, double[] inspectorOrientationError){
  		m_BG=BG; 
		m_scalingFactors=scalingFactors;
		m_bridgeOriginGPS=bridgeOriginGPS;
		m_line = line;
		determineInspectorParameters(inspectorPositionError, inspectorOrientationError);
  	}
  	
  	public void determineInspectorParameters(double[] positionError, double[] orientationError){
  		setOrientation_inspector(orientationError);
  		setIFR_origin(positionError);
  		setNPD_FPD();
  		  		
  	}
  	
  	public void determineInspectorParameters(){
  		setOrientation_inspector();
  		setIFR_origin();
  		setNPD_FPD();
  		System.out.println("I am in determnine Inspector");
  		  		
  	}
  	
	//Function to split given string, and then obtain position array
        private double[] getInspectorPosition()
        {
                String[] tempLine = m_line.split("\t");
                double[] tempRet = new double[3];
		
                for(int count = 0; count < 3; count++)
                        tempRet[count] = Double.parseDouble(tempLine[count]);

		//Longitude values in real field data should be negative because we are in western hemisphere
		tempRet[0] = -tempRet[0];
		
		System.out.println("Inspector position in GPS");
		for(int i=0; i<3; i++)
			System.out.println(tempRet[i]+ " ");
		System.out.println();

                return tempRet;
        }

	//Function to split given string, and then obtain orientation array
        private double[] getInspectorOrientation()
        {
                String[] tempLine = m_line.split("\t");
                double[] tempRet = new double[3];

		System.out.println("Inspector orientation in GPS");
                for(int count = 0; count < 3; count++)
                {
			System.out.println(tempLine[count+3]+ " ");
                        tempRet[count] = Double.parseDouble(tempLine[count+3]);
                }
		System.out.println();
                return tempRet;
        }
	
 	public void setOrientation_inspector()
	//Set the orientation of the inspector with respect to GPS
	{
		//Changed to accommodate reading from file with inspector positions/orientations
 		//double[] orientation=Config.getInspectorOrientation();
		double[] orientation = getInspectorOrientation();
		
		//Old way of getting roll pitch yaw where 52 degrees was not being added
		m_roll=-Math.PI*(orientation[0]/180);
		m_pitch=-Math.PI*(orientation[1]/180);
		m_yaw=-Math.PI*(orientation[2]/180);

		//m_roll=-Math.PI*(orientation[0]/180);
                //m_pitch=Math.PI*(orientation[1]/180);
                //m_yaw=Math.PI*(-(orientation[2]+52)/180);

	} 
 	
 	public void setOrientation_inspector(double[] error)
	//Set the orientation of the inspector with respect to GPS
	{
		//Changed to accommodate reading from file with inspector positions/orientations
 		//double[] orientation=Config.getInspectorOrientation();
		double[] orientation = getInspectorOrientation();
		//Old way of getting roll pitch yaw where 52 degrees was not being added
		//m_roll=-Math.PI*((orientation[0]+error[0])/180);
		//m_pitch=-Math.PI*((orientation[1]+error[1])/180);
		//m_yaw=-Math.PI*((orientation[2]+error[2])/180);
		
	} 
 	
 	@Override
	public void computeBoundingBoxParameters() {
		//Compute bounding box parameters i.e. 2 points and angle representing conic region of interest
			
				computeIGMatrix();
				m_query=new double[] {0,m_nearPlane,0};
				m_NPD=getBFRCoordinates();
				m_query=new double[] {0, m_farPlane, 0};
				m_FPD=getBFRCoordinates();

				for(int i=0;i<3;i++)	
					m_query[i]=0;
				m_inspectorBFR=getBFRCoordinates();
	}

	public void setNPD_FPD()
	//set the FPD and NPD values i.e. Far plane distance and near plane distance, of the inspector
	{
		double[] distance=Config.getNpdFpd();
		m_nearPlane=distance[0];
		m_farPlane=distance[1];	
	}
	
	@Override
	public void computeIGMatrix() 
	//Compute and return IG matrix
	{
		/*IG=[Angle Matrix (with z(theta1)->alpha(yaw), x(theta2)->beta(pitch), y(theta3)->gamma(roll))] *
		 * [0,0,scaling_factor[0];0,0,scaling_factor[1];0,0,scaling_factor[2]*/
			double[][] scaling=new double[1][3];
			scaling[0]=m_scalingFactors;
			Matrix scale=new Matrix(scaling);
			Matrix temp_IG=Matrix.identity(3, 3);
			for(int i=0;i<3;i++)
				temp_IG.set(i,i,1/scale.get(0,i));
			Matrix temp_yaw=new Matrix(3,3);
			Matrix temp_pitch=new Matrix(3,3);
			Matrix temp_roll=new Matrix(3,3);
			Matrix Angle=new Matrix(3,3);
			//Setting the roll, pitch and yaw matrices
			temp_roll.set(0, 0, Math.cos(m_roll));
			temp_roll.set(0, 2, Math.sin(m_roll));
			temp_roll.set(1, 1, 1);
			temp_roll.set(2, 0, -Math.sin(m_roll));
			temp_roll.set(2, 2, Math.cos(m_roll));
			
			temp_pitch.set(0, 0, 1);
			temp_pitch.set(1, 1, Math.cos(m_pitch));
			temp_pitch.set(1, 2, -Math.sin(m_pitch));
			temp_pitch.set(2, 1, Math.sin(m_pitch));
			temp_pitch.set(2, 2, Math.cos(m_pitch));
			
			temp_yaw.set(0, 0, Math.cos(m_yaw));
			temp_yaw.set(0, 1, -Math.sin(m_yaw));
			temp_yaw.set(1, 0, Math.sin(m_yaw));
			temp_yaw.set(1, 1, Math.cos(m_yaw));
			temp_yaw.set(2, 2, 1);
	
			Angle=temp_roll.times(temp_pitch).times(temp_yaw);
			m_IG=Angle.times(temp_IG);
		
	}

	public void setIFR_origin()
	//Inspector origin in GPS
	{
		//Changed to accommodate reading from list of inspector position/orientation from file
		//double[] origin=Config.getInspectorPosition();
		double[] origin = getInspectorPosition();
		
		m_inspectorOriginGPS=origin;
	}
	
	public void setIFR_origin(double[] error)
	//Inspector origin in GPS
	{
		//Changed to accommodate reading from list of inspector position/orientation from file
		//double[] origin=Config.getInspectorPosition();
		double[] origin = getInspectorPosition();
		
		for(int i=0;i<origin.length;i++){
				origin[i]=origin[i]+error[i];
		}
		
		m_inspectorOriginGPS=origin;
	}
	
	@Override
	public double[] getBFRCoordinates() 
	//Convert a query point from IFR coordinates to BFR coordinates
	{
		Matrix temp_point, temp_origin, temp_bridge;

		temp_point=new Matrix(new double[][] {m_query});
		temp_origin=new Matrix(new double[][] {m_inspectorOriginGPS});
		temp_bridge=new Matrix(new double[][] {m_bridgeOriginGPS});
		
		return (temp_point.times((m_BG.times(m_IG.inverse())).inverse())).plus((temp_origin.minus(temp_bridge)).times(m_BG.inverse())).getArray()[0];
	}


	@Override
	public double[] getFarPlane() {
		return m_FPD;
	}


	@Override
	public double[] getInspectorBFR() {
		return m_inspectorBFR;
	}


	@Override
	public double[] getNearPlane() {
		return m_NPD;
	}


	@Override
	public double getViewAngle() {
		return Math.PI*(Config.getViewAngle()/180);
	}

}
