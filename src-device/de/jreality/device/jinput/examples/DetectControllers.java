package de.jreality.device.jinput.examples;

import java.util.Arrays;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import de.jreality.util.NativePathUtility;

/**
 * prints all jinput devices, types, buttons, analogs and identifiers
 * @author Heydt
 */

public class DetectControllers {
	public static void main(String[] args) {
		NativePathUtility.set("jni");
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		Controller[] controllers = env.getControllers();
		for (Controller ctrl : controllers) {
			System.out.println("Controller: " + ctrl.getName());
			System.out.println("Devicetype: " + ctrl.getType());
			int numAnalogs =0;
			int numButtons = 0;
			for(Component cmp : ctrl.getComponents()){
				if(cmp.isAnalog())
					numAnalogs++;
				else numButtons++;
			}
			System.out.println("Device has " + numButtons + " Buttons and " + numAnalogs + " Analogs");
			System.out.println("Identifiers: " + Arrays.toString(ctrl.getComponents()));
		}
	}
}
