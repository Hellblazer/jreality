package de.jreality.tutorial.util.polygon;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class SubdividedPolygonPlugin extends ShrinkPanelPlugin {

	private int numControllPoints = 10;
	private int subdiv = 2;
	
	private	JRViewer viewer;
	private SceneGraphComponent root = new SceneGraphComponent("root");
	private DragPointSet dps;
	private PointSequenceView curveView; 
	private SubdividedPolygon subPoly;
	
	public SubdividedPolygonPlugin(JRViewer v) {
		viewer = v;
		shrinkPanel = new ShrinkPanel("Spherical Curve");
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		int y=0;
		c.gridy = y;
		
		final LabeledSpinner vertexSpinner = new LabeledSpinner("Control Points:",
				numControllPoints, 3, 100, 1);
		vertexSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				numControllPoints = (int)vertexSpinner.getValue();
				if (numControllPoints != vertexSpinner.getValue())
					vertexSpinner.setValue(numControllPoints);
				updateDragPoints(numControllPoints);
			}
		});
		c.gridy = y++;
		shrinkPanel.add(vertexSpinner,c);
		
		
		JLabel subdivLabel = new JLabel("Subdivision Level: [0 / 10]");
		c.gridy = y++;
		shrinkPanel.add(subdivLabel,c);
		
		final LabeledSpinner subdivSpinner = new LabeledSpinner("Subdivision Steps:",
				subdiv, 0, 10, 1);
		subdivSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				subdiv = (int)subdivSpinner.getValue();
				updatePolygon(subdiv);
			}
		});
		c.gridy = y++;
		shrinkPanel.add(subdivSpinner,c);
		
				
		dps = initDragPointSet(numControllPoints);
		
		subPoly = new SubdividedPolygon(dps, subdiv);
		curveView = new PointSequenceView(subPoly);
		
		root.addChild(dps.getBase());
		root.addChild(curveView.getBase());
		
		root.addChild(dps.getBase());
		root.addChild(subPoly.getBase());
		
		viewer.setContent(root);
	}
	
	public static double[][] circle(int n, double r) {
		double[][] verts = new double[n][3];
		double dphi = 2.0*Math.PI/n;
		for (int i=0; i<n; i++) {
			verts[i][0]=r*Math.cos(i*dphi);
			verts[i][1]=r*Math.sin(i*dphi);
		}
		return verts;
	}
	
	private DragPointSet initDragPointSet(int n) {
		double [][] vertices = circle(n, 1.);
		dps = new DragPointSet(vertices);
		
		DefaultGeometryShader dgs_cp = (DefaultGeometryShader)
				ShaderUtility.createDefaultGeometryShader(dps.getBase().getAppearance(), false);
			
		DefaultPointShader ps_cp = (DefaultPointShader) dgs_cp.getPointShader();
		ps_cp.setPointRadius(0.05);
		ps_cp.setPointSize(0.05);
		return dps;
	}

	public void updatePolygon(int sub) {
		subPoly.setSubdivisionLevel(sub);
	}

	public void updateDragPoints(int numControllPoints) {
		dps = new DragPointSet(circle(numControllPoints, 1.));
		subPoly = new SubdividedPolygon(dps,subdiv);
		curveView = new PointSequenceView(subPoly);
		root.removeAllChildren();
		root.addChild(dps.getBase());
		root.addChild(curveView.getBase());
		root.removeAllChildren();
		root.addChild(dps.getBase());
		root.addChild(subPoly.getBase());
	}
	
	public double[][] getPoints(){
		return dps.getPoints();
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
		
}
