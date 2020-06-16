package edu.arsl.intgra;

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
		String filePath = FileManagement.getOutputFolderName();
		
		//download the results file from RISE
		FileManagement.downloadFile(uri, filePath+"/result.zip");
		
		//RISE returns a Zipped file which we will unzip to get directly the log file
		FileManagement.extractZip(filePath+"/result.zip", filePath);
		request.getRequestDispatcher( "simulation.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rootFolderForGeneratedFiles = FileManagement.getOutputFolderName();
		String zipFileName = rootFolderForGeneratedFiles + "/" + PropertiesFile.getInstance().getProperty("input_qualifier")+".zip";
		
		String strOccupants = request.getParameter("occupants");
		String closeWindow = request.getParameter("closed");
		String nextPageURL = "simulation.jsp";
		try {
			prepareFilesForRise(Integer.parseInt(strOccupants), closeWindow);
			String folderToZip = FileManagement.getGeneratorOutputFolder();
			
			//RISE expects the makefile, pal and values files to be zipped
			zipModel(zipFileName, folderToZip);
			
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

	private boolean zipModel(String zipFileName, String folderToCompress) throws Exception {
		
		//the output of the generation tool must exist before we attempt to zip it
		if(FileManagement.checkFolder(folderToCompress)) {
			FileManagement.compressFolder(folderToCompress, zipFileName);
			return true;
		}
		else return false;
	}
	
	/**
	 * Some editing is done on the file before sending to RISE to accommodate the changes 
	 * that the user requested through their input in the application. 
	 * @param numberOfOccupants
	 * @param closeWindow
	 * @throws Exception
	 */
	private void prepareFilesForRise(int numberOfOccupants, String closeWindow) throws Exception{
		
		boolean isWindowClosed = (closeWindow!= null) && (closeWindow.equals("on"));
		
		//edit rules based on whether the windows are opened or not
		String dimClause = FileManagement.manipluateMakeFile(isWindowClosed);
		
		//add dimensions, as per make file, to the XML template file and add the update file to the output folder
		FileManagement.editXML(dimClause);
		
		//generated Pal is not complete so we overwrite it with a templated pal
		FileManagement.overwritePal();
		
		//add occupants to the initial values
		FileManagement.addOccupantsToValues(dimClause, numberOfOccupants);
	}
	

}

