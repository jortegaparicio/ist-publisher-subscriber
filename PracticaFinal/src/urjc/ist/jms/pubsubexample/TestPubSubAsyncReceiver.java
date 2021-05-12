package urjc.ist.jms.pubsubexample;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TestPubSubAsyncReceiver {
	
	private static final String factoryName = "Factoria2";
	private static final String topicName = "Topic1";

	public static void main(String[] args) {

		try {
			InitialContext jndi = new InitialContext();
			TopicConnectionFactory factory = 
					(TopicConnectionFactory)jndi.lookup(factoryName);
			TopicConnection connection = factory.createTopicConnection();
			Topic topic = (Topic)jndi.lookup(topicName);

			PubSubAsyncReceiver receiver = new PubSubAsyncReceiver(connection, topic);
			Thread thReceiver = new Thread(receiver);
			thReceiver.start();
			System.err.println("TRACE: Waiting in join()");
			thReceiver.join();
			System.err.println("TRACE: Closing connection");
			connection.close();
			System.err.println("END");

		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (NamingException ex) {
			ex.printStackTrace();
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}
}
