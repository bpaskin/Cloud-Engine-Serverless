package com.ibm.example.serverless;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class AcceptVote implements HttpHandler {

	public static void main(String[] args) {
		
		try {
			// Setup HTTP Server to accept requests on port 8080
			// All requests coming in on / will be routed to AcceptVote()
			HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
			server.createContext("/", new AcceptVote());
			server.start();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		System.out.println("Accept Vote is running!");
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		
		// get the form data sent from the HTTP Page
        StringBuilder sb = new StringBuilder();
        InputStream ios = t.getRequestBody();
        
        int i;
        while ((i = ios.read()) != -1) {
            sb.append((char) i);
        }
        
        ios.close();
		
        // make sure there is a selection
        String[] parts;
        
        if (sb.length() == 0) {
        	sendResponse(t, 400, "No selection sent");
        	return;
        } else {
        	parts = sb.toString().split("&");
        }
        
    	if (parts.length != 1) {
        	sendResponse(t, 400, "invalid data sent");
        	return;
    	} else  {
    		parts = parts[0].toString().split("=");
    	}
        	
    	if (parts.length != 2 ) {
        	sendResponse(t, 400, "invalid data sent");
        	return;
    	}
    	
		if (!parts[0].equalsIgnoreCase("selection")) {
        	sendResponse(t, 400, "invalid data sent");
        	return;
    	}   
		
		boolean found = false;
		for (Countries country : Countries.values()) {
			if (parts[1].equalsIgnoreCase(country.toString())) {
				found = true;
			}
		}
		
		if (!found) {
        	sendResponse(t, 400, "invalid country");
        	return;
		}
		
    	sendResponse(t, 200, "vote accetped");
    	return;
		
	}
	
	private void sendResponse(HttpExchange t, int responseCode, String response) throws IOException{
		t.sendResponseHeaders(responseCode, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
