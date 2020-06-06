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
			cmdLine += "-i " + cellsPath+"/"+inputFile + " -o " + cellsPath + " -w " + numOfCells;
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
}
