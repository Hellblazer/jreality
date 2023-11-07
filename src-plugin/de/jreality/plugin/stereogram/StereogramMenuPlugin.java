package de.jreality.plugin.stereogram;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class StereogramMenuPlugin extends SceneShrinkPanel {
	
	Box editorInspector = null;
	String folderPath = ""; 
	String imagePath = "./oldPaperTexture.jpg";
	
	public void install(Controller c) throws Exception{	
		super.install(c);
		c.getPlugin(StereogramPlugin.class);
		View v = c.getPlugin(View.class);
		v.setShowBottom(false);
		v.setShowLeft(true);
		v.setShowRight(false);
		v.setShowTop(false);
		editorInspector = (Box) getInspector(c);
		shrinkPanel.add(editorInspector);
	}
	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Stereogram Options");
	}
	
	public String getTexturePath() {
		return imagePath;
	}
	
	LinkedList<Component> texStereoComponents = new LinkedList<Component>();
	final JRadioButton ts  = new JRadioButton("Texture Stereogram");
	final JRadioButton rds = new JRadioButton("Random Dot Stereogram");
	final JSlider numSlices = new JSlider(JSlider.HORIZONTAL, 0, 20, 6);
	final JSlider depthFactor = new JSlider(JSlider.HORIZONTAL, 0, 400, 200);
	final JSlider backgroundSpeed = new JSlider(JSlider.HORIZONTAL, 0, 300, 0);
	final JTextField xZoom = new JTextField("1.0", 5);
	final JTextField yZoom = new JTextField("1.0", 5);
	final JRadioButton oldPaper = new JRadioButton("Old Paper");
	final JRadioButton colorExpl = new JRadioButton("Color Explosion");
	final JRadioButton stone = new JRadioButton("Stone");
	final JRadioButton churchWall = new JRadioButton("ChurchWall");
	final JRadioButton diamonds = new JRadioButton("Diamonds");
	final JRadioButton ownTexture = new JRadioButton("Texture File Path:");
	final JTextField pathField = new JTextField(imagePath);
	final JCheckBox showEdges = new JCheckBox("Show Quirky Lines");
	final JLabel label1 = new JLabel("Number of Slices");
	final JLabel label2 = new JLabel("Depth Factor");
	final JLabel label3 = new JLabel("Background Speed");
	final JLabel label4 = new JLabel("Zoom in x-Axis:  ");
	final JLabel label5 = new JLabel("Zoom in y-Axis:  ");
	public Component getInspector(final Controller c) {
		
		Box inspector = Box.createVerticalBox();		
		final Appearance rootApp = c.getPlugin(Scene.class).getRootAppearance();

		ButtonGroup buttonGroup = null;
		JPanel pane = null;
		JPanel littlePane = null;


		//--------------------------------------------------------------
		
		pane = new JPanel();
		pane.setBorder(BorderFactory.createRaisedBevelBorder());
		JCheckBox stereogramMode = new JCheckBox("Stereogram Mode");
		stereogramMode.setSelected(true);
		stereogramMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if ( ((JCheckBox) ae.getSource()).isSelected() ) {
					c.getPlugin(View.class).getViewer().selectViewer(1);
					for (Component comp: getTexStereoComponents())
						comp.setEnabled(true);
					for (Component comp: getOtherComponents())
						comp.setEnabled(true);
					ts.setEnabled(true);
					rds.setEnabled(true);
				} else {
					c.getPlugin(View.class).getViewer().selectViewer(0);
					for (Component comp: getTexStereoComponents())
						comp.setEnabled(false);
					for (Component comp: getOtherComponents())
						comp.setEnabled(false);
					ts.setEnabled(false);
					rds.setEnabled(false);
				}
			}
		});
		pane.add(stereogramMode);
		inspector.add(pane);
		
		//--------------------------------------------------------------
		
		
		
		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS ));
		
		buttonGroup = new ButtonGroup();
		ts.setSelected(true);
		ts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_RANDOMDOT, false);
				for (Component comp: getTexStereoComponents())
					comp.setEnabled(true);
			}		
		});
		buttonGroup.add(ts);
		pane.add(ts);
		
		rds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {			
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_RANDOMDOT, true);
				System.out.println("Random dot stereogram!");
				for (Component comp: getTexStereoComponents())
					comp.setEnabled(false);
				backgroundSpeed.setValue(100);
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_BACKGROUNDSPEED, backgroundSpeed.getValue());
			}		
		});
		buttonGroup.add(rds);
		pane.add(rds);
		
		
		//--------------------------------------------------------------
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
		
		
	
		pane.add(label1);
		numSlices.setMajorTickSpacing( 5 );
		numSlices.setMinorTickSpacing( 1 );
		numSlices.setPaintTicks ( true );
		numSlices.setPaintLabels( true );
		numSlices.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_NUM_SLICES, 2*((JSlider) e.getSource()).getValue());
				System.out.println("Number of Slices changed to " + ((JSlider) e.getSource()).getValue() + " !");
			}
		});
		pane.add(numSlices);
	
		
		
		//--------------------------------------------------------------
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
				
		
		
		pane.add(label2);
		depthFactor.setMajorTickSpacing( 100 );
		depthFactor.setMinorTickSpacing( 20 );
		depthFactor.setPaintTicks ( true );
		depthFactor.setPaintLabels( true );
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( 0 ), new JLabel("0.0") );
		labelTable.put( new Integer( 100 ), new JLabel("1.0") );
		labelTable.put( new Integer( 200 ), new JLabel("2.0") );
		labelTable.put( new Integer( 300 ), new JLabel("3.0") );
		labelTable.put( new Integer( 400 ), new JLabel("4.0") );
		depthFactor.setLabelTable( labelTable );
		depthFactor.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_DEPTHFACTOR, slider.getValue());
				System.out.println("Depth Value changed to " + slider.getValue() + " !");
			}
		});
		pane.add(depthFactor);
				
		
		
		//--------------------------------------------------------------
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
		
		
		
		
		pane.add(label3);
		backgroundSpeed.setMajorTickSpacing( 100 );
		backgroundSpeed.setMinorTickSpacing( 50 );
		backgroundSpeed.setPaintTicks ( true );
		backgroundSpeed.setPaintLabels( true );
		labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( 0 ), new JLabel("0.0") );
		labelTable.put( new Integer( 100 ), new JLabel("1.0") );
		labelTable.put( new Integer( 200 ), new JLabel("2.0") );
		labelTable.put( new Integer( 300 ), new JLabel("3.0") );
		backgroundSpeed.setLabelTable( labelTable );
		backgroundSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_BACKGROUNDSPEED, backgroundSpeed.getValue());
				System.out.println("Background Speed changed to " + backgroundSpeed.getValue() + " !");
			}
		});
		pane.add(backgroundSpeed);
		
		
		
		//--------------------------------------------------------------
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
				
			
		
		littlePane = new JPanel();		
		littlePane.add(label4);
		xZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_XZOOM, (float) 1.0/Float.parseFloat(xZoom.getText()));
				System.out.println("X-Zoom changed to " + 1.0/Float.parseFloat(xZoom.getText()) + " !");
				
			}
		});
		littlePane.add(xZoom);
		pane.add(littlePane);		
		
		littlePane = new JPanel();
		littlePane.add(label5);
		yZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_YZOOM, (float) 1.0/Float.parseFloat(yZoom.getText()));
				System.out.println("Y-Zoom changed to " + (float) 1.0/Float.parseFloat(yZoom.getText()) + " !");
				
			}
		});
		littlePane.add(yZoom);
		pane.add(littlePane);
		

		
		//--------------------------------------------------------------
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
				

		
		buttonGroup = new ButtonGroup();
		oldPaper.setSelected(true);
		oldPaper.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				imagePath = folderPath + "./oldPaperTexture.jpg";
				c.getPlugin(StereogramPlugin.class).changeImage(imagePath);
				xZoom.setText("1.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_XZOOM, (float) 1.0/Float.parseFloat(xZoom.getText()));
				yZoom.setText("1.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_YZOOM, (float) 1.0/Float.parseFloat(yZoom.getText()));
			}		
		});
		buttonGroup.add(oldPaper);
		pane.add(oldPaper);

		colorExpl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				imagePath = folderPath + "./colorExplosion.jpg";	
				c.getPlugin(StereogramPlugin.class).changeImage(imagePath);
				xZoom.setText("15.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_XZOOM, (float) 1.0/Float.parseFloat(xZoom.getText()));
				yZoom.setText("4.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_YZOOM, (float) 1.0/Float.parseFloat(yZoom.getText()));
				backgroundSpeed.setValue(35);
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_BACKGROUNDSPEED, backgroundSpeed.getValue());
			}		
		});
		buttonGroup.add(colorExpl);
		pane.add(colorExpl);
		
		stone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				imagePath = folderPath + "./stoneTexture.jpg";
				c.getPlugin(StereogramPlugin.class).changeImage(imagePath);
				xZoom.setText("1.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_XZOOM, (float) 1.0/Float.parseFloat(xZoom.getText()));
				yZoom.setText("1.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_YZOOM, (float) 1.0/Float.parseFloat(yZoom.getText()));
			}		
		});
		buttonGroup.add(stone);
		pane.add(stone);
		
		churchWall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				imagePath = folderPath + "./churchwallTexture.jpg";
				c.getPlugin(StereogramPlugin.class).changeImage(imagePath);
				xZoom.setText("1.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_XZOOM, (float) 1.0/Float.parseFloat(xZoom.getText()));
				yZoom.setText("1.5");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_YZOOM, (float) 1.0/Float.parseFloat(yZoom.getText()));
				backgroundSpeed.setValue(100);
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_BACKGROUNDSPEED, backgroundSpeed.getValue());
			}		
		});
		buttonGroup.add(churchWall);
		pane.add(churchWall);
		
		diamonds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				imagePath = folderPath + "./diamondsTexture.jpg";			
				c.getPlugin(StereogramPlugin.class).changeImage(imagePath);
				xZoom.setText("4.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_XZOOM, (float) 1.0/Float.parseFloat(xZoom.getText()));
				yZoom.setText("1.0");
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_YZOOM, (float) 1.0/Float.parseFloat(yZoom.getText()));
				backgroundSpeed.setValue(100);
				rootApp.setAttribute(CommonAttributes.STEREOGRAM_BACKGROUNDSPEED, backgroundSpeed.getValue());
			}		
		});
		buttonGroup.add(diamonds);
		pane.add(diamonds);
		
