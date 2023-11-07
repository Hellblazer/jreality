package de.jreality.device;

import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEventQueue;


/**
 * 
 * @author heydt
 *
 */
public interface RawDevice {
	
	public void setEventQueue(ToolEventQueue queue);
    
    /**
	 * 
     * @param identifier
     * @param inputDevice
     */
    public void setIdentifier(String identifier, InputSlot inputDevice);
    
    public InputSlot getIdentifier(String identifier);
    
	public void dispose();
	public String getName();

}
