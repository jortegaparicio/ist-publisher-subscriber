package urjc.ist.jms.pubsubexample;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * <h1>PubSubSender class</h1>
 * 
 * <p> The PubSubSender implements Runnable overriding run() method.
 * This class publish each publisher message in the Topic of the Payara server 
 * for the publisher/subscriber pattern.
 * <p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 11/05/2021
 */
public class PubSubSender implements Runnable{
	private static final String STOP        = "CLOSE";  // Message sent to stop subscriber Threads
	private static final String TOPIC_NAME  = "Topic1"; // Name of the topic
	private static final int    NMESSAGE    = 3;        // Number of messages 
	private static final int    MILISLEEP   = 1000;     // ms sleeping time

	private TopicConnectionFactory factory; // The factory where we creates the connection
	private InitialContext jndi;            // The initial context of the communication
	

	/**
	 * Constructor method of this class, the initial context and the factory are needed in this method
	 * @param factory
	 * @param jndi
	 */
	public PubSubSender(InitialContext jndi, TopicConnectionFactory factory) {
		super();
		this.jndi = jndi;
		this.factory = factory;
	}

	/**
	 * This method closes all connections and sends the close message to the subscribers
	 * 
	 * @param connection This is the connection that this method will close
	 * @param topic This is the Topic where this method send the close message
	 */
	private static void closeConnection(TopicConnection connection, TopicPublisher publisher, TextMessage msg) {
		

		// Connection closure
		System.err.println("Sending message to close connection...");
		try {
			
			// End message STOP must be handled to end the consumer that have received it
			msg.setText(STOP);
			publisher.publish(msg);

			// Closes the connection
			System.err.println("Closing connection...");
			connection.close();  
			System.err.println("END");
			
		}catch(JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method override run() method from Runnable. This methods sends 3 messages to the Topic.
	 */
	@Override
	public void run(){
		
		TopicSession session = null;
		TopicPublisher publisher = null;
		TextMessage msg = null;
		TopicConnection connection = null; 
		Topic topic = null; 

		try {

			// Topic is referenced here by lookup function
			connection = factory.createTopicConnection(); 
			try {
				topic = (Topic)jndi.lookup(TOPIC_NAME);
			} catch (NamingException e) {
				e.printStackTrace();
			} 
			
			// Create session and activate auto-commit
			session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			
			// Creating publisher and creating the message 
			publisher = session.createPublisher(topic);
			msg = session.createTextMessage();
			
			// Send NMESSAGE to the subscribers
			for(int i = 0; i < NMESSAGE; i++){
				
				// Create the message
				msg.setText("Message " + i + " to " + TOPIC_NAME);
				
				// Publish the message
				publisher.publish(msg);
				
				// Print the message associated to this Thread
				System.out.println("Thread " + Thread.currentThread().getId() + 
						". Sending message " + i + " to " + TOPIC_NAME);
				Thread.sleep(MILISLEEP);
			}

		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally{
			closeConnection(connection, publisher, msg);
		}
	}

		
}
