package de.jreality.plugin.geometry;

import de.jreality.geometry.ParametricTriangularSurfaceFactory;


/** A user interface for a {@link ParametricTriangularSurfaceFactory}.
 * 
 * @author G. Paul Peters, 03.06.2010
 */
public class ParametricTriangularSurfaceFactoryCustomizer extends AbstractGeometryFactoryCustomizer<ParametricTriangularSurfaceFactory> {
	private static final long serialVersionUID = 1L;
	
	public ParametricTriangularSurfaceFactoryCustomizer() {
		super();
	}
	
	public ParametricTriangularSurfaceFactoryCustomizer(ParametricTriangularSurfaceFactory factory) {
		super(factory);
	}
	
	Class<ParametricTriangularSurfaceFactory> getAcceptableClass() {
		return ParametricTriangularSurfaceFactory.class;
	}
	
	protected void initSliderProperties() {
		super.initSliderProperties();
	}
	
	protected void initToggleProperties() {
		super.initToggleProperties();
		toggleProperties.add(new ToggleProperty("generateVertexNormals", "vrtxNrmls"));
		toggleProperties.add(new ToggleProperty("generateFaceNormals", "fcNrmls"));
		toggleProperties.add(new ToggleProperty("generateEdgesFromFaces", "edges"));
		toggleProperties.add(new ToggleProperty("generateVertexLabels", "vrtxLbls"));
		toggleProperties.add(new ToggleProperty("generateEdgeLabels", "edgeLbls"));
		toggleProperties.add(new ToggleProperty("generateFaceLabels", "faceLbls"));
		toggleProperties.add(new ToggleProperty("generateAABBTree", "AABBTree"));
	}
}
