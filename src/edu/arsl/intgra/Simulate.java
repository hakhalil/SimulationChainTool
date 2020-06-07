package edu.arsl.intgra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

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
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String genDir = PropertiesFile.getInstance().getFullyQualifiedGenertorDir();
		String inputName = genDir + "/" + PropertiesFile.getInstance().getProperty("input_file_name");
		String outputName = genDir + "/" + PropertiesFile.getInstance().getProperty("output_file_name");
		
		String nextPageURL = "simulation.jsp";
		try {
			zipModel(inputName, outputName);
			prepareRiseXML(outputName);
			RunProcess.callRISE();
			request.setAttribute("NotGen", "0");
			
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("NotGen", "1");
			nextPageURL = "ct.jsp";
		}
		request.getRequestDispatcher(nextPageURL).forward(request, response);
	}

	private boolean zipModel(String inputName, String outputName) throws Exception {
		File generatedDir = new File(outputName);
		if(generatedDir.exists() && generatedDir.isDirectory() && 
				generatedDir.list() != null && generatedDir.list().length > 0) {
			String zipFileName = inputName +".zip";
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFileName));
			FileManagement.addFolderToZip(generatedDir, zip, outputName+"/");
			zip.close();
			return true;
		}
		else return false;
	}
	
	private void prepareRiseXML(String outputName) throws Exception{
		String dimClause = FileManagement.readDimFromMake(outputName);
		FileManagement.editXML(dimClause);
	}
}

