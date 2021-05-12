package urjc.ist.jms.pubsubexample;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * <h1>TestPubSubAsyncReceiver class</h1>
 * 
 * <p> The TestPubSubAsyncReceiver class launches N concurrent subscribers for 
 * the publisher/subscriber pattern.
 * <p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 11/05/2021
 */
public class TestPubSubAsyncReceiver {
	
	private static final int    NSUBS       = 3;           // Number of subscribers
	private static final String factoryName = "Factoria2"; // Name of our factory
	private static final String topicName   = "Topic1";    // Name of our topic
	
	private static ExecutorService ReceiverPool = Executors.newFixedThreadPool(NSUBS); // Pool of threads

	
	/**
	 * Method to close the ExecutorService in two stages: first avoiding running new
	 * tasks in the pool and after, requesting the tasks running to finish.
	 * 
	 * @param firstTimeout Timeout to the first waiting stage.
	 * @param secondTimeout Timeout to the second waiting stage.
	 */
	private static void shutdownAndAwaitTermination(int firstTimeout, int secondTimeout) {
		ReceiverPool.shutdown(); 
		try {
			// Waiting for all threads to finish their tasks
			if (!ReceiverPool.awaitTermination(firstTimeout, TimeUnit.SECONDS)) {
				
				// If tasks are not finished then we have to force closure
				System.err.println("Uncompleted tasks. forcing closure...");
				ReceiverPool.shutdownNow(); 
				
				// If tasks are not finished again, we have to finish with some threads
				if (!ReceiverPool.awaitTermination(secondTimeout, TimeUnit.SECONDS)) {
					System.err.println("Unended thread pool");
				}else {
					System.out.println("All threads closed");
				}
				
			}else {
				System.out.println("All threads closed");
			}
			
		} catch (InterruptedException ie) {
			ReceiverPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	
	public static void main(String[] args) {

		try {
			// Context initialization
			InitialContext jndi = new InitialContext();
			
			// Factory is referenced here by lookup function
			TopicConnectionFactory factory = 
					(TopicConnectionFactory)jndi.lookup(factoryName);
			
			// Topic is referenced here by lookup function
			TopicConnection connection = factory.createTopicConnection(); 
			Topic topic = (Topic)jndi.lookup(topicName); 
			
			// Launch NSUBS
			for(int i = 0; i < NSUBS; i++) {
				
				// Asynchronous Pub/Sub is created here 
				PubSubAsyncReceiver receiver = new PubSubAsyncReceiver(connection, topic); 
				
				// Launch receiver
				ReceiverPool.submit(receiver); 
			}
			
			// Closing thread pool
			shutdownAndAwaitTermination(60, 60);

			// Close the connection
			System.err.println("TRACE: Closing connection");
			connection.close(); 
			System.err.println("END");

		} catch (NamingException ex) {
			ex.printStackTrace();
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}
}
