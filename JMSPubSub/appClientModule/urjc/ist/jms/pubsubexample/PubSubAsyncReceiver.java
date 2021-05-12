package urjc.ist.jms.pubsubexample;

import java.util.Objects;
import javax.jms.*;


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

	private static final int MILISLEEP = 1000;    // ms sleeping time
	private static final String STOP   = "CLOSE"; // Message received to stop Subscriber Threads

	private TopicConnection connection; // Establish a connection with Payara Server
	private Topic topic;                // Topic where we receive messages
	private boolean stopFlag;           // Flag that stops the Threads

	/**
	 * Constructor method of PubSubAsyncReceiver class with parameters.
	 * @param con Connection parameter, to establish a connection with the Payara Server
	 * @param topic Topic where we receive messages
	 */
	public PubSubAsyncReceiver(TopicConnection con, Topic topic){
		this.connection = con;
		this.topic = topic;
	}

	/**
	 * Overridden method from Runnable that establish a new concurrent connection with the Payara Server.
	 * It represents the subscriber concurrent method in the publisher/subscriber pattern.
	 */
	@Override
	public void run(){
		try {   
			
			// Create session and activate auto-commit
			TopicSession session = 
					connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			
			// Creating subscriber and link it to a Message Listener (to handle messages asynchronously) 
			TopicSubscriber subscriber = session.createSubscriber(topic);
			subscriber.setMessageListener(this);
			
			// Start the connection and print which Thread is subscribed
			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " subscribed!");
			
			// While we don't receive the STOP message all threads will sleep
			while (!stopFlag) {
				Thread.sleep(MILISLEEP);
			}
			
			System.err.println("TRACE: Return Thread");
			return;
			
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
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
				System.out.println("No more messages. Closing now!");
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
