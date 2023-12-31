package de.jreality.toolsystem;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.toolsystem.raw.PollingDevice;

/**
 * Polling devices implement PollingDevice, and do their
 * polling in the poll() method. Please write no raw
 * devices that start extra timers.
 * 
 * This class is ONLY for internal usage in the ToolSystem.
 * 
 * @author Steffen Weissmann
 *
 */
class Poller implements Runnable {
	
	private final List<PollingDevice> pollingDevices= Collections.synchronizedList(new LinkedList<PollingDevice>());
	
	private static Poller pollerInstance=new Poller();
	
	static Poller getSharedInstance() {
		return pollerInstance;
	}
	
	private Poller() {
		Thread thread = new Thread(this, "jreality raw device polling");
		thread.setPriority(Thread.NORM_PRIORITY+1);
		thread.start();
	}

	void addPollingDevice(final PollingDevice pd) {
		pollingDevices.add(pd);
	}
	void removePollingDevice(final PollingDevice pd) {
		pollingDevices.remove(pd);
	}
	
	public void run() {
		while (true) { // the thread
			synchronized (pollingDevices) {
				long when = System.currentTimeMillis();
				for (PollingDevice pd : pollingDevices) pd.poll(when); // for all Pollingdevices  in the list, poll() new data
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
}
