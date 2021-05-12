package urjc.ist.jms.pubsubexample;

import java.util.Objects;
import javax.jms.*;

public class PubSubAsyncReceiver implements Runnable, MessageListener{

	private TopicConnection connection;
	private Topic topic;
	private boolean stopFlag;

	public PubSubAsyncReceiver(TopicConnection con, Topic topic){
		this.connection = con;
		this.topic = topic;
	}

	@Override
	public void run(){
		try {   
			TopicSession session = 
					connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			TopicSubscriber subscriber = session.createSubscriber(topic);
			subscriber.setMessageListener(this);
			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " subscribed!");
			
			
			while (!stopFlag) {
				Thread.sleep(1000);
			}
			System.err.println("TRACE: Return Thread");
			return;
			
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public void onMessage(Message msg) {
		try {
			TextMessage m = (TextMessage)msg;
			
			if (Objects.equals(m.getText(), "CLOSE")){
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
