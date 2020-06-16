package edu.arsl.intgra;

import java.io.File;

import org.apache.commons.io.IOUtils;

public class RunProcess {
	
	/**
	 * Run the Python script that generates a model from an image or a revit file
	 * @param numOfCells - how big do we want the model (based on real size of the room
	 * represented by the inptu file)
	 * @param inputFile - the image or the revit file
	 * @return
	 */
	public static int runCDGenerator(int numOfCells, String inputFile) {
		int returnVal = 0;
		if(inputFile != null) {
			
			//Running a Python script. It is assumed that Python is intalled on the server and is in the path
			String cmdLine = "Python ";
			
			//providing the script name to run
			cmdLine+= FileManagement.getFullyQualifiedFileName("cell")+"/co2_cd_generator.py ";
			
			String cellsPath = FileManagement.getOutputFolderName();
			//parameters to the script
			cmdLine += "-i " + cellsPath+"/"+inputFile + " -o " + FileManagement.getGeneratorOutputFolder() + " -w " + numOfCells;
			cmdLine += " -r "+ FileManagement.getFullyQualifiedFileName("cell")+"/co2_rules.txt -bv 500";
			try {
				Runtime runtime = Runtime.getRuntime();
		        
				//running the command line in the home directory of the script. Otherwise the script did not run properly
				Process process = runtime.exec(cmdLine, null, new File(FileManagement.getFullyQualifiedFileName("cell")));
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
		String fileName = PropertiesFile.getInstance().getProperty("input_qualifier");
		
		String framework = PropertiesFile.getInstance().getProperty("RISE_frameworkname");
		//first delete previous framework
		client.deleteFramework(framework);
		
		int retVal = client.putXMLFile( framework, fileName);
		if(retVal >= 200 && retVal <=202) {
			retVal = client.postZipFile(framework+"?zdir=in", fileName);
			if(retVal >= 200 && retVal <=202)
				retVal = client.runSimulation(framework+"/simulation");
		}
		return (retVal >= 200 && retVal <=202);
	}
}
