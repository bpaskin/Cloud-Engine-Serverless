package com.ibm.example.serverless;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class AcceptVote implements HttpHandler {

	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/", new AcceptVote());
		server.start();

		System.out.println("Welcome to my world!");
	}

	public void handle(HttpExchange t) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream ios = t.getRequestBody();
        int i;
        while ((i = ios.read()) != -1) {
            sb.append((char) i);
        }
        System.out.println("hm: " + sb.toString());
		
		String response;
		
		if ( t.getAttribute("pizza") == null) {
			 response = "no pizza found";
		} else {
			 response = "This is the response";
	
		}
		
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
