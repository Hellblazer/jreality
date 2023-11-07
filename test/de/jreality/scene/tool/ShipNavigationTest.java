package de.jreality.scene.tool;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.scene.Avatar;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.PickUtility;

public class ShipNavigationTest {

	public static void main(String[] args) throws Exception {
		SceneGraphComponent earth = Primitives.sphere(2, null); 
		PickUtility.assignFaceAABBTrees(earth);
		JRViewer v = new JRViewer();
		v.setContent(earth);
		Avatar a = new Avatar();
		v.registerPlugin(a);
		v.addBasicUI();
		v.startup();

		Camera c = (Camera) v.getViewer().getCameraPath().getLastElement();
		c.setNear(0.1);
		c.setFar(1000);
		v.startup();
	}
}
