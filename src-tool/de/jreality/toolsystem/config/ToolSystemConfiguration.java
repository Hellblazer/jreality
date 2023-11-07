/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.toolsystem.config;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import de.jreality.util.Input;
//jInput
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;


/**
 *
 * TODO: comment this.
 * This is the main code that will read the .xml files that calls  other xml files for each device to set up the interactions.
 * An object of the Type ToolSystemConfiguration contains multiple Lists for RawDeviceConfig, RawMapping, VirtualDeviceConfig, VirtualConstants and VirtualMappings
 * This code is called on every startup.
 * @author weissman
 *
 */
public class ToolSystemConfiguration {

	public static String DEFAULT_TOOLCONFIG = "toolconfig-split.xml"; // this file contains the list of devices. hardcode
	//note: the lists are defined further below

	public static ToolSystemConfiguration loadDefaultConfiguration() {
		try {
			final URL toolconfig = ToolSystemConfiguration.class.getResource(DEFAULT_TOOLCONFIG);
			//System.out.println("tollconfig = " + toolconfig.toString()); returns the file:ABSOLUTE path
			if (toolconfig == null) {
				String text="Resource \"toolconfig.xml\" not found.\n Expected in "+ToolSystemConfiguration.class.getPackage().toString()
						+".\n This is often caused by Eclipse when Preferences->Java->Building->Filtered Resources includes \"*.xml\".";    	  
				// The message is also printed to stderr, because the message of the RuntimeException may be suppressed, 
				// e.g. by de.jreality.util.Secure.doPrivileged()
				System.err.println(text);
				throw new RuntimeException(text);
			}
			
					
			
			// if everything worked fine, load the configuration
			return loadConfiguration(Input.getInput(toolconfig)); // Input.getInput( ) returns an abstract inputmanager, basically an inputstream and some extra info
		} catch (IOException e) {
			throw new Error();
		}
	}

	public static ToolSystemConfiguration loadDefaultDesktopAndPortalConfiguration() throws IOException {
		List<ToolSystemConfiguration> all = new LinkedList<ToolSystemConfiguration>();
		all.add(loadDefaultPortalConfiguration());
		all.add(loadDefaultDesktopConfiguration());
		return merge(all);
	}

