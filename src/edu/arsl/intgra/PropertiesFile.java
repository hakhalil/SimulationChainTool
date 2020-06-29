package edu.arsl.intgra;

import java.io.FileReader;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * File to manage the reading/writing of the config file
 * This is a singlton wihtin the system
 * @author Hoda
 *
 */
public class PropertiesFile {

	Properties properties = null;
	static PropertiesFile _instance = null;
	static String systemRoot = null;
	
	final static Logger log = LogManager.getLogger(PropertiesFile.class);

	/**
	 * Private constructor to allow only for the initializing method to create the class
	 */
	private PropertiesFile() {
		try {
			String path = PropertiesFile.class.getResource("").getPath();
			
			//Trying to offset the path to where WebContent folder is. The resource path is giving 
			//a value relative to the .class location.
			
			int indexOfWebContent = path.lastIndexOf("WEB-INF");
			path = path.substring(0, indexOfWebContent);
			String propertyFilePath = path+"WEB-INF/config.properties";
			FileReader reader = new FileReader(propertyFilePath);
			
			properties = new Properties();
			properties.load(reader);
			
			systemRoot = path.substring(1, path.length());
				
		} catch (Exception e) {
			log.fatal("Failed to create properties file", e);
		}
	}
	
	public static String getSystemRoot() {
		return systemRoot;
	}

	/**
	 * Initializing instance for the Singelton
	 * @return
	 */
	public static PropertiesFile getInstance() {
		if (_instance == null) {
			_instance = new PropertiesFile();
		}
		return _instance;
	}

	/**
	 * return the property value for the given property name
	 */
	public String getProperty(String name) {
		return (properties != null) ? properties.getProperty(name) : null;
	}
	

}
