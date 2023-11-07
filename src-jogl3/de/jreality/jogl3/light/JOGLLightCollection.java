package de.jreality.jogl3.light;

import java.util.LinkedList;

/**
 * saves the viewMatrix and lists of distinct types of Light. Important for later rendering.
 * with de.jreality.jogl3.JOGLSceneGraphComponentInstance.collectGlobalLights(double[] trafo, JOGLLightCollection collection, boolean visitAll)
 * it collects all lights 
 * 
 * 
 * comment padilla
 *
 */
public class JOGLLightCollection {
	//the matrix for transforming world to camera coordinates
	public JOGLLightCollection(double[] ViewMatrix){
		viewMatrix = ViewMatrix;
	}
	
	public double[] getViewMatrix() {
		return viewMatrix;
	}
	//is the matrix that transforms world coordinates to camera space
	private double[] viewMatrix;
	
	public LinkedList<JOGLPointLightInstance> pointLights = new LinkedList<JOGLPointLightInstance>();
	public LinkedList<JOGLSpotLightInstance> spotLights = new LinkedList<JOGLSpotLightInstance>();
	public LinkedList<JOGLDirectionalLightInstance> directionalLights = new LinkedList<JOGLDirectionalLightInstance>();
}
