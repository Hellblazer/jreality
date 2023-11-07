package de.jreality.plugin.device;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.ToolSystemPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.toolsystem.config.RawMapping;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.NativePathUtility;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
//import de.jreality.device.jinput.component.JInputAnalog;
//import de.jreality.device.jinput.component.JInputButton;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 * a Shrinkpanel to view the devices connected to the ToolSystem and their mappings.
 * Categorized by Mouse, Keyboard and Controllers
 * march 2015
 * @author heydt, padilla
 *
 */
public class DeviceManagerPlugin extends ShrinkPanelPlugin implements ChangeListener{
	//	private ShrinkPanel shrinkPanel; replaced "extends Plugin" with " extends SchrinkPanelPlugin" so the deviceManager is not nested somewhere else

	// have a scenegraphcomponent to add the tool to.
	de.jreality.scene.SceneGraphComponent devices;

	// create a tool to interact with the shrinkplugin
	DeviceManagerTool deviceManagerTool;

	// tabs to switch between mouse, keyboard and controller view.
	String[]titles = new String[]{"Identifier", "Value", "InputSlot"};
	JTabbedPane tabbedPane = new JTabbedPane();
	boolean tableInitialized = false; // the first time a button is pressed, the tables is filled. (to avoid 

	// plugin controller, to read the toolsystem after install
	de.jtem.jrworkspace.plugin.Controller pluginController;
	boolean toolSystemPluginFound = false;

	// the ToolSystem after reading it at install
	ToolSystemPlugin toolSystemPlugin;
	ToolSystem toolSys =null;

	// details for the tabbedPanel
	GridBagConstraints constrain;
	JScrollPane mousePane; //only one mouse and keyboard
	JScrollPane keyboardPane;
	ArrayList<JScrollPane> controllerPaneList = new ArrayList<JScrollPane>();

	
	// details of the devices
	ArrayList<String> DeviceIdList;
	HashMap<String,ArrayList<Vector>> deviceId2VectorList;
	
	



	// constructor
	public DeviceManagerPlugin(){

		// shrinkpanel setup
		shrinkPanel = new ShrinkPanel("Device Manager");
		shrinkPanel.setShrinked(false);
		shrinkPanel.setIcon(getPluginInfo().icon);
		shrinkPanel.add(Box.createHorizontalStrut(5));
		shrinkPanel.setLayout(new GridBagLayout());//MinSizeGridBagLayout());
		//		GridBagConstraints constrain = new GridBagConstraints();
		constrain = LayoutFactory.createLeftConstraint();
		//		constrain.fill = GridBagConstraints.HORIZONTAL;
		//		constrain.insets = new Insets(2,2,2,2);
		constrain.gridx = 0;
		constrain.gridy = 0;


		// reset button =============
		JButton resetButton = new JButton("reread devices");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("reread button pressed");
				rereadButtonScript();
				//				tabbedPane.disable();
				//				tabbedPane.updateUI();
				//				if(tabbedPane.)
				//				tabbedPane.remove(mousePane);
				//				if(!firsttry){
				//					mousePane = addMouse();
				////					tabbedPane.add("Mouse2" , mousePane);
				//					mousePane.repaint();
				//					tabbedPane.repaint();;
				//				}
				//				firsttry=false;
				//				
				// since i can't see how to repaint/update tabs i remove and read them all again
				//sandbox for something else
//				pluginController.getPlugin(ViewPreferences.class).setShowToolBar(false); // this works
//				viewPreferences.setShowToolBar(false);
				
				fillTabbedPaneScript();
				initializeTables();
				addDeviceTable();


			}

		});
		constrain.gridy=++constrain.gridy;
		//		constrain.gridy=heighY++;
		shrinkPanel.add(resetButton,constrain);
		// end pause play button ==================================


		// controller table ============================

		// JTabbedPane setup to fill it

		

		// there is no need to call this now since the toolSystem is available only after install, so not yet
		fillTabbedPaneScript();

		// add the tabs to the panel
		constrain.gridy=++constrain.gridy;
		shrinkPanel.add(tabbedPane,constrain);

		// end controller table ============================





	}



	private JScrollPane addMouse() {

		//case, no toolSystem
		if(toolSys == null ) return addNothing();
		else{
			//case: toolSystem found



			//model sets title and can add rows afterwards
			DefaultTableModel model = new DefaultTableModel( titles, 0 );

			// get the mouse from the toolSystem
			//			toolSys.


			//			model.addRow(rowData);






			// convert model to table in place it into a scroll panel
			JTable table = new JTable(model);
			table.setEnabled(false);
			JScrollPane jsp = new JScrollPane(table);
			jsp.setPreferredSize(new Dimension(200,200));
			jsp.setMinimumSize(jsp.getPreferredSize());

			return jsp;
		}

	}

	private JScrollPane addKeyboard() {
		// TODO Auto-generated method stub
		return addMouse();
		//		return null;
	}

	private void addDeviceTable() {
		

		// create a series of JTables and add them at the end
		HashMap<String, DefaultTableModel> deviceId2Model = new HashMap<String, DefaultTableModel>();

		for(String currentDeviceID : DeviceIdList ){ // for every Device

			//model sets title and can add rows afterwards
			DefaultTableModel model = new DefaultTableModel( titles, 0 );
			deviceId2Model.put(currentDeviceID, model);
			
			for(Vector v : deviceId2VectorList.get(currentDeviceID)){ // for each mapping of this device

			// the above vector is added as a row in the table
			deviceId2Model.get(currentDeviceID).addRow(v);
			
			}
		}

		
		// clears our tabbedPane and rereads up to date information
		tabbedPane.removeAll();
		for(String currentDeviceID : DeviceIdList ){ // for every Device
			// convert model to table in place it into a scroll panel
			JTable table = new JTable(  deviceId2Model.get(currentDeviceID)  );
			table.setEnabled(false);
			JScrollPane jsp = new JScrollPane(table);
			jsp.setPreferredSize(new Dimension(200,200));
			jsp.setMinimumSize(jsp.getPreferredSize());
			tabbedPane.add(currentDeviceID, jsp);
		}
		
		}
		
