package de.jreality.tutorial.util.polygon;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;

public class SubdividerDemo {

	public static void main(String[] args) {
		
		JRViewer v = new JRViewer();
		v.registerPlugin(new ContentTools());
		v.registerPlugin(new ContentLoader());
		v.addContentSupport(ContentType.CenteredAndScaled);
		v.addBasicUI();
		
		v.registerPlugin(new SubdividedPolygonPlugin(v));
		v.startup();
	}
	
}
