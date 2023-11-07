package de.jreality.plugin.menu;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import de.jreality.jogl3.JOGL3Viewer;
import de.jreality.jogl3.helper.PostProcessingHelper;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.scene.Viewer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class PostProcessingHelperMenu extends Plugin {

	private ViewMenuBar viewMenuBar;
	private JMenu postProcessingMenu = new JMenu("Effects (JOGL3)");
	private static View view;

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Effects (JOGL3)";
		info.vendorName = "Marcel Padilla, Andre Heydt";
		info.icon = null;
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		view = c.getPlugin(View.class);
		for(Viewer v : view.getViewer().getViewers()){
			if(v instanceof JOGL3Viewer){
				// add the menue
				double priorityEf = 10000.0;
				String MenuName = "Effects (JOGL3)";
				makeMenu( (JOGL3Viewer) v, MenuName);			
				postProcessingMenu.setMnemonic('e');
				viewMenuBar.addMenu(getClass(), priorityEf, postProcessingMenu);
				viewMenuBar.addMenuSeparator(getClass(), 0.0, MenuName);
			}
		}


	}

	@Override
	public void uninstall(Controller c) throws Exception {
		viewMenuBar.removeMenu(getClass(), postProcessingMenu);
		super.uninstall(c);
	}
	
	/**
	 * creates the Menu for everything. It is used in the ViewMenuBar.java.
	 * @param viewerPlugin
	 * @return
	 */
	public void makeMenu( JOGL3Viewer jogl3 , String MenuName) {
		if( jogl3.postProcessor == null){
			jogl3.postProcessor = new PostProcessingHelper();
		}

		final PostProcessingHelper postProcessor = jogl3.postProcessor;
		ArrayList<String> shaderNames = PostProcessingHelper.getAvailableShaderList();

		//enable,disable button
		JMenuItem item = new JRadioButtonMenuItem(
				new AbstractAction("en/dis-able Effects") {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						// mod post processing
						postProcessor.isEnabled = !postProcessor.isEnabled;
						view.viewerSwitch.render();
					}
				});
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		item.setSelected(false);
		postProcessingMenu.add(item);

		// remove all button
		item = new JMenuItem(
				new AbstractAction("remove all Effects") {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						// mod post processing
						postProcessor.removeAllShaders();
						view.viewerSwitch.render();
					}
				});

		item.setSelected(false);
		postProcessingMenu.add(item);
		postProcessingMenu.addSeparator();

		// special fxaa menu
		int maxFxaa = 16;
		final JMenu fxaaMenu = new JMenu("FXAA");
		ButtonGroup fxaaBg = new ButtonGroup();
		for(int times = 0 ; times <= maxFxaa ; times++){
			JMenuItem fxaaItem = makeFxaaShaderButton( postProcessor, times);
			fxaaBg.add(fxaaItem);
			fxaaMenu.add(fxaaItem);
		}
		postProcessingMenu.add(fxaaMenu);		
		postProcessingMenu.addSeparator();

		// add buttons for shaders
		for (String Location : shaderNames) {
			//skip fxaa, done before
			if(Location.contains("fxaa.f")) continue;
			if(Location.contains("copy.v")) continue;
			if(Location.contains("dof.f")) continue;
			postProcessingMenu.add(makeShaderDropdown( postProcessor, Location));

		}
	}
	public static JMenu makeShaderDropdown(final PostProcessingHelper postProcessor, final String Location){
		final JMenu shaderMenu = new JMenu(Location.substring(15));
		JButton effect = makeShaderButton( postProcessor, Location, true);
		shaderMenu.add(effect);
		effect = makeShaderButton( postProcessor, Location, false);
		shaderMenu.add(effect);
		return shaderMenu;	
	}

	public static JButton makeShaderButton( final PostProcessingHelper postProcessor, final String Location, final boolean add){
		String operation = "";
		if(add) operation = "+  ";
		else operation = "-  ";
		String name = operation + Location.substring(15);

		JButton item = new JButton(
				new AbstractAction(name) {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						// mod post processing
						if (add) postProcessor.addShader(Location);
						else postProcessor.removeShader(Location);
						view.viewerSwitch.render();
					}
				});

		item.setSelected(false);
		return item;
	}

	public static JMenuItem makeFxaaShaderButton( final PostProcessingHelper postProcessor,  final int times){
		String name;
		if(times==0) name = "remove FXAA";
		else name = times + "x FXAA";

		JMenuItem item = new JMenuItem(
				new AbstractAction(name) {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						// mod post processing
						postProcessor.addFxaaShader(times, false);							

						view.viewerSwitch.render();
					}
				});

		//for times = 1, make this a toggable option
		if(times == 1){
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		}

		return item;
	}	
}