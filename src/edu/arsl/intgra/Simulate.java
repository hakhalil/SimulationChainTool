package edu.arsl.intgra;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

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
		String zipFileName = genDir + PropertiesFile.getInstance().getProperty("default_name")+".zip";
		File generatedDir = new File(genDir);
		
		String nextPageURL = "simulation.jsp";
		if(generatedDir.exists() && generatedDir.isDirectory() && 
				generatedDir.list() != null && generatedDir.list().length > 0) {
			ZipFile zipfile = new ZipFile(zipFileName);
			request.setAttribute("NotGen", "0");
		} else {
			request.setAttribute("NotGen", "1");
			nextPageURL = "ct.jsp";
		}
		request.getRequestDispatcher(nextPageURL).forward(request, response);
	}

}
