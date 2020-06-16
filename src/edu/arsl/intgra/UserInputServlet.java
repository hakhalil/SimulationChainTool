package edu.arsl.intgra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet implementation class UserInputServlet
 */
@WebServlet("/userInputServlet")
@MultipartConfig
public class UserInputServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public UserInputServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String nextPageURL = "ct.jsp";
		String contentType = request.getContentType();

		//since we are sending a file stream, the form has to be a multipart
		if ((contentType.indexOf("multipart/form-data") >= 0)) {
			//first get the file
			Part uploadedFile = request.getPart("file");
			
			//check that the output folder exits and clear it for a fresh start
			FileManagement.initializeOutputFolder();
			
			String uploadFileName = Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString(); 
			uploadFileName = getPropertlyFormatedFileName(uploadedFile);
			
			String fileNameOnServer = getFileNameOnServer(uploadedFile, uploadFileName);
			
			//attempt to write the file to the file system. If all is OK, proceed with the rest of the processing
			//otherwise go back with error message
			if(!saveFilePartToServer(uploadedFile, uploadFileName, fileNameOnServer)) {
				request.setAttribute("WrongFile", "1");
				nextPageURL = "start.jsp";
			} else {
				//reading the regular parameters which are the room width and depth
				String roomWidth = request.getParameter("rmWidth");
				//String roomDepth = request.getParameter("rmDepth");
				
				int numOfCells = calculateCells(roomWidth);
				request.setAttribute("CellSize", numOfCells);
				
				RunProcess.runCDGenerator(numOfCells, fileNameOnServer);
				if(FileManagement.checkFolder(FileManagement.getGeneratorOutputFolder())) {
					request.setAttribute("NotGen", "0");
				}
			}
		}
		request.getRequestDispatcher(nextPageURL).forward(request, response);
	}

	/**
	 * Fixes the file name as it is stripped of the separators
	 * @param part
	 * @return
	 */
	private String getPropertlyFormatedFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); 
			}
		}
		return null;
	}

	/**
	 * Writes the file on the request to the server file system
	 * @param filePart
	 */
	private boolean saveFilePartToServer(Part filePart, String uploadedFileName, String filenameOnServer) {
		boolean returnValue = false;
		
		
		//we only want to work with acceptable extensions
		boolean validType = FileManagement.checkFileType(uploadedFileName);

		if (validType) {
			try {

				String fileNameOnServer = getFileNameOnServer(filePart, uploadedFileName);

				InputStream fileContent = filePart.getInputStream();
				//rename the file name on server to use the input qualifier but keep the extension
				returnValue = FileManagement.writeFile(fileNameOnServer, fileContent);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return returnValue;
	}

	/** 
	 * get room width and based on it calculate the number of cells in the model
	 * @param roomWidth
	 * @return
	 */
	private int calculateCells(String roomWidth) {

		String default_cell_size = PropertiesFile.getInstance().getProperty("default_cell_size");

		return Integer.parseInt(default_cell_size) * Integer.parseInt(roomWidth);
	}
	
	/**
	 * Generate the name of the image (or REVIT) on the server using the uploaded extension as
	 * well as the default names for the application
	 * @param filePart
	 * @param uploadedFileName
	 * @return
	 */
	private String getFileNameOnServer(Part filePart, String uploadedFileName) {
		String fileNameOnServer = PropertiesFile.getInstance().getProperty("input_qualifier");
		fileNameOnServer += uploadedFileName.substring(uploadedFileName.length() - 4, uploadedFileName.length());
		return fileNameOnServer;
	}
	
}
