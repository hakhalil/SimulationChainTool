package edu.arsl.intgra;

import java.io.File;
import java.io.IOException;

public class RunProcess {
	public static int runCDGenerator(int numOfCells) {
		String inputFile = FileManagement.getCDInputFileName();
		int returnVal = 0;
		if(inputFile != null) {
			
			//Running a Python script. It is assumed that Python is intalled on the server and is in the path
			String cmdLine = "Python ";
			
			//providing the script name to run
			cmdLine+= PropertiesFile.getSystemRoot()+"cell/cd_generator.py ";
			
			String cellsPath = PropertiesFile.getInstance().getFullyQualifiedGenertorDir();
			//parameters to the script
			cmdLine += "-i " + cellsPath+"/"+inputFile + " -o " + cellsPath+"/" +PropertiesFile.getInstance().getProperty("output_file_name") + " -w " + numOfCells;
			try {
			//	Process process = new ProcessBuilder(cmdLine).start();
				 Runtime runtime = Runtime.getRuntime();
			        Process process = runtime.exec(cmdLine, null, new File(PropertiesFile.getSystemRoot()+"cell"));
			        process.getErrorStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return returnVal;
	}
	
	public static void callRISE() {
		RiseClient client = new RiseClient();
		String fileName = PropertiesFile.getInstance().getProperty("input_file_name");
		String usrName = "test";
		String passWd = "test";
		String framework = "test/lopez/IntegratedTool";
		//first delete previous framework
		client.DeleteFramework(usrName, passWd, "test/lopez/TheBigD");
		client.DeleteFramework(usrName, passWd, framework);
		
		client.PutXMLFile(usrName, passWd, framework, fileName);
		client.PostZipFile(usrName, passWd, framework+"?zdir=n", fileName);
		client.PutFramework(usrName, passWd, framework+"/simulation");
	}
}
