package de.jreality.device;

import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEventQueue;

/**
 * 
 * @author heydt
 *
 */
public class Device implements RawDevice {
	
	private String name;
	
//	public Device(){
//		this.name = "";
//	}
	
	public Device(String name){
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setEventQueue(ToolEventQueue queue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIdentifier(String identifier, InputSlot inputDevice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputSlot getIdentifier(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
