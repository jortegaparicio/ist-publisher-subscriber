package urjc.ist.jms.pubsubexample;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * <h1>TestPubSubSender class</h1>
 * 
 * <p> The TestPubSubSender class launches N concurrent publisher for 
 * the publisher/subscriber pattern.
 * <p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 11/05/2021
 */
public class TestPubSubSender {
	
	private static final int    NPUBL        = 20;          // Number of publisher
	private static final String FACTORY_NAME = "Factoria2"; // Name of our factory

	private static ExecutorService PublisherPool;           // Pool of threads

	
	/**
	 * Method to close the ExecutorService in two stages: first avoiding running new
	 * tasks in the pool and after, requesting the tasks running to finish.
	 * 
	 * @param firstTimeout Timeout to the first waiting stage.
	 * @param secondTimeout Timeout to the second waiting stage.
	 */
	private static void shutdownAndAwaitTermination(int firstTimeout, int secondTimeout) {
		PublisherPool.shutdown(); 
		try {
			// Waiting for all threads to finish their tasks
			if (!PublisherPool.awaitTermination(firstTimeout, TimeUnit.SECONDS)) {
				
				// If tasks are not finished then we have to force closure
				System.err.println("Uncompleted tasks. forcing closure...");
				PublisherPool.shutdownNow(); 
				
				// If tasks are not finished again, we have to finish with some threads
				if (!PublisherPool.awaitTermination(secondTimeout, TimeUnit.SECONDS)) {
					System.err.println("Unended thread pool");
				}else {
					System.err.println("All threads closed");
				}
				
			}else {
				System.err.println("All threads closed");
			}
			
		} catch (InterruptedException ie) {
			PublisherPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	
	public static void main(String[] args) {

		PublisherPool = Executors.newFixedThreadPool(NPUBL);
		InitialContext jndi = null;
		TopicConnectionFactory factory = null;
		PubSubSender publisher = null; 

		try {
			
			// Context initialization
			jndi = new InitialContext();
			
			// Factory is referenced here by lookup function
			factory = (TopicConnectionFactory)jndi.lookup(FACTORY_NAME);
						
			// Launch NPUBL
			for(int i = 0; i < NPUBL; i++) {
				
				// Asynchronous publisher is created here 
				publisher = new PubSubSender(jndi, factory); 
				
				// Launch receiver
				PublisherPool.submit(publisher); 
			}

		} catch (NamingException ex) {
			ex.printStackTrace();
		}finally {
			// Closing thread pool
			shutdownAndAwaitTermination(60, 60);	
		}
	}
}
