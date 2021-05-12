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
 * <p> The TestPubSubSender class launches NPUBL concurrent publisher for 
 * the publisher/subscriber pattern.
 * <p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 11/05/2021
 */
public class TestPubSubSender {
	
	private static final int    NPUBL       = 2;           // Number of publisher
	private static final String factoryName = "Factoria2"; // Name of our factory
	private static final String topicName   = "Topic1";    // Name of our topic
	private static final String STOP        = "CLOSE";     // Message sent to stop subscriber Threads

	private static ExecutorService PublisherPool = Executors.newFixedThreadPool(NPUBL); // Pool of threads

	
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
					System.out.println("All threads closed");
				}
				
			}else {
				System.out.println("All threads closed");
			}
			
		} catch (InterruptedException ie) {
			PublisherPool.shutdownNow();
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
			
			// Launch NPUBL
			for(int i = 0; i < NPUBL; i++) {
				
				// Asynchronous publisher is created here 
				PubSubSender publisher = new PubSubSender(connection, topic); 
				
				// Launch receiver
				PublisherPool.submit(publisher); 
			}
			
			// Closing thread pool
			shutdownAndAwaitTermination(60, 60);
			
			// Connection closure
			System.err.println("Sending message to close connection...");

			TopicSession session = 
					connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			TopicPublisher publisher = session.createPublisher(topic);
			TextMessage msg = session.createTextMessage();

			// End message STOP must be handled to end the consumer that have received it
			msg.setText(STOP);
			publisher.publish(msg);

			// Closes the connection
			System.err.println("Closing sender...");
			connection.close();  
			System.err.println("END");

		} catch (NamingException ex) {
			ex.printStackTrace();
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}
}
