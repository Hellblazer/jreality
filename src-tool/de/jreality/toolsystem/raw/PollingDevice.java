package de.jreality.toolsystem.raw;

/**
 * Polling devices implement this interface and do their
 * polling in the poll() method. Please write no raw
 * devices that start extra timers.
 * 
 * @author Steffen Weissmann
 *
 */
public interface PollingDevice {

	/**
	 * Perform polling for the device in this method.
	 * This means for e.g. a controller: check all the states of the components and every time a change
	 *  has been found, add a ToolEvent to the ToolEventQueue that is shared with the ToolSystem.
	 * @param when 
	 */
	void poll(long when);
	
}
