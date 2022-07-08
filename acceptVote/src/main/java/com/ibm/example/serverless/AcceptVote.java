package com.ibm.example.serverless;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.net.ssl.HttpsURLConnection;

public class AcceptVote implements HttpHandler {
	
	private static String MQ_QMGR;
	private static String  MQ_PORT;
	private static String MQ_HOST;
	private static String MQ_QUEUE; 
	private static String MQ_CHANNEL; 

	
	public static void main(String[] args) {
		
		MQ_QMGR = System.getenv("MQ_QMGR");
		MQ_PORT = System.getenv("MQ_PORT");
		MQ_HOST = System.getenv("MQ_HOST");
		MQ_QUEUE = System.getenv("MQ_QUEUE");
		MQ_CHANNEL = System.getenv("MQ_CHANNEL");

		
		if (MQ_QMGR == null || MQ_PORT == null || MQ_HOST == null || MQ_QUEUE == null || MQ_CHANNEL == null) {
			throw new RuntimeException("MQ environment variables not set!");
		}
		
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
    	
		if (!parts[0].equalsIgnoreCase("selection")) {
        	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "invalid data sent");
        	return;
    	}   
		
		boolean found = false;
		String selection = parts[1];
		for (Countries country : Countries.values()) {
			if (selection.equalsIgnoreCase(country.toString())) {
				found = true;
			}
		}
		
		if (!found) {
        	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "invalid country");
        	return;
		}
		
		Connection conn = null;
		MQConnectionFactory cf = new MQQueueConnectionFactory();
		
		try {
			cf.setTransportType(WMQConstants.WMQ_CM_CLIENT);
			cf.setHostName(MQ_HOST);
			cf.setPort(Integer.parseInt(MQ_PORT));
			cf.setQueueManager(MQ_QMGR);
			cf.setChannel(MQ_CHANNEL);
			
			conn = cf.createConnection();
			
			Session session = conn.createSession(true, 0);
			Queue q = session.createQueue(MQ_QUEUE);
			
			MessageProducer producer = session.createProducer(q);
			conn.start();
			
			Message msg = session.createTextMessage(selection);
			producer.send(msg);
			session.commit();
			session.close();
			conn.close();
		} catch (JMSException e) {
			e.printStackTrace(System.err);
	    	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "MQ Exception : " +  e.getMessage());
		}
			
    	sendResponse(t, HttpsURLConnection.HTTP_OK, "vote accepted");
    	return;
	}
	
	private void sendResponse(HttpExchange t, int responseCode, String response) throws IOException{
		t.sendResponseHeaders(responseCode, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
