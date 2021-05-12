package urjc.ist.jms.pubsubexample;

import javax.jms.*;

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

	private static final String topicName   = "Topic1"; // Name of the topic
	private static final int    NMESSAGE    = 3;        // Number of messages 
	private static final int    MILISLEEP   = 1000;     // ms sleeping time
	
	private TopicConnection connection; // Attribute that establish a connection with Payara Server
	private Topic topic;                // Attribute where we post messages
	
	/**
	 * Constructor with arguments. It requires the connection with the Payara Server and 
	 * the Topic where we share our messages.
	 * 
	 * @param connection Connection with Payara Server
	 * @param topic Topic where we share messages
	 */
	public PubSubSender(TopicConnection connection, Topic topic) {
		super();
		this.connection = connection;
		this.topic = topic;
	}

	/**
	 * Method override run() method from Runnable that sends NMESSAGES to the subscribers
	 */
	@Override
	public void run(){
		try {

			// Create session and activate auto-commit
			TopicSession session = 
					connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			
			// Creating publisher and creating the message 
			TopicPublisher publisher = session.createPublisher(topic);
			TextMessage msg = session.createTextMessage();
			
			// Send NMESSAGE to the subscribers
			for(int i = 0; i < NMESSAGE; i++){
				
				// Create the message
				msg.setText("Message " + i + " to " + topicName);
				
				// Publish the message
				publisher.publish(msg);
				
				// Print the message associated to this Thread
				System.err.println("Thread " + Thread.currentThread().getId() + 
						". Enviado mensaje " + i + " a " + topicName);
				Thread.sleep(MILISLEEP);
			}

		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} 
	}
		
}
