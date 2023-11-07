package de.jreality.tutorial.util.polygon;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.Color;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;

/**
 * A subdivider that uses 4-point subdivision. For closed or open point sequences.
 * It will be updated whenever the control point sequence changes.
 * 
 * @author Steffen Weissmann
 */
public class SubdividedPolygon implements PointSequence, ChangeListener {

	int subdivisionSteps;
	
	// the PointSequence we will subdivide...
	private PointSequence controlPoints;

	
	// the resulting points
	private double[][] vertices;
	
	// the component containing the polygon geometry
	private SceneGraphComponent base = new SceneGraphComponent("subdivided poly");
	private IndexedLineSetFactory lsf = new IndexedLineSetFactory();
	
	// Shaders to set appearance attributes
	private DefaultPointShader ps;
	private DefaultLineShader ls;
	
	/**
	 * Create a SubdividedPolygon for a set of control points.
	 * @param controlPoints the control point sequence used for subdivision.
	 */
	public SubdividedPolygon(PointSequence controlPoints) {
		this.controlPoints = controlPoints;
		subdivisionSteps = 2;
		// we attach this class as a change listener to the control points.
		controlPoints.addChangeListener(this);
		
		initAppearance();
		update();
	}

	/**
	 * Create a SubdividedPolygon for a set of control points.
	 * @param controlPoints the control point sequence used for subdivision.
	 */
	public SubdividedPolygon(PointSequence controlPoints, int subdivisonsteps) {
		subdivisionSteps = subdivisonsteps;
		// we attach this class as a change listener to the control points.
		controlPoints.addChangeListener(this);
		this.controlPoints = controlPoints;
		
		initAppearance();
		update();
	}
	
	private void initAppearance() {
		//shader
		base.setGeometry(lsf.getGeometry());
		base.setAppearance(new Appearance());
		
		DefaultGeometryShader dps = (DefaultGeometryShader)
			ShaderUtility.createDefaultGeometryShader(base.getAppearance(), false);
		ps = (DefaultPointShader) dps.getPointShader();
		ls = (DefaultLineShader) dps.getLineShader();
		
		ps.setPointRadius(0.04);
		ls.setTubeRadius(0.02);
		ls.setDiffuseColor(Color.orange);
		ps.setDiffuseColor(Color.green);
	}

	/**
	 * Set the number of subdivision steps.
	 * @param n number of steps
	 */
	public void setSubdivisionLevel(int n) {
		this.subdivisionSteps=n;
		update();
	}

	// compute the subdivided points
	private void computeSpline() {
		double[][]cur = controlPoints.getPoints();
		for (int i=0; i<subdivisionSteps; i++) {
			int n = isClosed() ? cur.length : cur.length-1;
			double[][] sub = new double[cur.length+n][];
			for (int j=0; j<n; j++) {
				sub[2*j] = cur[j];
				sub[2*j+1] = subdivide(
						point(cur, j-1),
						point(cur, j),
						point(cur, j+1),
						point(cur, j+2));
			}
			if (!isClosed()) {
				sub[2*n]=cur[n];
			}
			cur = sub;
		}
		vertices = cur;
	}
	
	
	// 4-point subdivision: res = (9*(v2+v3)-v1-v4) /16
	private double[] subdivide (double[] v1, double[] v2, double[] v3, double[] v4) {
		double[] res = Rn.add(null, v2, v3);
		res = Rn.times(null, 9., res);
		res = Rn.subtract(null, res, v1);
		res = Rn.subtract(null, res, v4);
		res = Rn.times(null, 1./16., res);
    	return res;
	}
	
	private double[] point(double[][] pts, int idx) {
		int n=pts.length;
		if (idx>=0 && idx<n) return pts[idx];
		if (controlPoints.isClosed()) return pts[(idx+n)%n];
		double[] p0=null, p1=null;
		//boundary
		if (idx==-1) {
			p0 = pts[0];
			p1 = pts[1];
		}
		if (idx==n) {
			p1 = pts[n-2];
			p0 = pts[n-1];
		}
		double[] ret = Rn.linearCombination(null, 2, p0, -1, p1);
		return ret;
	}
	

	// recompute subdivision
	private void update() {
		computeSpline();
		drawLineSet();
		fireChange();
	}

	private void drawLineSet() {
		double[][] pts = vertices;
		int n = pts.length;
		if (n != lsf.getVertexCount()) {
			int [][] inds = new int[n][2];
			for (int i=0, m=isClosed() ? n : n-1; i<m; i++) {
				inds[i][0]=i;
				inds[i][1]=(i+1)%n;
			}
			lsf.setVertexCount(n);
			lsf.setEdgeCount(inds.length);
			lsf.setEdgeIndices(inds);
		}
		lsf.setVertexCoordinates(pts);
		lsf.update();
	}
	
	/**
	 * this is called from the control point sequence.
	 */
	public void stateChanged(ChangeEvent e) {
		update();
	}

	/******** listener code ********/
	
	private List<ChangeListener> listeners = new LinkedList<ChangeListener>();
	
	private void fireChange() {
		final ChangeEvent ev = new ChangeEvent(this);
		synchronized (listeners ) {
			for (ChangeListener cl : listeners) cl.stateChanged(ev);
		}
	}

	public void addChangeListener(ChangeListener cl) {
		synchronized (listeners) {
			listeners.add(cl);
		}
	}
	
	public void removeChangeListener(ChangeListener cl) {
		synchronized (listeners) {
			listeners.remove(cl);
		}
	}

	@Override
	public double[][] getPoints() {
		return vertices;
	}

	@Override
	public boolean isClosed() {
		return controlPoints.isClosed();
	}
	
	public SceneGraphComponent getBase() {
		return base;
	}
	
}
