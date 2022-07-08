package com.ibm.example.serverless;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URL;

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
	private static String MQ_PORT;
	private static String MQ_HOST;
	private static String MQ_QUEUE; 
	private static String MQ_CHANNEL; 
	private static String DB_URL; 

	
	public static void main(String[] args) {
		
		System.setProperty("javax.net.debug", "all");
		
		MQ_QMGR = System.getenv("MQ_QMGR");
		MQ_PORT = System.getenv("MQ_PORT");
		MQ_HOST = System.getenv("MQ_HOST");
		MQ_QUEUE = System.getenv("MQ_QUEUE");
		MQ_CHANNEL = System.getenv("MQ_CHANNEL");
		DB_URL= System.getenv("DB_URL");

		
		if (MQ_QMGR == null || MQ_PORT == null || MQ_HOST == null || MQ_QUEUE == null || MQ_CHANNEL == null) {
			throw new RuntimeException("MQ environment variables not set!");
		}
		
		if (DB_URL == null) {
			throw new RuntimeException("DB URL environment variables not set!");
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
		
		t.close();
		
		Connection conn = null;
		MQConnectionFactory cf = new MQQueueConnectionFactory();
		String vote = "";

		
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
			return;
		}
		
		// send to cloudant ...
		
		if (vote == null || vote.length() == 0) {
			System.err.println("Vote has no data");
			return;
		}
		
		String data = "country=" + vote;
				
		System.out.println("before connection");
		 try {
			    //Create connection
			    URL url = new URL(DB_URL);
			    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			    connection.setRequestMethod("POST");
			    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			    connection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
			    connection.setUseCaches(false);
			    connection.setDoOutput(true);
			    connection.connect();

				System.out.println("before data");

			    OutputStreamWriter wr = new OutputStreamWriter(connection.getOuputStream());
			    wr.writeBytes(data);
			    wr.flush();
			    
				System.out.println("after data");
				
			    connection.disconnect();
		 } catch (Exception e) {
				e.printStackTrace(System.err);
		 }
		 
			System.out.println("after connection");

	}
}
