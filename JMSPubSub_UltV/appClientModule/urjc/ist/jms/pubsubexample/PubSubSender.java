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

	private static final String TOPIC_NAME  = "Topic1"; // Name of the topic
	private static final int    NMESSAGE    = 3;        // Number of messages 
	private static final int    MILISLEEP   = 3000;     // ms sleeping time
	
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
	 * Method that close the session given
	 * @param session The session that is going to be closed
	 */
	private void closeSession(TopicSession session) {
		
		System.err.println("Closing publisher session in Thread : " + Thread.currentThread().getId());

		try {
			session.close();
		} catch (JMSException e) {
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

		try {

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
			closeSession(session);
		}
	}

	@Override
	public String toString() {
		return "PubSubSender [connection=" + connection + ", topic=" + topic + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connection == null) ? 0 : connection.hashCode());
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PubSubSender other = (PubSubSender) obj;
		if (connection == null) {
			if (other.connection != null)
				return false;
		} else if (!connection.equals(other.connection))
			return false;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		return true;
	}
		
}
