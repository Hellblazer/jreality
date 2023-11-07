package de.jreality.device.jinput.component;

import de.jreality.scene.tool.InputSlot;

public class JInputButton {
	
	private String name;
	private boolean pressed;
	private InputSlot slot;
	
	public JInputButton(String name, boolean pressed, InputSlot slot) {
		this.name = name;
		this.pressed = pressed;
		this.slot = slot;
	}

	public String getName() {
		return name;
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public InputSlot getSlot() {
		return slot;
	}

	public void setSlot(InputSlot slot) {
		this.slot = slot;
	}
}
