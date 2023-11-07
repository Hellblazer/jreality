package de.jreality.portal.calibration;

import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.scene.Avatar;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

public class CalibrationApp {

	private static final InputSlot POINTER = //InputSlot.getDevice("ShipHeadTransformation");
											   InputSlot.getDevice("PointerTransformation");
	static SceneGraphComponent scene = new SceneGraphComponent();
    static Coordinates centerC;
    static Coordinates rightC;
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.registerPlugin(new Avatar());
		v.addBasicUI();
		v.addBeanShellSupport();
		v.setContent(scene);
		v.startup();
		MatrixBuilder.euclidean().translate(0, 1.7, 0).assignTo(v.getViewer().getCameraPath().getLastComponent());
		
		//scene.addChild(DebugLattice.makeWorld());
		
		SceneGraphComponent left=new SceneGraphComponent();
		SceneGraphComponent right=new SceneGraphComponent();
		SceneGraphComponent center=new SceneGraphComponent();

		SceneGraphComponent ball = Primitives.sphere(.1, 0.,0.,0.);
		left.addChild(ball);
		right.addChild(ball);
		center.addChild(ball);
		
		centerC=new Coordinates();
		centerC.kill(2);
	    rightC=new Coordinates();
	    rightC.kill(0);		
//		left.addChild(yz.getSystem());
		right.addChild(rightC.getSystem());
		center.addChild(centerC.getSystem());

//		left.addTool(new DisplayTool() {
//			Matrix project(ToolContext tc) {
//				Matrix m = new Matrix(tc.getTransformationMatrix(POINTER));
//				double[] vec=m.getColumn(1);
//				vec[0]=0;
//				rightC.set(1, vec);
//				vec=m.getColumn(2);
//				vec[0]=0;
//				rightC.set(2, vec);
//				m.setEntry(0, 3, -1.24);
//				return m;
//			}
//		});
		
		right.addTool(new DisplayTool() {
			Matrix project(ToolContext tc) {
				Matrix m = new Matrix(tc.getTransformationMatrix(POINTER));
				double[] vec=m.getColumn(1);
				vec[0]=0;
				rightC.set(1, vec);
				vec=m.getColumn(2);
				vec[0]=0;
				rightC.set(2, vec);
				m.setEntry(0, 3, 1.24);
				return m;
			}
		});

		center.addTool(new DisplayTool() {
			Matrix project(ToolContext tc) {
				Matrix m = new Matrix(tc.getTransformationMatrix(POINTER));
				double[] vec=m.getColumn(0);
				vec[2]=0;
				centerC.set(0, vec);
				vec=m.getColumn(1);
				vec[2]=0;
				centerC.set(1, vec);
				m.setEntry(2, 3, -1.24);
				return m;
			}
		});
		
		scene.addChild(left);
		scene.addChild(right);
		scene.addChild(center);
	}
	
	private static abstract class DisplayTool extends AbstractTool {

		public DisplayTool() {
			addCurrentSlot(POINTER);
		}
		
		public void perform(ToolContext tc) {
			Matrix m = project(tc);
			MatrixBuilder.euclidean().translate(m.getColumn(3)).assignTo(tc.getRootToToolComponent().getLastComponent());
		}

		abstract Matrix project(ToolContext tc);
		
	}
}
