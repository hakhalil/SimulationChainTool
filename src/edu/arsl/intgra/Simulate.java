package edu.arsl.intgra;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Simulate
 */
@WebServlet("/simulate")
public class Simulate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Simulate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = PropertiesFile.getInstance().getProperty("RISE_uri") + PropertiesFile.getInstance().getProperty("RISE_frameworkname")+"/results";
		String filePath = PropertiesFile.getInstance().getGenerationFolderWithFullPath();
		FileManagement.downloadFile(uri, filePath+"/result.zip");
		FileManagement.extractZip(filePath+"/result.zip", filePath);
		request.getRequestDispatcher( "simulation.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rootFolderForGeneratedFiles = PropertiesFile.getInstance().getGenerationFolderWithFullPath();
		String folderInZip = rootFolderForGeneratedFiles + "/" + PropertiesFile.getInstance().getProperty("input_qualifier");
		String conversionToolOutputFolder = rootFolderForGeneratedFiles + "/" + PropertiesFile.getInstance().getProperty("output_qualifier");
		
		String strOccupants = request.getParameter("occupants");
		String nextPageURL = "simulation.jsp";
		try {
			prepareFilesForRise(conversionToolOutputFolder, Integer.parseInt(strOccupants));
			zipModel(folderInZip, conversionToolOutputFolder);
			
			if(RunProcess.callRISE()) {
				request.setAttribute("NotGen", "0");
			} else throw new Exception("RISE did not work");
			
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("NotGen", "1");
			nextPageURL = "ct.jsp";
		}
		request.getRequestDispatcher(nextPageURL).forward(request, response);
	}

	private boolean zipModel(String zipFileBaseName, String outputFolder) throws Exception {
		
		File generatedDir = new File(outputFolder);
		
		//the output of the generation tool must exist before we attempt to zip it
		if(generatedDir.exists() && generatedDir.isDirectory() && 
				generatedDir.list() != null && generatedDir.list().length > 0) {
			String zipFileName = zipFileBaseName +".zip";
			FileManagement.compress(outputFolder, zipFileName);
			return true;
		}
		else return false;
	}
	
	private void prepareFilesForRise(String folderName, int numberOfOccupants) throws Exception{
		String dimClause = FileManagement.readDimFromMake(folderName);
		
		//add dimensions, as per make file, to the XML template file and add the update file to the output folder
		FileManagement.editXML(dimClause);
		
		//generated Pal is not complete we overwrite with a templated pal
		FileManagement.overwritePal();
		
		//add occupants
		FileManagement.editStartingValues(dimClause, folderName, numberOfOccupants);
	}
	

}

