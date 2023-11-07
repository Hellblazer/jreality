/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.ui.viewerapp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.jreality.scene.Geometry;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.actions.AbstractSelectionListenerAction;
import de.jreality.ui.viewerapp.actions.edit.AddTool;
import de.jreality.ui.viewerapp.actions.edit.AssignFaceAABBTree;
import de.jreality.ui.viewerapp.actions.edit.CreateAppearance;
import de.jreality.ui.viewerapp.actions.edit.LoadFileToNode;
import de.jreality.ui.viewerapp.actions.edit.LoadReflectionMap;
import de.jreality.ui.viewerapp.actions.edit.LoadTexture;
import de.jreality.ui.viewerapp.actions.edit.Remove;
import de.jreality.ui.viewerapp.actions.edit.Rename;
import de.jreality.ui.viewerapp.actions.edit.RotateReflectionMapSides;
import de.jreality.ui.viewerapp.actions.edit.SaveSelected;
import de.jreality.ui.viewerapp.actions.edit.ToggleAppearance;
import de.jreality.ui.viewerapp.actions.edit.TogglePickable;
import de.jreality.ui.viewerapp.actions.edit.ToggleVisibility;
import de.jreality.ui.viewerapp.actions.edit.export.ExportIFStoOBJ;
import de.jreality.ui.viewerapp.actions.edit.export.ExportIFStoSTL;
import de.jreality.ui.viewerapp.actions.edit.export.ExportIFStoVRML;


/**
 * Creates the viewerApp's menu bar and contains static fields
 * for names of menus and actions.
 * 
 * @author msommer
 */
public class ViewerMenu {

	//menu names
	public static String FILE_MENU = "File";
	public static String EDIT_MENU = "Edit";
	public static String VIEW_MENU = "View";

	//FILE MENU
	public static String LOAD_FILE = "Load files";
	public static String LOAD_FILE_MERGED = "Load merged files";
	public static String LOAD_SCENE = "Load scene";
	public static String SAVE_SCENE = "Save scene";
	public static String EXPORT = "Export";
	public static String QUIT = "Quit";

	//EDIT MENU
	public static String SAVE_SELECTED = "Save selected";
	public static String LOAD_FILE_TO_NODE = "Load files into node";
	public static String REMOVE = "Remove";
	public static String RENAME = "Rename";
	public static String TOGGLE_VISIBILITY = "Toggle visibility";
	public static String ASSIGN_FACE_AABBTREE = "Assign AABBTree";
	public static String APPEARANCE = "Appearance";
	public static String CREATE_APPEARANCE = "Create new Appearance";
	public static String TOGGLE_VERTEX_DRAWING = "Toggle vertex drawing";
	public static String TOGGLE_EDGE_DRAWING = "Toggle egde drawing";
	public static String TOGGLE_FACE_DRAWING = "Toggle face drawing";
	public static String LOAD_TEXTURE = "Load texture";
	public static String REFLECTIONMAP = "Reflection map";
	public static String LOAD_REFLECTIONMAP = "Load reflection map";
	public static String ROTATE_REFLECTIONMAP_SIDES = "Rotate reflection map sides";
	public static String GEOMETRY = "Geometry";
	public static String EXPORT_OBJ = "Write OBJ";
	public static String EXPORT_VRML = "Write VRML";
	public static String EXPORT_STL = "Write STL";
	public static String TOGGLE_PICKABLE = "Toggle pickable";
	public static String ADD_TOOL = "Add Tools";

	//CAMERA MENU
	public static String DECREASE_FIELD_OF_VIEW = "Decrease fieldOfView";
	public static String INCREASE_FIELD_OF_VIEW = "Increase fieldOfView";
	public static String DECREASE_FOCUS = "Decrease focus";
	public static String INCREASE_FOCUS = "Increase focus";
	public static String DECREASE_EYE_SEPARATION = "Decrease eyeSeparation";
	public static String INCREASE_EYE_SEPARATION = "Increase eyeSeparation";
	public static String TOGGLE_PERSPECTIVE = "Toggle perspective";
	public static String TOGGLE_STEREO = "Toggle stereo";
	public static String TOGGLE_CURSOR = "Toggle cursor";
	public static String LOAD_CAMERA_PREFERENCES = "Load camera preferences";
	public static String SAVE_CAMERA_PREFERENCES = "Save camera preferences";

	//VIEW MENU
	public static String TOGGLE_NAVIGATOR = "Show navigator";
	public static String TOGGLE_EXTERNAL_NAVIGATOR = "Open navigator in separate frame";
	public static String TOGGLE_BEANSHELL = "Show bean shell"; 
	public static String TOGGLE_EXTERNAL_BEANSHELL = "Open bean shell in separate frame";
	public static String TOGGLE_RENDER_SELECTION = "Show selection";
	public static String TOGGLE_MENU = "Hide menu bar";
	public static String SET_BACKGROUND_COLOR = "Set background color";
	public static String SKYBOX ="Skybox";
	public static String LOAD_SKYBOX ="Load skybox";
	public static String ROTATE_SKYBOX_SIDES ="Rotate skybox sides";
	public static String TOGGLE_VIEWER_FULL_SCREEN = "Toggle full screen";
	public static String MAXIMIZE = "Maximize frame size";
	public static String RESTORE = "Restore frame size";
	public static String SET_VIEWER_SIZE ="Set viewer size";

