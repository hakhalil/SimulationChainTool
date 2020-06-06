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
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;

public class RiseClient {
	
	  
	  public  void PostZipFile(String loginUsername, String loginPassword, String frameworkName, final String filename) {
	    OutputRepresentation outfile = new OutputRepresentation(MediaType.APPLICATION_ZIP) {
	        public void write(OutputStream stream) throws IOException {
	          byte[] outfile = getBytesFromFile(new File(getCurrentDir() + filename));
	          stream.write(outfile);
	        }
	      };
	    String uri = "http://134.117.62.118:8080/cdpp/sim/workspaces/" + frameworkName;
	    Request request = getAuthenticatedRequest(Method.POST, uri, loginUsername, loginPassword);
	    request.setEntity((Representation)outfile);
	    Response resp = (new Client(Protocol.HTTP)).handle(request);
	    System.out.println(resp.getStatus());
	    System.out.println(resp.getEntity());
	  }
	  

	  public  byte[] getBytesFromFile(File file) throws IOException {
		    InputStream is = new FileInputStream(file);
		    long length = file.length();
		    if (length > 2147483647L);
		    byte[] bytes = new byte[(int)length];
		    int offset = 0;
		    int numRead = 0;
		    while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
		      offset += numRead; 
		    if (offset < bytes.length)
		      throw new IOException("Could not completely read file " + file.getName()); 
		    is.close();
		    return bytes;
		  }
	  
	  private String getCurrentDir() {return null;
	  
	  }
	  
	  public  Request getAuthenticatedRequest(Method method, String uri, String login, String password) {
		    Request request = new Request(method, uri);
		    request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, login, password));
		    return request;
		  }
}
