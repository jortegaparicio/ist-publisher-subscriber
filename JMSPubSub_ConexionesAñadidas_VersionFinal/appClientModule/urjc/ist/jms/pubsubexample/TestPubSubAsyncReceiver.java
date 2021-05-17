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
	
	private static final int    NSUBS        = 10;          // Number of subscribers
	private static final String FACTORY_NAME = "Factoria2"; // Name of our factory
	
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
					System.err.println("All threads closed");
				}
				
			}else {
				System.err.println("All threads closed");
			}
			
		} catch (InterruptedException ie) {
			ReceiverPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	public static void main(String[] args) {
		
		InitialContext jndi = null;
		TopicConnectionFactory factory = null;
		PubSubAsyncReceiver receiver = null; 

		try {
			// Context initialization
			jndi = new InitialContext();
			
			// Factory is referenced here by lookup function
			factory = (TopicConnectionFactory)jndi.lookup(FACTORY_NAME);
			
			// Launch NSUBS
			for(int i = 0; i < NSUBS; i++) {

				// Asynchronous Pub/Sub is created here 
				receiver = new PubSubAsyncReceiver(jndi, factory); 
				
				// Launch receiver
				ReceiverPool.submit(receiver); 
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} catch (NamingException ex) {
			ex.printStackTrace();
		}finally {
			shutdownAndAwaitTermination(60, 60);
		}
	}
}
