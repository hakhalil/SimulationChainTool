package edu.arsl.intgra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;

public class RiseClient {

	public int PostZipFile( String frameworkName, final String filename) {
		OutputRepresentation outfile = new OutputRepresentation(MediaType.APPLICATION_ZIP) {
			public void write(OutputStream stream) throws IOException {
				byte[] outfile = getBytesFromFile(new File(PropertiesFile.getInstance().getFullyQualifiedGenertorDir() +"/"+filename+".zip"));
				stream.write(outfile);
			}
		};
		String uri = PropertiesFile.getInstance().getProperty("RISE_uri") + frameworkName;
		Request request = getAuthenticatedRequest(Method.POST, uri);
		request.setEntity((Representation) outfile);
		Response resp = (new Client(Protocol.HTTP)).handle(request);
		System.out.println(resp.getStatus());
		System.out.println(resp.getEntity());
		int retVal = resp.getStatus().getCode();
		return retVal;
	}

	public int PutXMLFile( String frameworkName, String filename) {
		File file = new File(PropertiesFile.getInstance().getFullyQualifiedGenertorDir() + "/"+filename+".xml");
		int retVal = 200;
		if (file.exists()) {
			FileRepresentation outfile = new FileRepresentation(PropertiesFile.getInstance().getFullyQualifiedGenertorDir() + "/"+filename+".xml", MediaType.TEXT_XML);
			String uri = PropertiesFile.getInstance().getProperty("RISE_uri") + frameworkName;
			Request request = getAuthenticatedRequest(Method.PUT, uri);
			request.setEntity((Representation) outfile);
			Response resp = (new Client(Protocol.HTTP)).handle(request);
			System.out.println(resp.getStatus());
			System.out.println(resp.getEntity());
			retVal = resp.getStatus().getCode();
		} else {
			System.out.println("Couldn't find file " + file.getName());
		}
		return retVal;
	}
	
	 public  int PutFramework( String frameworkName) {
		    String uri = PropertiesFile.getInstance().getProperty("RISE_uri") + frameworkName;
		    Request request = getAuthenticatedRequest(Method.PUT, uri);
		    Response resp = (new Client(Protocol.HTTP)).handle(request);
		    System.out.println(resp.getStatus());
		    System.out.println(resp.getEntity());
		    int retVal = resp.getStatus().getCode();
		    return retVal;
		  }

	 public  int DeleteFramework( String frameworkName) {
		    String uri = PropertiesFile.getInstance().getProperty("RISE_uri") + frameworkName;
		    Request request = getAuthenticatedRequest(Method.DELETE, uri);
		    Response resp = (new Client(Protocol.HTTP)).handle(request);
		    int retVal = resp.getStatus().getCode();
		    System.out.println(resp.getStatus() + " : " + resp.getLocationRef());
		    return retVal;
		  }
	public byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		if (length > 2147483647L)
			;
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
			offset += numRead;
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file " + file.getName());
		}
		is.close();
		return bytes;
	}

	public Request getAuthenticatedRequest(Method method, String uri) {
		Request request = new Request(method, uri);
		request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, PropertiesFile.getInstance().getProperty("RISE_usrname"), 
					PropertiesFile.getInstance().getProperty("RISE_passwd")));
		return request;
	}
}
