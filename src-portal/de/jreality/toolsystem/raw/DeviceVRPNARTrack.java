package de.jreality.toolsystem.raw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.util.LoggingSystem;

/**
 * 
 * Trackers (matrices), buttons and analogs are available 
 * 
 * @author Andre Heydt, Emiliano Pastorelli, Heiko Hermann
 *
 */
public class DeviceVRPNARTrack implements RawDevice, PollingDevice {
	int numTrackers, numButtons, numAnalogs;
	String device;
	protected ToolEventQueue queue;
	private double EPS = 10E-4;

	private DeviceVRPNButton VRPNButtonDevice;
	private DeviceVRPNAnalog VRPNAnalogDevice;
	private DeviceVRPNTracker VRPNTrackerDevice;

	private double[][] trackerMatrix; 
	private int[] buttonStates; 
	private double[] analogValues;

	public String getName() {
		return "VRPN ARTrack driver";
	}

	public void initialize(Viewer viewer, Map<String, Object> config) {
		System.out.println("init");
		if(config.containsKey("num_trackers"))numTrackers = (Integer)config.get("num_trackers");
		else{ System.out.println("trackers"); LoggingSystem.getLogger(this).warning("not using default trackers");}
		if(config.containsKey("num_buttons")) numButtons = (Integer)config.get("num_buttons");
		else{ System.out.println("buttons"); LoggingSystem.getLogger(this).warning("not using default buttons");}
		if(config.containsKey("num_analogs")) numAnalogs = (Integer)config.get("num_analogs");
		else{ System.out.println("analogs"); LoggingSystem.getLogger(this).warning("not using default analogs");}

		buttonStates = new int[numButtons];
		analogValues = new double[numAnalogs];
		trackerMatrix = new double[numTrackers][16];

		System.out.println("VRPN ARTrack: trackers="+numTrackers+" buttons="+numButtons+" analogs="+numAnalogs);
		if(config.containsKey("id_string")) {
			device = (String) config.get("id_string");
			System.out.println("Device "+ device );
			VRPNButtonDevice = new DeviceVRPNButton(device, numButtons);
			VRPNAnalogDevice = new DeviceVRPNAnalog(device, numAnalogs);
			VRPNTrackerDevice = new DeviceVRPNTracker(device, numTrackers);
		}

	}

	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		String[] split = rawDeviceName.split("_");
		int index = Integer.parseInt(split[1]);
		if ("tracker".equals(split[0])) {
			if (index >= numTrackers) throw new IllegalArgumentException("unknown tracker: "+index);
			double[] sensorMatrix = VRPNTrackerDevice.getMatrix(index);
			// register slot:
			trackerMatrix[index]= sensorMatrix;
			VRPNTrackerDevice.setSlot(index, inputDevice);
			// read initial value
			return new ToolEvent(this, System.currentTimeMillis(), inputDevice, null, new DoubleArray(sensorMatrix));
		} else if ("button".equals(split[0])) {
			if (index >= numButtons) throw new IllegalArgumentException("Unknown button: "+index);
			//register slot:
			int buttonState = VRPNButtonDevice.getState(index);
			buttonStates[index]=buttonState;
			VRPNButtonDevice.setSlot(index, inputDevice);
			return new ToolEvent(this,  System.currentTimeMillis(), inputDevice, buttonState == 0 ? AxisState.ORIGIN : AxisState.PRESSED, null);
		} else if ("analog".equals(split[0])) {
			if (index >= numAnalogs) throw new IllegalArgumentException("unknown analog: "+index);
			double value = VRPNAnalogDevice.getValue(index);
			VRPNAnalogDevice.setSlot(index, inputDevice);
			return new ToolEvent(this, System.currentTimeMillis(), inputDevice, new AxisState(value), null);
		} else {
			throw new IllegalArgumentException("unknown trackd device: "+rawDeviceName);
		}
	}

	public void setEventQueue(ToolEventQueue queue) {
		this.queue=queue;
	}


	public synchronized void poll(long when) {

		// count tps and print to file
//		tpsCounter();

		//poll
		for(int t=0;t<trackerMatrix.length;t++){
			counterOfTracks++;
			InputSlot slot = VRPNTrackerDevice.getSlot(t);
			double[]matrix = VRPNTrackerDevice.getMatrix(t);
			ToolEvent te = new MyToolEvent(this, when, slot, null, new DoubleArray(matrix));
			if (queue != null) queue.addEvent(te);
			//			if (queue != null) queue.addFirstEvent(te);
			else System.out.println(te);
		}
		for(int b=0;b<buttonStates.length;b++){
			int bs = buttonStates[b];
			int state = VRPNButtonDevice.getState(b);
			if(bs != state){
				buttonStates[b] =  state;
				InputSlot slot = VRPNButtonDevice.getSlot(b);
				ToolEvent te = new ToolEvent(this, when, slot, state == 0 ? AxisState.ORIGIN : AxisState.PRESSED, null);
				if (queue != null) queue.addEvent(te);
				else System.out.println(te);
			}
		}
		for(int i=0;i<analogValues.length;i++){
			double av = analogValues[i];
			double newVal = VRPNAnalogDevice.getValue(i);
			if(StrictMath.abs(av - newVal) > EPS){
				analogValues[i] = newVal;
				InputSlot slot = VRPNAnalogDevice.getSlot(i);
				ToolEvent te = new ToolEvent(this, when, slot, new AxisState(newVal), null);
				if (queue != null) queue.addEvent(te);
				else System.out.println(te);
			}
		}
	}

	static class MyToolEvent extends ToolEvent {

		private static final long serialVersionUID = -8503410127439268525L;

		public MyToolEvent(Object source, long when, InputSlot device, AxisState axis, DoubleArray trafo) {
			super(source, when, device, axis, trafo);
		}

		protected boolean compareTransformation(DoubleArray trafo1, DoubleArray trafo2) {
			return true;
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}




	//================= FOR DEBUG ============
	// set up counters to see TPS
	int counterOfTracks=0;
	long counterStartTime=0;
	// but since we can't find the logs, lets make our own
	int nrOfLinesInTPSLog =0;
	String tpsLogString ="";
	boolean stopLogging=false;


	
	public void tpsCounter(){
		
		//crash the system to see if it gets here
		//int[] crash = new int[1]; crash[2]=1; //result vrpn std setup does not call this
		
		if(!stopLogging){
			//Tracks per second counters
			if(counterStartTime<1) counterStartTime = System.currentTimeMillis();
			if(System.currentTimeMillis()-counterStartTime > 1000 ){ // 1 sec
				tpsLogString += "TPS = "+ counterOfTracks + " in time = " + (System.currentTimeMillis()-counterStartTime) + "\n";
				counterStartTime = System.currentTimeMillis();
				counterOfTracks=0;
				nrOfLinesInTPSLog++;
			}
			if(nrOfLinesInTPSLog > 100){
				// only print to file if we have enough intel worthy to print
				File logFile = new File("test/" + "TPSlog"); // hardcode
				try {
					setFileFromString(logFile, tpsLogString);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				stopLogging=true;

			}
		}
	}

	// for manipulating the xml files. taken from http://www.javapractices.com/topic/TopicAction.do?Id=42 TODO license
	/**
	 * Change the contents of text file in its entirety, overwriting any
	 * existing text.
	 *
	 * This style of implementation throws all exceptions to the caller.
	 *
	 * @param aFile is an existing file which can be written to.
	 * @throws IllegalArgumentException if param does not comply.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if problem encountered during write.
	 */
	static public void setFileFromString(File aFile, String aContents)
			throws FileNotFoundException, IOException {
		if (aFile == null) {
			throw new IllegalArgumentException("File should not be null.");
		}
		if (!aFile.exists()) {
			throw new FileNotFoundException ("File does not exist: " + aFile);
		}
		if (!aFile.isFile()) {
			throw new IllegalArgumentException("Should not be a directory: " + aFile);
		}
		if (!aFile.canWrite()) {
			throw new IllegalArgumentException("File cannot be written: " + aFile);
		}

		//use buffering
		Writer output = new BufferedWriter(new FileWriter(aFile));
		try {
			//FileWriter always assumes default encoding is OK!
			output.write( aContents );
		}
		finally {
			output.close();
		}
	}
}

