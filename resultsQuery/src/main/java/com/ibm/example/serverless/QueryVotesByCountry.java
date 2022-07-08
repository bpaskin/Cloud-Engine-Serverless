package com.ibm.example.serverless;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.ibm.cloud.cloudant.v1.Cloudant;
import com.ibm.cloud.cloudant.v1.model.FindResult;
import com.ibm.cloud.cloudant.v1.model.PostFindOptions;
import com.ibm.cloud.sdk.core.http.HttpConfigOptions;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class QueryVotesByCountry implements HttpHandler {
	
	private static String IAMKEY;
	private static String SERVICE_NAME;
	private static String SERVICE_URL;
	private static String DBNAME;
	

	public static void main(String[] args)  {
		
		// cloudant info
		IAMKEY = System.getenv("IAMKEY");
		SERVICE_NAME = System.getenv("SERVICE_NAME");
		SERVICE_URL = System.getenv("SERVICE_URL");

		if (IAMKEY == null || SERVICE_NAME == null || SERVICE_URL == null) {
			throw new RuntimeException("Cloudant environment variables not set!");
		}
		
		try {
			// Setup HTTP Server to accept requests on port 8080
			// All requests coming in on / will be routed to QueryVotesByCountry()
			HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
			server.createContext("/", new QueryVotesByCountry());
			server.start();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		System.out.println("Query Votes by Country is running!");
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
        	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "No selection sent");
        	return;
        } else {
        	parts = sb.toString().split("&");
        }
        
    	if (parts.length != 1) {
        	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "invalid data sent");
        	return;
    	} else  {
    		parts = parts[0].toString().split("=");
    	}
        	
    	if (parts.length != 2 ) {
        	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "invalid data sent");
        	return;
    	}
    	
		if (!parts[0].equalsIgnoreCase("country")) {
        	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "invalid data sent");
        	return;
    	}   
		
		String country = parts[1];
		
		IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(IAMKEY).build();

		Cloudant service = new Cloudant(SERVICE_NAME, authenticator);
		service.setServiceUrl(SERVICE_URL);

		HttpConfigOptions options =  new HttpConfigOptions.Builder().loggingLevel(HttpConfigOptions.LoggingLevel.BASIC).build();
		service.configureClient(options);
			
		Map<String, Object> selector = Collections.singletonMap("vote",country);
		PostFindOptions pfo = new PostFindOptions.Builder().db(DBNAME).selector(selector).build();
		FindResult response = service.postFind(pfo).execute().getResult();
	
		sendResponse(t, HttpsURLConnection.HTTP_OK, String.valueOf(response.getDocs().size()));
	}
	
	private void sendResponse(HttpExchange t, int responseCode, String response) throws IOException{
		t.sendResponseHeaders(responseCode, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
