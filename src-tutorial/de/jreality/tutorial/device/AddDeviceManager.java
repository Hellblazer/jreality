package de.jreality.tutorial.device;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.device.DeviceManagerPlugin;

public class AddDeviceManager {
	public static void main(String[] args) {
		// customize a JRViewer to have Virtual Reality support (skyboxes, terrain, etc)
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		v.setContent(Primitives.icosahedron());
		v.registerPlugin(new ContentAppearance());
		v.registerPlugin(new ContentLoader());
		v.registerPlugin(new ContentTools());
		v.registerPlugin(new DeviceManagerPlugin());
		v.setShowPanelSlots(true, true, false, false);
		v.startup();
	}

}
