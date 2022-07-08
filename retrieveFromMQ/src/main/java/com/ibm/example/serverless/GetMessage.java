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
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.net.ssl.HttpsURLConnection;

public class GetMessage implements HttpHandler {
	
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
			server.createContext("/", new GetMessage());
			server.start();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		System.out.println("Get Message is running!");
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		
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
			
			MessageConsumer consumer = session.createConsumer(q);	
			Message receivedMsg = null;
			
			conn.start();
			
			String vote; 
			if ((receivedMsg = consumer.receiveNoWait()) != null) {
				if (receivedMsg != null) {
					vote = ((TextMessage) receivedMsg).getText();
				}
			}
			
			session.commit();
			session.close();
			conn.close();
			
		} catch (JMSException e) {
			e.printStackTrace(System.err);
	    	sendResponse(t, HttpsURLConnection.HTTP_NOT_ACCEPTABLE, "MQ Exception : " +  e.getMessage());
		}
		
		// send to cloudant ...
		
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
