package edu.arsl.intgra;

import java.io.File;

import org.apache.commons.io.IOUtils;

public class RunProcess {
	public static int runCDGenerator(int numOfCells) {
		String inputFile = FileManagement.getCDInputFileName();
		int returnVal = 0;
		if(inputFile != null) {
			
			//Running a Python script. It is assumed that Python is intalled on the server and is in the path
			String cmdLine = "Python ";
			
			//providing the script name to run
			cmdLine+= PropertiesFile.getSystemRoot()+"cell/co2_cd_generator.py ";
			
			String cellsPath = PropertiesFile.getInstance().getFullyQualifiedGenertorDir();
			//parameters to the script
			cmdLine += "-i " + cellsPath+"/"+inputFile + " -o " + cellsPath+"/" +PropertiesFile.getInstance().getProperty("output_file_name") + " -w " + numOfCells;
			cmdLine += " -r "+ PropertiesFile.getSystemRoot()+"cell/co2_rules.txt -bv 500";
			try {
				Runtime runtime = Runtime.getRuntime();
		        Process process = runtime.exec(cmdLine, null, new File(PropertiesFile.getSystemRoot()+"cell"));
		        process.waitFor();
		        returnVal = process.exitValue();
		        System.out.println("Python finished with the message - "+IOUtils.toString(process.getErrorStream(), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return returnVal;
	}
	
	public static boolean callRISE() {
		RiseClient client = new RiseClient();
		String fileName = PropertiesFile.getInstance().getProperty("input_file_name");
		
		String framework = PropertiesFile.getInstance().getProperty("RISE_frameworkname");
		//first delete previous framework
		client.DeleteFramework(framework);
		
		int retVal = client.PutXMLFile( framework, fileName);
		if(retVal >= 200 && retVal <=202) {
			retVal = client.PostZipFile(framework+"?zdir=in", fileName);
			if(retVal >= 200 && retVal <=202)
				retVal = client.PutFramework(framework+"/simulation");
		}
		return (retVal >= 200 && retVal <=202);
	}
}
