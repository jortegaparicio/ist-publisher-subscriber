package urjc.ist.jms.pubsubexample;

import java.util.Objects;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * <h1>PubSubAsyncReceiver class</h1>
 * 
 * <p> The PubSubAsyncReceiver implements Runnable and MessageListener overriding run() method
 * and onMessage() method. This class subscribe each subscriber for the publisher/subscriber pattern.
 * <p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 11/05/2021
 */
public class PubSubAsyncReceiver implements Runnable, MessageListener{

	private static final int MILISLEEP       = 3000;        // ms sleeping time
	private static final String STOP         = "CLOSE";     // Message received to stop Subscriber Threads
	private static final String TOPIC_NAME   = "Topic1";    // Name of our topic

	private TopicConnectionFactory factory; // Factory that we use in the communication 
	private InitialContext jndi;            // The initial context
	private boolean stopFlag;               // Flag that stops the Threads


	/**
	 * Constructor method of this class.
	 * @param jndi Initial Context
	 * @param factory Factory of the connection
	 */
	public PubSubAsyncReceiver(InitialContext jndi, TopicConnectionFactory factory) {
		super();
		this.jndi = jndi;
		this.factory = factory;
	}
	
	/**
	 * Method that close the connection given
	 * @param connection The connection that is going to be closed
	 */
	private void closeConnection(TopicConnection connection) {
		
		System.err.println("Closing subscriber connection in Thread : " + Thread.currentThread().getId());
		// Close the connection
		try {
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Overridden method from Runnable that establish a new concurrent connection with the Payara Server.
	 * It represents the subscriber concurrent method in the publisher/subscriber pattern.
	 */
	@Override
	public void run(){
		
		TopicConnection connection = null; 
		Topic topic = null; 
		TopicSession session = null;
		TopicSubscriber subscriber = null;

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
			
			// Creating subscriber and link it to a Message Listener (to handle messages asynchronously) 
			subscriber = session.createSubscriber(topic);
			subscriber.setMessageListener(this);
			
			// Start the connection and print which Thread is subscribed
			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " subscribed!");
			
			// While we don't receive the STOP message all threads will sleep
			while (!stopFlag) {
				Thread.sleep(MILISLEEP);
			}
			
			System.err.println("TRACE: Return Thread: " + Thread.currentThread().getId());
			
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}finally {
			closeConnection(connection);
		}
		return;
	}
	
	/**
	 * Method overridden to handle received messages (asynchronously)
	 * 
	 * @param msg the message to handle
	 */
	@Override
	public void onMessage(Message msg) {
		try {
			TextMessage m = (TextMessage)msg;
			
			if (Objects.equals(m.getText(), STOP) && stopFlag == false){
				System.err.println("No more messages. Closing now listener running in thread: " + Thread.currentThread().getId());
				stopFlag = true; //Enable condition to stop current Thread		
			} else {
				System.out.println("Listener, Thread " + 
						Thread.currentThread().getId() +
						" message received: " + m.getText());
			}
			
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}
}
