package de.jreality.plugin.device;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import de.jreality.ui.LayoutFactory;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
//
public class DeviceInspector extends ShrinkPanelPlugin implements ActionListener{
	
	
//	// this is the plugin that will controll all variables and situations of the curtain simulation
//
//	// sliders to change the wind direction and the wind strength
//	private JSliderVR windDirectionSlider;
//	private JSliderVR windSpeedSlider;
//
//
//	/**
//	 * A timer to simulate the motion it get an ActionListener. If it's running
//	 * it calls after a specified time (in milliseconds) the actionPerformed of
//	 * the listener.
//	 */
//	private Timer timer = new Timer(25, this);
//
//
//
//
	public DeviceInspector() {

//		startTime = System.currentTimeMillis();

		
		shrinkPanel.setLayout(new GridBagLayout());
		// add components
		GridBagConstraints constraint = LayoutFactory.createLeftConstraint();
		int y=0;

		// detect button ==================================
		JButton playPauseButton = new JButton("detect Controllers");
		playPauseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//selectRandomEdge();	
				//kleinerTest();

				System.out.println("test button");
				kleinerTest();
			}
		}); 





		// run create simple hds button ==================================
		JButton runCreateSimpleHDSButton = new JButton("simple hds script");
		runCreateSimpleHDSButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("simple hds button");
//				simpleHDSinitScript();
			}
		}); 

		constraint.gridy=y++;
		shrinkPanel.add(runCreateSimpleHDSButton,constraint);

		// end create simple hds button ==================================

		// new position script button ==================================
		JButton newPositionButton = new JButton("new positions for curtain");
		newPositionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("new position script button");
//				newPositionScript();
			}
		}); 

		constraint.gridy=y++;
		shrinkPanel.add(newPositionButton,constraint);

		// end new positions script button ==================================


		



		//		// run generate curtain script button ==================================
		//		JButton runGenerateCurtainButton = new JButton("generate curtain");
		//		runGenerateCurtainButton.addActionListener(new ActionListener() {
		//
		//			@Override
		//			public void actionPerformed(ActionEvent e) {
		//				System.out.println("generate curtain button");
		//				curtainInitScript();
		//			}
		//		}); 
		//
		//		constraint.gridy=y++;
		//		shrinkPanel.add(runGenerateCurtainButton,constraint);
		//
		//		// end run default script button ==================================



		/*//////////////////////////////////////
		 * // This spinner defines the up & down clickable selection of functions.
		 // in EdgeOperationUtility.getOperations(), a list of strings is returned containing all the methods of a class  
		final LabeledListSpinner EdgeSpinner = new LabeledListSpinner("Edge Operation:", EdgeOperationUtility.getOperations());
		EdgeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				operation = EdgeSpinner.getStringValue();
				System.out.println("New Edge Operation: " + operation);
			}
		});

		constraint.gridy = y++;
		shrinkPanel.add(EdgeSpinner,constraint);
		 */ //////////////////

		/*////////////////////////////////////
		 // the button that will use the string "operation" to go into a switch case to then read then execute that operation

		JButton vButton = new JButton("Apply Edge Operation");
		vButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				performAction(operation);
			}
		});
		constraint.gridy=y++;
		shrinkPanel.add(vButton,constraint);
		 */////////////////////////////////////////

	}

	private void kleinerTest(){

	} 

	
	
//
//	@Override
//	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
//		return View.class;
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent arg0) {
//		// TODO Auto-generated method stub
//		if(play){
//
//			long deltaTimeMilliseconds=17; // 60FPS
//			if(playAfterBreak){
//				playAfterBreak = false;
//				lastTime = System.currentTimeMillis();
//			} else {
//				deltaTimeMilliseconds = System.currentTimeMillis() - lastTime;
//				lastTime = System.currentTimeMillis();
//			}
//			// convert deltaTimeMili to sec
//			double deltaTimeSec = deltaTimeMilliseconds*0.001;//*timeScale, timescale now inside Curtain;
//
//
//			System.out.println("Elapsed time was " + deltaTimeMilliseconds + " miliseconds. In seconds = " + deltaTimeSec);
//
//			//		System.out.println("---------------");
//			//time= time + 3.14/1200;
//			//		double angle= time;
//			//		Curtain.tiltedPosition(angle);
//			curtainObject.updateSimulation(deltaTimeSec);//0.01);//deltaTimeSec);//0.01);
//			Curtain.copyUpdateGeomtryPositions();
//			halfEdgeInterFaceObject.update();
//
//		}// end play
//
	

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