//		return jsp;
	
	

	private JScrollPane addControllerTable(Controller ctrl) {

		Component[] cmpArray = ctrl.getComponents();
		//model sets title and can add rows afterwards
		DefaultTableModel model = new DefaultTableModel( titles, 0 );


		for(int i=0;i<cmpArray.length;i++){ // for every component
			String name = cmpArray[i].getName();
			InputSlot slot = InputSlot.LEFT_BUTTON; // just a test, will result in "PrimaryAction"
			Vector v = new Vector();
			v.add(name);
			double value = 0.0;//cmpArray[i].;
			if(cmpArray[i].isAnalog()){

//				JInputAnalog analog = new JInputAnalog(name, value, slot);
				v.add(value);
			} else {
				boolean pressed = false;
//				JInputButton button = new JInputButton(name, pressed, slot);
				v.add("---");
			}
			v.add(slot);

			// the above vector is added as a row in the table
			model.addRow(v);

		}


		// convert model to table in place it into a scroll panel
		JTable table = new JTable(model);

		table.setEnabled(false);
		JScrollPane jsp = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		return jsp;
	}

	/**
	 * needed at the constructor since the toolsystem is not yet ready 
	 * @return
	 */
	private JScrollPane addNothing() {

		//model sets title and can add rows afterwards
		DefaultTableModel model = new DefaultTableModel( titles, 0 );

		// convert model to table in place it into a scroll panel
		JTable table = new JTable(model);
		table.setEnabled(false);
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(200,200));
		jsp.setMinimumSize(jsp.getPreferredSize());

		return jsp;
	}

	/**
	 *  clears what was inside the tabbedPane of the plugin an refills it with up to date device info
	 */
	private void fillTabbedPaneScript(){
		// clears our tabbedPane and rereads up to date information
		tabbedPane.removeAll();
		// mouse
		tabbedPane.add("Mouse", addMouse());
		//keyboard
		tabbedPane.add("Keyboard", addKeyboard());
		//controllers
		NativePathUtility.set("jni"); // Q what does it do?
		// we add on tab for each controller that jinput detected
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		Controller[] controllers = env.getControllers();
		for (Controller ctrl : controllers) {
			tabbedPane.add(ctrl.getName(),addControllerTable(ctrl));
		}
	}


	/**
	 * this is called in the perform method of the deviceManagerTool and tells what to mark was pressed, unpressed.
	 * It will update the tables with the correct values. Note: we handle these event in a Tool to not start a new thread 
	 * @param slot
	 */
	public void jInputInteraction(ToolContext tc){
		// the deviceManagerTool tells us to update the values of this specific entry from the toolcontext
		// before we can do this, we need to have registered all the components. Do this at the first time deviceManager.perform() is called
	}



	/**
	 *   since the toolsystemplugin can only be called at install() and this happens after the constructor, we almost "reconstruct" the plugin with a button.
	 *  Press it to get a new reading of all the buttons from the toolsystem
	 */
	private void rereadButtonScript() {

		//		 // attempt to reinit the ToolSystem to read new controllers  or changed configurations 
		//		try {
		//			tsp.uninstall(pluginController);
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		////		try {
		////			tsp.install(pluginController);
		////		} catch (Exception e) {
		////			// TODO Auto-generated catch block
		////			e.printStackTrace();
		////		}
		//		((JRViewer) pluginController).registerPlugin(new ToolSystemPlugin());
		//		((JRViewer) pluginController).startup();



	}

	/**
	 * this function is called in the install() call and already has access to the full toolSystem.
	 * It will create the panes and tables for each device and each component as set by the ToolSystemConfig
	 */
	private void initializeTables(){
		if(toolSystemPluginFound){
			// get the toolSystem
			ToolSystem toolSys = pluginController.getPlugin(ToolSystemPlugin.class).getToolSystem();
			
			// at first, try just to get to view all rawdevice inputslots
			ToolSystemConfiguration toolSysConfig = toolSys.getToolSystemConfiguration();
			List<RawMapping> rawMappings = toolSysConfig.getRawMappings();
			
			// map a device ID to a list of the column of the table it belongs to
			DeviceIdList = new ArrayList<String>();
			deviceId2VectorList = new HashMap<String,ArrayList<Vector>>();
			
			
			// we will go through all rawMappings. Each rawMapping has a DeviceID and we will base our tab creation upon these names
			for(RawMapping rawMap : rawMappings){

				// make new line in the table
				Vector lineInTable = new Vector();
				
				// List the device if not previously done so. Also, init the HashSet for the hashmap at this point
				if( !DeviceIdList.contains( rawMap.getDeviceID() ) ) {
					DeviceIdList.add(rawMap.getDeviceID());
//					linesOfTable linesOfDeviceTable = new linesOfTable();
					ArrayList<Vector> linesOfDeviceTable = new ArrayList<Vector>();
					deviceId2VectorList.put(rawMap.getDeviceID(), linesOfDeviceTable);
				}
				
				// fill the line
				lineInTable.add(rawMap.getSourceSlot());
				lineInTable.add("---");
				lineInTable.add(rawMap.getTargetSlot());
				
				// save this vector in the List specified to the device
				deviceId2VectorList.get(rawMap.getDeviceID()).add(lineInTable);
								
			} // end rawMapping reading
			
			
		}
		else{
			System.out.println("Error! No toolSystemPlugin found at install()");
		}

	}



	private void reinstallToolSystemPlugin(){
		// call this to apply new settings		
	}


	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Device Manager";
		info.vendorName = "Andre Heydt, Marcel Padilla"; 
		info.icon = ImageHook.getIcon("mouse.png");
		info.isDynamic = false;
		return info;
	}

	@Override
	public void install(de.jtem.jrworkspace.plugin.Controller c) throws Exception { // use this type for c since "Controller" in use by jInput
		super.install(c);

		// let the tool know in what plugin it was created in.
		deviceManagerTool = new DeviceManagerTool(this);

		Scene scene = c.getPlugin(Scene.class);
//		tabbedPane = new JTabbedPane();

		// need to access the ToolSystem
		// PROBLEM: tollSystem is still null at this point, so save a reference of the Controller
		// solution: press reset once inside the plugin
		pluginController = c;
		toolSystemPluginFound = false;
		toolSystemPlugin = pluginController.getPlugin(ToolSystemPlugin.class); // this works
		if (toolSystemPlugin!=null) toolSystemPluginFound=true;
		
		// read toolsystemconfig
		initializeTables();
		addDeviceTable();


		//		System.out.println("Device ManagerPlugin was Installed");
		//		System.out.println("tsp is not null = " + tsp!=null);
				ToolSystem toolSys = c.getPlugin(ToolSystemPlugin.class).getToolSystem();
		//		// we need to be able to view the toolSystem for device mapping info
//				JRViewer.getPlugin(ToolSystemPlugin.class).getToolSystem();	
		//		System.out.print("TOOLSYSTEM IS THERE = ");
		//		System.out.println(toolSys!=null);
//				System.out.println(toolSys.config );
		System.out.println( toolSys.getToolSystemConfiguration() );
		//		System.out.println(toolSys.);




		// copied from avatar plugin
		updateComponents(scene);
		installTools();

		scene.addChangeListener(this);



		// put this panel as a subpanel of the VR controls panel. REMOVED so it becomes its own window
		//		VRPanel vp = c.getPlugin(VRPanel.class);
		//		vp.addComponent(getClass(), panel, 4.0, "VR");

	}


	/* (non-Javadoc)
	 * Attempt to copy the functioning avatar plugin to add the tools.
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof Scene) {
			Scene scene = (Scene) e.getSource();
			uninstallTools();
			updateComponents(scene);
			installTools();
		}
	}

	private void updateComponents(Scene scene) {
		//		devices = scene.getDevicesComponent(); // this is how the scenegraphcomponent is linked to the scene
		//DIRTY temporary solution, just use the AvatarScenegraphcomponent.
		devices = scene.getAvatarComponent();	
	}



	private void installTools() {
		// check for existance of our node, then add the tool
		if (devices != null) devices.addTool(deviceManagerTool);

	}

	private void uninstallTools() {
		// check for existance of our node, then remove the tool
		if (devices != null) devices.removeTool(deviceManagerTool);

	}


	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		// TODO Auto-generated method stub
		return View.class;
		//		return null;
	}
}
