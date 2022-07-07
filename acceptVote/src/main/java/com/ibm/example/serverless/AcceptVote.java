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
		
		// setup response
		int responseCode = 200;
		String response = "ok";
		
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
        	responseCode = 400;
        }
        
        if (sb.length() > 0) {
        	parts = sb.toString().split("&");
        	
        	if (parts.length == 1) {
        		
        	} else {
        		
        	}
        }
       
        

		
		t.sendResponseHeaders(responseCode, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
