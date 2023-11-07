package de.jreality.device;

import java.util.Map;

import net.java.games.input.Component;
import net.java.games.input.Controller;

public class DeviceJinput extends Device{
	
	private Controller controller;
	private Component[] components;
	private Map<String,Integer> componentMap;
	
	public DeviceJinput(Controller ctrl) {
		super(ctrl.getName());
		controller = ctrl;
		components = controller.getComponents();
		
	}
	

}
