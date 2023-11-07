package de.jreality.device.jinput.component;

import de.jreality.scene.tool.InputSlot;

public class JInputAnalog {
	private String name;
	private double value;
	private InputSlot slot;
	
	public JInputAnalog(String name, double value, InputSlot slot) {
		this.name = name;
		this.value = value;
		this.slot = slot;
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public InputSlot getSlot() {
		return slot;
	}

	public void setSlot(InputSlot slot) {
		this.slot = slot;
	}
}
