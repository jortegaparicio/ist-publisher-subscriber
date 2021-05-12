package urjc.ist.jms.pubsubexample;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Objects;


public class PubSubSyncReceiver {

	private static final String factoryName = "Factoria2";
	private static final String topicName = "Topic1";

	public static void main(String[] args) {

		try {
			InitialContext jndi = new InitialContext();
			TopicConnectionFactory factory = 
					(TopicConnectionFactory)jndi.lookup(factoryName);
			TopicConnection connection = factory.createTopicConnection();
			
			Topic topic = (Topic)jndi.lookup(topicName);
			TopicSession session = 
					connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = session.createConsumer(topic);

			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " listening!");
			
			while (!Thread.currentThread().isInterrupted()) {
				Message msg = consumer.receive();
				
				if ((msg != null) && (msg instanceof TextMessage)) {
					TextMessage m = (TextMessage)msg;
					
					if (Objects.equals(m.getText(), "CLOSE")){
						System.out.println("No more messages. Closing now!");
						break;
					} else {	
						System.out.println("Consumer, Thread " + 
								Thread.currentThread().getId() + 
								" message received: " + m.getText());
					}
				} else {
					System.err.println("Message discarded, wrong format...");
				}	
			}    
		} catch (NamingException ex) {
			ex.printStackTrace();
		} catch (JMSException ex) {
			ex.printStackTrace();
		} 
	}
}
