// NOTE: Connection between de.jreality.plugin.stereogram.StereogramPlugin and 
//       de.jreality.jogl3.glsl.testing.stereogram.f via
//       de.jreality.jogl3.helper.TransparencyHelper and
//       de.jreality.shader.CommonAttributes

package de.jreality.plugin.stereogram;

import javax.swing.Box;

import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public class StereogramPlugin extends Plugin {

	transient protected Box inspector = Box.createVerticalBox();
	ImageData id = null;
	Appearance rootApp = null;
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		c.getPlugin(StereogramMenuPlugin.class);
	
		// TODO
//		Viewer v = c.getPlugin(View.class).getViewer();
//		v.render(); // alle 50 millisekunden
				
		c.getPlugin(View.class).getViewer().selectViewer(1);
		
		rootApp = c.getPlugin(View.class).getViewer().getSceneRoot().getAppearance();
		Camera cam = c.getPlugin(View.class).getViewer().getCameraPath().getLastComponent().getCamera();

		changeImage(c.getPlugin(StereogramMenuPlugin.class).getTexturePath());
				
		rootApp.setAttribute(CommonAttributes.STEREOGRAM_RENDERING, true);
		rootApp.setAttribute(CommonAttributes.STEREOGRAM_NUM_SLICES, 12); // actually half the number
		
		// changes in TransparencyHelper and CommonAttributes
		rootApp.setAttribute(CommonAttributes.STEREOGRAM_SHOWEDGES, false);
		rootApp.setAttribute(CommonAttributes.STEREOGRAM_RANDOMDOT, false);		
		cam.setNear(0.5);
	}
	
	
	public void changeImage(String imagePath) {
		try{ // this is for images in the de.jreality.plugin.stereogram folder
			id = ImageData.load(Input.getInput(this.getClass().getResource(imagePath)));
			TextureUtility.setBackgroundTexture(rootApp, id); // sets background texture
		}catch(Exception e){
			try{ // this is to load own images
				id = ImageData.load(Input.getInput(imagePath));
				TextureUtility.setBackgroundTexture(rootApp, id); // sets background texture
			}catch(Exception ee){
				ee.printStackTrace();
			}
		}
	}
	
}