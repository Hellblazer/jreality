package de.jreality.plugin.geometry;

import de.jreality.geometry.ParametricSurfaceFactory;


/** A user interface for a {@link ParametricSurfaceFactory}.
 * 
 * @author G. Paul Peters, 03.06.2010
 */
public class ParametricSurfaceFactoryCustomizer extends AbstractGeometryFactoryCustomizer<ParametricSurfaceFactory> {
	private static final long serialVersionUID = 1L;
	
	public ParametricSurfaceFactoryCustomizer() {
		super();
	}
	
	public ParametricSurfaceFactoryCustomizer(ParametricSurfaceFactory factory) {
		super(factory);
	}
	
	Class<ParametricSurfaceFactory> getAcceptableClass() {
		return ParametricSurfaceFactory.class;
	}
	
	
	protected void initSliderProperties() {
		super.initSliderProperties();
		sliderProperties.add(new DoubleSliderProperty("uMin", -10., 10.));
		sliderProperties.add(new DoubleSliderProperty("uMax", -10., 10.));
		sliderProperties.add(new DoubleSliderProperty("vMin", -10., 10.));
		sliderProperties.add(new DoubleSliderProperty("vMax", -10., 10.));
		sliderProperties.add(new IntegerSliderProperty("uLineCount", "uLines", 2, 100));
		sliderProperties.add(new IntegerSliderProperty("vLineCount", "vLines", 2, 100));
		sliderProperties.add(new DoubleSliderProperty("uTextureScale", 0., 10.));
		sliderProperties.add(new DoubleSliderProperty("vTextureScale", 0., 10.));
		sliderProperties.add(new DoubleSliderProperty("uTextureShift", 0., 1.));
		sliderProperties.add(new DoubleSliderProperty("vTextureShift", 0., 1.));
	}
	
	protected void initToggleProperties() {
		super.initToggleProperties();
		toggleProperties.add(new ToggleProperty("generateVertexNormals", "vrtxNrmls"));
		toggleProperties.add(new ToggleProperty("generateFaceNormals", "fcNrmls"));
		toggleProperties.add(new ToggleProperty("generateTextureCoordinates", "txCrds"));
		toggleProperties.add(new ToggleProperty("generateEdgesFromFaces", "edges"));
		toggleProperties.add(new ToggleProperty("edgeFromQuadMesh", "cnctEdgs"));
		toggleProperties.add(new ToggleProperty("closedInUDirection", "uClosed"));
		toggleProperties.add(new ToggleProperty("closedInVDirection", "vClosed"));
		toggleProperties.add(new ToggleProperty("generateVertexLabels", "vrtxLbls"));
		toggleProperties.add(new ToggleProperty("generateEdgeLabels", "edgeLbls"));
		toggleProperties.add(new ToggleProperty("generateFaceLabels", "faceLbls"));
		toggleProperties.add(new ToggleProperty("generateAABBTree", "AABBTree"));
	}
}