	public static ToolSystemConfiguration loadDefaultDesktopConfiguration() throws IOException {
		return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource(DEFAULT_TOOLCONFIG)));
	}

	public static ToolSystemConfiguration loadDefaultDesktopConfiguration(List<Input> additionalInputs) throws IOException {
		if (additionalInputs.isEmpty()) return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource(DEFAULT_TOOLCONFIG)));
		List<ToolSystemConfiguration> all = new LinkedList<ToolSystemConfiguration>();
		all.add(loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource(DEFAULT_TOOLCONFIG))));
		all.add(loadConfiguration(additionalInputs));
		return merge(all);
	}

	public static ToolSystemConfiguration loadDefaultPortalConfiguration() throws IOException {
		return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("chunks/portal/toolconfig-portal.xml")));
	}

	public static ToolSystemConfiguration loadRemotePortalConfiguration() throws IOException {
		return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("chunks/portal/toolconfig-portal-remote.xml")));
	}

	public static ToolSystemConfiguration loadRemotePortalMasterConfiguration() throws IOException {
		return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("chunks/portal/toolconfig-portal-remote-master.xml")));
	}

	public static ToolSystemConfiguration loadDefaultPortalConfiguration(List<Input> additionalInputs) throws IOException {
		if (additionalInputs.isEmpty()) return loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("chunks/portal/toolconfig-"
				+ ""
				+ ".xml")));
		List<ToolSystemConfiguration> all = new LinkedList<ToolSystemConfiguration>();
		all.add(loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource(DEFAULT_TOOLCONFIG))));
		all.add(loadConfiguration(additionalInputs));
		return merge(all);
	}

	/**
	 * given the Input object of an xmlFile, this method returns an object of type "toolsystemconfiguration".
	 * It is intended to read the "main" configuration file that will call device specific configuration files.
	 * 
	 * @param xmlFile
	 * @return
	 * @throws IOException
	 */
	public static ToolSystemConfiguration loadConfiguration(Input xmlFile) throws IOException {

		// padilla: this is where basic jInputs are added
		// idea: maybe just map every single component of the controller onto something just to use it later in the deviceManagerPlugin
//		tempGamePadConfigJInputWriter();
		
		// something needed to read xml.
		TransformerFactory tfactory = TransformerFactory.newInstance(); 
		tfactory.setURIResolver(new URIResolver() {
			public Source resolve(String href, String base) throws TransformerException {
				try {
					URL url = ToolSystemConfiguration.class.getResource(href);
					Input input = url == null ? Input.getInput(href) : Input.getInput(url);
//					System.out.println("resolved: "+input); // TODO remove syso.
					return new StreamSource(input.getInputStream());
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace(); 
				}
				return null;
			}
		});

		// read the toolconfig.xsl file TODO: hardcode warning 
		Input xslt = Input.getInput(ToolSystemConfiguration.class.getResource("toolconfig.xsl"));
		Transformer transformer = null;
		try {
			transformer = tfactory.newTransformer(new StreamSource(xslt.getInputStream()));
		} catch (TransformerConfigurationException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}

		// convert the inputstream to a streamsource
		StreamSource src = new StreamSource(xmlFile.getInputStream());
		DOMResult outResult = null;

		// attempt 5 times to create a DOMResult and to use the transformer to apply the srcxml to it
		for (int i=0; i<5; i++) {
			if (outResult != null) src = new StreamSource(domToInputStream(outResult.getNode()));
			outResult = new DOMResult();
			try {
				transformer.transform(src, outResult);
			} catch (TransformerException e1) {
				// Auto-generated catch block
				e1.printStackTrace();
			} 

			//		System.out.println("After transform: ");
			//		sysoutXML(outResult);

		};  

		// create XML decoder
		XMLDecoder dec = new XMLDecoder(domToInputStream(outResult.getNode()), null,
				new ExceptionListener() {
			public void exceptionThrown(Exception e) {
				e.printStackTrace();
			}
		},
		ToolSystemConfiguration.class.getClassLoader()
				); // end create xml decoder


		// read the ToolSystemConfiguration object
		ToolSystemConfiguration tsconfig = (ToolSystemConfiguration) dec.readObject();



		//		// =============================================================================
		//		// this is where we can add more devices to the toolsystemconfig that are not written in the .xml file.
		//		// e.g. for now: new controllers manageable by jInput. 
		//		// note: make sure to remove the tested controller from the toolconfig-split.xml
		//		// author: padilla Feb 2015
		//
		//		// problem: it seems as if the only way to create a toolsystemconfig object is to read an xml file
		//
		//		// controller detection
		//		// read all devices
		//		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		//		Controller[] controllers = env.getControllers();
		//		if(controllers.length>0) {
		//			Controller controller = controllers[0]; // TODO, for now only bother with one controller
		//
		//			// rawDeviceConfig
		////			Map<String, Object> config = new Map<String, Object>(); // Q: what hashmap?
		////			RawDeviceConfig cRawDevice = new RawDeviceConfig(
		////					"de.jreality.toolsystem.raw.DeviceJinputController",
		////					"Gamepad",
		////					config
		////					) ;
		//
		//
		//			// rawMapping
		//
		//			// virtualDeviceConfig
		//
		//			// virtualConstants
		//
		//			// virtualMapping		
		//
		//		} // end if(controllers.length>0)
		//
		//		// =============================================================================		

		return tsconfig;
	}

	private static void sysoutXML(DOMResult outResult) {
		StringBuffer sb = new StringBuffer(1024);
		domToString(outResult.getNode(), sb, 0);
		System.out.println(sb.toString());
	}

	/**
	 * loadconfiguration that will be called for each element of a list. The result is then merged to one toolsystemconfig
	 * @param inputs
	 * @return
	 * @throws IOException
	 */
	public static ToolSystemConfiguration loadConfiguration(List<Input> inputs) throws IOException {
		List<ToolSystemConfiguration> confs = new LinkedList<ToolSystemConfiguration>();
		for (Input in : inputs) {
			confs.add(loadConfiguration(in));
		}
		return merge(confs);
	}

	/**
	 * merges a list of toolsystemconfigs to one big toolsystemconfig
	 * @param list
	 * @return
	 */
	private static ToolSystemConfiguration merge(List<ToolSystemConfiguration> list) {
		ToolSystemConfiguration result = new ToolSystemConfiguration();
		for (ToolSystemConfiguration conf : list) {
			result.rawConfigs.addAll(conf.rawConfigs);
			result.rawMappings.addAll(conf.rawMappings);
			result.virtualConfigs.addAll(conf.virtualConfigs);
			result.virtualConstants.addAll(conf.virtualConstants);
			result.virtualMappings.addAll(conf.virtualMappings);
		}
		return result;
	}

	// Lists that make up the ToolSystemConfiguration. they contain all relevant mappings and information for the use of devices.
	private List<RawDeviceConfig> rawConfigs = new LinkedList<RawDeviceConfig>();
	private List<RawMapping> rawMappings = new LinkedList<RawMapping>();
	private List<VirtualDeviceConfig> virtualConfigs = new LinkedList<VirtualDeviceConfig>();
	private List<VirtualMapping> virtualMappings = new LinkedList<VirtualMapping>();
	private List<VirtualConstant> virtualConstants = new LinkedList<VirtualConstant>();

	public List<RawDeviceConfig> getRawConfigs() {
		return rawConfigs;
	}
	public List<RawMapping> getRawMappings() {
		return rawMappings;
	}
	public List<VirtualDeviceConfig> getVirtualConfigs() {
		return virtualConfigs;
	}
	public List<VirtualMapping> getVirtualMappings() {
		return virtualMappings;
	}
	public List<VirtualConstant> getVirtualConstants() {
		return virtualConstants;
	}
	public void setRawConfigs(List<RawDeviceConfig> rawConfigs) {
		this.rawConfigs = rawConfigs;
	}
	public void setRawMappings(List<RawMapping> rawMappings) {
		this.rawMappings = rawMappings;
	}
	public void setVirtualConfigs(List<VirtualDeviceConfig> virtualConfigs) {
		this.virtualConfigs = virtualConfigs;
	}
	public void setVirtualMappings(List<VirtualMapping> virtualMappings) {
		this.virtualMappings = virtualMappings;
	}
	public void setVirtualConstants(List<VirtualConstant> virtualConstants) {
		this.virtualConstants = virtualConstants;
	}
	public void addRawDeviceConfig(RawDeviceConfig config) {
		rawConfigs.add(config);
	}
	public void addRawMapping(RawMapping mapping) {
		rawMappings.add(mapping);
	}
	public void addVirtualDeviceConfig(VirtualDeviceConfig config) {
		virtualConfigs.add(config);
	}
	public void addVirtualMapping(VirtualMapping mapping) {
		virtualMappings.add(mapping);
	}
	public void addVirtualConstant(VirtualConstant constant) {
		virtualConstants.add(constant);
	}

	public String toString() {
		// print all relevant information that was found
		StringBuffer sb = new StringBuffer();
		sb.append("RawDevices:\n");
		for (Iterator i = getRawConfigs().iterator(); i.hasNext(); )
			sb.append("\t"+i.next().toString()).append('\n');
		sb.append("\nRawMappings:\n");
		for (Iterator i = getRawMappings().iterator(); i.hasNext(); )
			sb.append("\t"+i.next().toString()).append('\n');
		sb.append("\nVirtualDevices:\n");
		for (Iterator i = getVirtualConfigs().iterator(); i.hasNext(); )
			sb.append("\t"+i.next().toString()).append('\n');
		sb.append("\nVirtualMappings:\n");
		for (Iterator i = getVirtualMappings().iterator(); i.hasNext(); )
			sb.append("\t"+i.next().toString()).append('\n');
		sb.append("\nVirtualConstants:\n");
		for (Iterator i = getVirtualConstants().iterator(); i.hasNext(); )
			sb.append("\t"+i.next().toString()).append('\n');
		sb.append('\n');
		return sb.toString();
	}

	public static InputStream domToInputStream(Node root) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer(1024);
		domToString(root, sb, 0);
		byte[] bytes = sb.toString().getBytes("UTF-8");
		return new ByteArrayInputStream(bytes);
	}

	public static void domToString(Node node, StringBuffer sb, int ind)
	{
		switch(node.getNodeType())
		{
		case Node.ELEMENT_NODE:
			String name=node.getNodeName();
			switch(sb.length()>0? sb.charAt(sb.length()-1): '\n')
			{
			case '>': sb.append('\n'); //missing break is intentional
			case '\n':
				for(int ix=0; ix<ind; ix++) sb.append(' ');
			}
			sb.append('<').append(name);
			NamedNodeMap attr=node.getAttributes();
			if(attr!=null)
			{
				for(int ix=0, n=attr.getLength(); ix<n; ix++)
				{
					final Node a=attr.item(ix);
					if(((Attr)a).getSpecified())
					{
						sb.append(' ').append(a.getNodeName()).append("=\"");
						quote(a.getNodeValue(), sb);
						sb.append('"');
					}
				}
			}
			if(!node.hasChildNodes())
			{
				sb.append("/>");
				return;
			}
			sb.append(">");
			int lastPos=sb.length();
			ind+=2;
			for(Node n=node.getFirstChild(); n!=null; n=n.getNextSibling())
				domToString(n, sb, ind);
			ind-=2;
			if(lastPos<sb.length())
			{
				switch(sb.charAt(sb.length()-1))
				{
				case '>': if(sb.charAt(sb.length()-2)==']') break;
				sb.append('\n'); //missing break is intentional
				case '\n':
					for(int ix=0; ix<ind; ix++) sb.append(' ');
				}
			}
			sb.append("</").append(name).append('>');
			break;
		case Node.TEXT_NODE:
			String text=node.getNodeValue();
			quote(text, sb);
			break;
		case Node.CDATA_SECTION_NODE:
			sb.append("<![CDATA[").append(node.getNodeValue()).append("]]>");
			break;
		case Node.COMMENT_NODE:
			sb.append("<!--").append(node.getNodeValue()).append("-->");
			break;
		case Node.DOCUMENT_NODE:
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			for(Node n=node.getFirstChild(); n!=null; n=n.getNextSibling())
				domToString(n, sb, ind);
			break;
		case Node.DOCUMENT_TYPE_NODE:
			DocumentType type=(DocumentType)node;
			sb.append("<!DOCTYPE ").append(type.getName()).append(' ');
			String spec=type.getPublicId();
			if(spec!=null) sb.append("PUBLIC \"").append(spec).append('"');
			spec=type.getSystemId();
			if(spec!=null) sb.append("SYSTEM \"").append(spec).append('"');
			sb.append(">\n");
			break;
		}
	}

	private static void quote(String text, StringBuffer sb) {
		sb.ensureCapacity(sb.length()+text.length());
		for(int ix=0, num=text.length(); ix<num; ix++)
			switch(text.charAt(ix))
			{
			default: sb.append(text.charAt(ix)); break;
			case '&': sb.append("&amp;"); break;
			case '<': sb.append("&lt;"); break;
			case '>': sb.append("&gt;"); break;
			case '"': sb.append("&quot;"); break;
			}
	}

	
	/**
	 * this is where we can add more devices to the toolsystemconfig that are not written in the .xml file.
	 * e.g. for now: new controllers manageable by jInput. 
	 * note: when it finds a controller, it add the toolconfig-temp-gamepad.xml in the toolconfig-split.xml
	 * author: padilla Feb 2015
	 */
	static public void tempGamePadConfigJInputWriter(){

		// =============================================================================
		// since it seems that controller can only be added by the xml files, we will add a temporary .xml file which is automatically written at startup when an unknown controller was found
		System.out.println("Info: reading controllers and writing a toolconfig-temp-gamepad.xml file if needed");
		
		// controller detection
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		Controller[] controllers = env.getControllers();
		if(controllers.length>0) {// TODO, for now only bother with one controller
			Controller controller = controllers[0]; 
			String GamepadID= "\""+ controller.getName() +"\"";

			// now that one controller has been found, prepare and xml file
			String tempXmlName = "toolconfig-temp-gamepad.xml";
			File tempXmlFile = new File("src-tool/de/jreality/toolsystem/config/chunks/" + tempXmlName); // hardcode

			// create one long string that we will then write into a file
			String xmlString = "";

			// first line
			xmlString+= "<toolconfig>\n\n";

			// rawdevice
			xmlString+= " <rawdevices>\n\n";
			xmlString+= "  <rawdevice id=" + GamepadID + "\n";
			xmlString+= "   type=\"de.jreality.toolsystem.raw.DeviceJinputController\">\n";
			xmlString+= "   <prop name=\"id_string\">\n";
			xmlString+= "    <string>" + controller.getName() + "</string>\n"; // native controller name here. hope no controller has a " in its name
			xmlString+= "   </prop>\n";
			xmlString+= "  </rawdevice>\n";
			xmlString+= "\n </rawdevices>\n\n";

			//rawslots
			xmlString+= " <rawslots>\n\n";
			// now we will map different components to arbritray commands
//			for(Component cmp : controller.getComponents()){
//				if(cmp.isAnalog()){
//					// if its an analog
//					xmlString+= "  <mapping device=\"Gamepad\" src=\""+ cmp.getName()+ "\" target=\"RawX\" />\n";
//				}else {
//					// if button
//					xmlString+= "  <mapping device=\"Gamepad\" src=\""+ cmp.getName()+ "\" target=\"JumpActivation\" />\n";
//				}
//			}
			for(Component cmp : controller.getComponents()){
				if(!cmp.isAnalog()){
					// if button
					xmlString+= "  <mapping device=" + GamepadID + " src=\""+ cmp.getName()+ "\" target=\"JumpActivation\" />\n";
					break;
				}
			}
			for(Component cmp : controller.getComponents()){
				if(cmp.isAnalog()){
					// if its an analog
					xmlString+= "  <mapping device=" + GamepadID + " src=\""+ cmp.getName()+ "\" target=\"RawX\" />\n";
					break;
				}
			}
			
//			xmlString+= "  <mapping device=\"Gamepad\" src=\""+ "x" + "\" target=\"RawX\" />\n";
//			xmlString+= "  <mapping device=\"Gamepad\" src=\""+ "A"+ "\" target=\"JumpActivation\" />\n";
			xmlString+= "\n </rawslots>\n\n";

			// virtual devices
			xmlString+= "\n <virtualdevices>\n\n";
			
			xmlString+= "  <virtualdevice\n";
			xmlString+= "   type=\"de.jreality.toolsystem.virtual.VirtualDeadzoneAxis\">\n";
			xmlString+= "   <inputslot>RawX</inputslot>\n";
			xmlString+= "   <outputslot>ForwardBackwardAxis</outputslot>\n";
			xmlString+= "   <prop name=\"threshold\">\n";
			xmlString+= "    <double>0.25</double>\n";
			xmlString+= "   </prop>\n";
			xmlString+= "  </virtualdevice>\n";
			
			xmlString+= "\n </virtualdevices>\n\n";


			// last line
			xmlString+= "</toolconfig>";




			// flush the entire string into the file
			try {
				setFileFromString(tempXmlFile, xmlString);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			


		} // end if(controllers.length>0)
		
		//---------------------------			
		
		// also modify the toolconfig-split.xml to include the temporary xml file	
		// if no controller was detected, remove the temp file entry by comment
		
		File toolSystemSplitXmlFile = new File("src-tool/de/jreality/toolsystem/config/toolconfig-split.xml"); // hardcode
		String toolSystemSplitString = getStringFromFile(toolSystemSplitXmlFile);
//		System.out.println(toolSystemSplitString);
		
		//check for the presence of a substring and replace it by the commented, uncommented version if necessary
		String disabled ="<!-- import href=\"chunks/toolconfig-temp-gamepad.xml\" /-->";
		String enabled = "<import href=\"chunks/toolconfig-temp-gamepad.xml\" />";
		if(controllers.length > 0)  toolSystemSplitString = toolSystemSplitString.replaceAll( disabled , enabled );
		if(controllers.length == 0) toolSystemSplitString = toolSystemSplitString.replaceAll( enabled , disabled );
		
//		System.out.println(" NEW \n\n\n" + toolSystemSplitString);
		
		// overwrite the old file
		try {
			setFileFromString(toolSystemSplitXmlFile, toolSystemSplitString);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// =============================================================================		

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
	
	 /**
	  * Fetch the entire contents of a text file, and return it in a String.
	  * This style of implementation does not throw Exceptions to the caller.
	  *
	  * @param aFile is a file which already exists and can be read.
	  */
	  static public String getStringFromFile(File aFile) {
	    //...checks on aFile are elided
	    StringBuilder contents = new StringBuilder();
	    
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null; //not declared within while loop
	        /*
	        * readLine is a bit quirky :
	        * it returns the content of a line MINUS the newline.
	        * it returns null only for the END of the stream.
	        * it returns an empty String if two newlines appear in a row.
	        */
	        while (( line = input.readLine()) != null){
	          contents.append(line);
	          contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    return contents.toString();
	  }
	
	


	
	
	
	
	public static void main(String[] args) throws IOException {
		//ToolSystemConfiguration ts = ToolSystemConfiguration.loadConfiguration(Input.getInput(ToolSystemConfiguration.class.getResource("test-single-toolconfigs.xml")));
		ToolSystemConfiguration ts = ToolSystemConfiguration.loadDefaultConfiguration();
		System.out.println(ts);
		System.out.println("==================================");

	}

	

}




