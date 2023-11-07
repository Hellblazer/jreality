package de.jreality.jogl3.shader;

import de.jreality.shader.PolygonShader;
import de.jreality.shader.TwoSidePolygonShader;

/**
 * jogl3 Implementation of two sided polygon shader as found in jogl1. TODO
 * @author padilla
 *
 */
public class TwoSidePolygonShaderImplementation extends de.jreality.jogl3.shader.PolygonShader implements TwoSidePolygonShader {

	private PolygonShader front;
	private PolygonShader back;
	
	
	public TwoSidePolygonShaderImplementation() {
		front = null;//new DefaultPolygonShader();
		back = null;//new DefaultPolygonShader();
	}

	@Override
	public PolygonShader getFront() {
		return front;
	}

	@Override
	public PolygonShader createFront(String shaderName) {
		front=null;
		return null;
	}

	@Override
	public PolygonShader getBack() {
		return back;
	}

	@Override
	public PolygonShader createBack(String shaderName) {
		back=null;
		return null;
	}

}
