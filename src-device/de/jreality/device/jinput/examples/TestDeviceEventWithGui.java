package de.jreality.device.jinput.examples;

import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import de.jreality.util.NativePathUtility;
/**
 * Displays all jinput device actions. 
 * Each device gets his own frame.
 * @author Heydt
 *
 */
public class TestDeviceEventWithGui implements Runnable{
	private final static long TIMESTEP = 1;
	private Thread anim;
	
	private Controller c;
	private Component cmps[];
	private JFrame frame;
	private int numAnalogs, numButtons;
	private JRadioButton[] rbButtons;
	private HashMap<String,Integer> buttonMap;
	private JTable tAnalogs;
	private HashMap<String,Integer> analogMap;
	
	public TestDeviceEventWithGui(Controller ctrl) {
		c = ctrl;
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame(c.getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		frame.getContentPane().add(panel);
		
		analogMap = new HashMap<String, Integer>();
		buttonMap = new HashMap<String, Integer>();
		
		numAnalogs = 0;
		numButtons = 0;
		cmps = c.getComponents();
		for(Component cmp : cmps){
			if(cmp.isAnalog()){
				analogMap.put(cmp.getName(), numAnalogs);
				numAnalogs++;
			} else {
				buttonMap.put(cmp.getName(), numButtons);
				numButtons++;
			}
		}
		
		//Buttons
		rbButtons = new JRadioButton[numButtons];
		JPanel pButtons = new JPanel(new GridLayout(1, 0));
		pButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Buttons"), BorderFactory.createEmptyBorder(
				5, 5, 5, 5)));
		for (int i = 0; i < numButtons; i++) {
			rbButtons[i] = new JRadioButton();
			rbButtons[i].setEnabled(false);
			pButtons.add(rbButtons[i]);
		}
		panel.add(pButtons);
		//Analogs
		tAnalogs = new JTable(1,numAnalogs);
		JPanel ptAnalogs = new JPanel();
		ptAnalogs.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Analogs"), BorderFactory.createEmptyBorder(0,
				0, 0, 0)));
		ptAnalogs.add(tAnalogs);
		panel.add(ptAnalogs);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public void run() {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		while (Thread.currentThread() == anim) {
			try {
				update();
				Thread.sleep(TIMESTEP);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	private void update() {
		c.poll();
		EventQueue queue = c.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {
			Component cmp = event.getComponent();
			if (cmp.isAnalog()){
				double value = event.getValue();
				int idx = analogMap.get(cmp.getName());
				tAnalogs.setValueAt(value, 0, idx);
			} else {
				int idx = buttonMap.get(cmp.getName());
				boolean b = rbButtons[idx].isSelected();
				rbButtons[idx].setSelected(!b);
			}
				
		}
	}
	
	public void start() {
		if (anim == null)
			anim = new Thread(this);
		anim.start();
	}

	public void stop() {
		anim = null;
	}

	public static void main(String[] args) {
		NativePathUtility.set("jni");
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		final Controller[] controllers = env.getControllers();
		for (Controller ctrl : controllers) {
//			System.out.println("Controller: " + ctrl.getName());
//			System.out.println("Devicetype: " + ctrl.getType());
//			System.out.println("Identifiers: " + Arrays.toString(ctrl.getComponents()));
			TestDeviceEventWithGui tdewg = new TestDeviceEventWithGui(ctrl);
			tdewg.start();
		}
	}
}