	/**
	 * Creates an edit menu containing all appropriate actions.<br>
	 * Also used for creating the navigator's context menu. 
	 * @param parentComp use as parent component for dialogs
	 * @param sm the selection manager to be used by contained actions
	 */
	protected static JMenu createEditMenu(Component parentComp, SelectionManager sm) {
		JMenu editMenu = new JMenu(EDIT_MENU);
		editMenu.setMnemonic(KeyEvent.VK_E);

		editMenu.add(new JMenuItem(new LoadFileToNode(LOAD_FILE_TO_NODE, sm, parentComp)));
		editMenu.add(new JMenuItem(new SaveSelected(SAVE_SELECTED, sm, parentComp)));
		editMenu.addSeparator();
		editMenu.add(new JMenuItem(new Remove(REMOVE, sm)));
		editMenu.add(new JMenuItem(new Rename(RENAME, sm, parentComp)));
		editMenu.addSeparator();
		editMenu.add(new JMenuItem(new ToggleVisibility(TOGGLE_VISIBILITY, sm)));
		editMenu.add(new JMenuItem(new AssignFaceAABBTree(ASSIGN_FACE_AABBTREE, sm)));
		editMenu.addSeparator();

		//appearance actions
		JMenu appearance = new JMenu(new AbstractSelectionListenerAction(APPEARANCE, sm){
			{this.setShortDescription(APPEARANCE+" options");}
			@Override
			public void actionPerformed(ActionEvent e) {}
			@Override
			public boolean isEnabled(SelectionEvent e) {
				return (e.componentSelected() || e.appearanceSelected());
			}
		});
		editMenu.add(appearance);
		appearance.add(new JMenuItem(new CreateAppearance(CREATE_APPEARANCE, sm)));
		appearance.addSeparator();
		appearance.add(new JMenuItem(new ToggleAppearance(TOGGLE_VERTEX_DRAWING, CommonAttributes.VERTEX_DRAW, sm)));
		appearance.add(new JMenuItem(new ToggleAppearance(TOGGLE_EDGE_DRAWING, CommonAttributes.EDGE_DRAW, sm)));
		appearance.add(new JMenuItem(new ToggleAppearance(TOGGLE_FACE_DRAWING, CommonAttributes.FACE_DRAW, sm)));
		appearance.addSeparator();
		appearance.add(new JMenuItem(new LoadTexture(LOAD_TEXTURE, sm, parentComp)));
		JMenu reflectionmap = new JMenu(REFLECTIONMAP);
		appearance.add(reflectionmap);
		reflectionmap.add(new JMenuItem(new LoadReflectionMap(LOAD_REFLECTIONMAP, sm, parentComp)));
		reflectionmap.add(new JMenuItem(new RotateReflectionMapSides(ROTATE_REFLECTIONMAP_SIDES, sm, parentComp)));

		//geometry actions
		JMenu geometry = new JMenu(new AbstractSelectionListenerAction(GEOMETRY, sm){
			{this.setShortDescription(GEOMETRY+" options");}
			@Override
			public void actionPerformed(ActionEvent e) {}
			@Override
			public boolean isEnabled(SelectionEvent e) {
				if (e.geometrySelected()) return true;
				Geometry g = null;
				if (e.componentSelected())
					g = e.getSelection().getLastComponent().getGeometry();
				return (g instanceof Geometry);
			}
		});
		editMenu.add(geometry);
		geometry.add(new JMenuItem(new ExportIFStoOBJ(EXPORT_OBJ, sm, parentComp)));
		geometry.add(new JMenuItem(new ExportIFStoVRML(EXPORT_VRML, sm, parentComp)));
		geometry.add(new JMenuItem(new ExportIFStoSTL(EXPORT_STL, sm, parentComp)));
		geometry.add(new JMenuItem(new TogglePickable(TOGGLE_PICKABLE, sm)));
		editMenu.addSeparator();

		editMenu.add(new JMenuItem(new AddTool(ADD_TOOL, sm, parentComp)));

		return editMenu;
	}
	
	/**
	 * Updates a given ActionMap by recursively adding all actions with accelerator key 
	 * from the specified menu using the accelerator keystroke as key. 
	 * @param actionMap the action map where to add the actions with corresponding keystroke
	 * @param menu the menu containing the actions
	 * @return the modified action map
	 */
	protected static ActionMap updateActionMap(ActionMap actionMap, JMenu menu) {
		Object[] elements = menu.getMenuComponents();

		for (int i = 0; i < elements.length; i++) {
			try { 
				JMenuItem element = (JMenuItem) elements[i];  //throws ClassCastExc when Separator
				if (element instanceof JMenu)
					updateActionMap(actionMap, (JMenu)element);
				else {
					//add action's accelerator key binding to the action map
					Action a = element.getAction();
					if (a!=null && a.getValue(Action.ACCELERATOR_KEY)!=null)
						actionMap.put(a.getValue(Action.ACCELERATOR_KEY), a);
				}
			} 
			catch (Exception e) { 
				//e.printStackTrace();
			}
		}
		
		return actionMap;
	}
	
}