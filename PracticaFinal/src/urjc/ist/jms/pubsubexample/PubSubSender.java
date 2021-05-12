package urjc.ist.jms.pubsubexample;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class PubSubSender {

	private static final String factoryName = "Factoria2";
	private static final String topicName = "Topic1";

	public static void main(String[] args) {
		try {

			InitialContext jndi = new InitialContext();
			TopicConnectionFactory factory = 
					(TopicConnectionFactory)jndi.lookup(factoryName);
			Topic topic = (Topic)jndi.lookup(topicName);

			TopicConnection connection = factory.createTopicConnection();
			TopicSession session = 
					connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			TopicPublisher publisher = session.createPublisher(topic);

			TextMessage msg = session.createTextMessage();
			for(int i = 0; i < 10; i++){
				msg.setText("Message " + i + " to " + topicName);
				publisher.publish(msg);
				System.err.println("Enviado mensaje " + i + " a " + topicName);
				Thread.sleep(1000);
			}
			System.err.println("\nSending message to close connection...");
			// Enviar mensaje de cierre al receptor
			msg.setText("CLOSE");
			publisher.publish(msg);
			connection.close();
			System.err.println("\nClosing publisher...");

		} catch (NamingException ex) {
			ex.printStackTrace();
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} 
	}

}