//		ownTexture.setSelected(true);
		ownTexture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(ownTexture.isSelected())
					pathField.setEnabled(true);		
			}
		});
		buttonGroup.add(ownTexture);
		pane.add(ownTexture);
		pathField.setEnabled(false);
		pathField.setText(" i.g. D:\\MyPictures\\MyTexture");		
		//pathField.setText("D:\\EigeneDateien\\Eigene Bilder\\EnhancedReality.jpg");			
		pathField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imagePath = pathField.getText();			
				c.getPlugin(StereogramPlugin.class).changeImage(imagePath);
			}
		});
		pane.add(pathField);
				

		
		//--------------------------------------------------------------
		pane.add(Box.createRigidArea(new Dimension(0,3)));
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
		
		
		
		showEdges.setSelected(false);
		showEdges.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent ae) {
				if ( ((JCheckBox) ae.getSource()).isSelected() ) {
					rootApp.setAttribute(CommonAttributes.STEREOGRAM_SHOWEDGES, true);
					System.out.println("Show Lines!");
				} else {
					rootApp.setAttribute(CommonAttributes.STEREOGRAM_SHOWEDGES, false);
					System.out.println("Don't show lines!");
				}
			}		
		});
		pane.add(showEdges);
		
		
		
		//--------------------------------------------------------------
		pane.add(Box.createRigidArea(new Dimension(0,3)));
		pane.add(new JSeparator(SwingConstants.HORIZONTAL));
				
				
					
		inspector.add(pane);	
		return inspector;
	}
	
	private LinkedList<Component> getTexStereoComponents() {
		LinkedList<Component> list = new LinkedList<Component>();
		list.add(xZoom);
		list.add(yZoom);
		list.add(oldPaper);
		list.add(colorExpl);
		list.add(stone);
		list.add(churchWall);
		list.add(diamonds);
		list.add(xZoom);
		list.add(yZoom);
		list.add(ownTexture);
		list.add(pathField);
		list.add(label4);
		list.add(label5);
		return list;
	}

	private LinkedList<Component> getOtherComponents() {
		LinkedList<Component> list = new LinkedList<Component>();
		list.add(numSlices);
		list.add(depthFactor);
		list.add(backgroundSpeed);
		list.add(showEdges);
		list.add(label1);
		list.add(label2);
		list.add(label3);		
		return list;
	}	
}