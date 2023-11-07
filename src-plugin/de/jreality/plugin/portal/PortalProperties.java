package de.jreality.plugin.portal;

import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * This class is a wrapper for @see de.jreality.portal.PortalCoordinateSystem.
 * It is used to get src-plugin independent of src-portal
 *  
 * @author Heydt
 *
 */
public class PortalProperties {
	
	static double portalScale = 1.0;
	static Vector<ActionListener> listeners = new Vector<ActionListener>();

	public static double getPortalScale() {
		return portalScale;
	}
	
	public static void setPortalScale(double pScale){
		portalScale = pScale;
	}
	
	public static void setListeners(Vector<ActionListener> l){
		listeners = l;
	}
	
	public static void addChangeListener(ActionListener l)	{
		if (listeners.contains(l)) return;
		listeners.add(l);
	}
	
	public static void removeChangeListener(ActionListener l){
		listeners.remove(l);
	}

}
