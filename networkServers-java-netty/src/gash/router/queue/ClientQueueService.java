package gash.router.queue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import gash.router.server.ConfigurationReader;
import gash.router.server.SystemConstants;
import gash.router.server.db.Record;
import gash.router.util.Logger;
import raft.proto.ImageTransfer;
import raft.proto.ImageTransfer.ImageMsg;

public class ClientQueueService {
	
	static ClientQueueService instance = null;

	private static final String QUEUE_URL = ConfigurationReader.getInstance().getQueueURL();

	Connection connection = null;
	Channel channel = null;	
	String callbackQueueName = null;
	String get_callbackQueueName = null;
	QueueingConsumer consumer = null;
	QueueingConsumer get_consumer = null;
	
	public static ClientQueueService getInstance() {
		if (instance == null) {
			instance = new ClientQueueService();
		}
		return instance;
	}
	
	private ClientQueueService() {
			try {
				ConnectionFactory factory = new ConnectionFactory();
				//factory.setUri(QUEUE_URL);
				factory.setHost("localHost");
		    	connection = factory.newConnection();
			    channel = connection.createChannel();		    		    
			    callbackQueueName = channel.queueDeclare().getQueue();
			    get_callbackQueueName = channel.queueDeclare().getQueue();
			    consumer = new QueueingConsumer(channel);
			    get_consumer = new QueueingConsumer(channel);
			    
			    channel.basicConsume(callbackQueueName, true, consumer);
			    channel.basicConsume(get_callbackQueueName, true, get_consumer);
			    
			/*} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private void shutdown() throws IOException {
		channel.close();
	    connection.close();
	}
		
	public void putMessage(String key, ImageTransfer.ImageMsg message) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {		
		 BasicProperties props = new BasicProperties
                 .Builder()
                 .type(SystemConstants.PUT)
                 .correlationId(key)
                 .build();

		 channel.basicPublish("", SystemConstants.INBOUND_QUEUE, props, message.getImageData().toByteArray());

	}
	
	public String postMessage(ImageTransfer.ImageMsg message) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		String corrId = java.util.UUID.randomUUID().toString();

	    BasicProperties props = new BasicProperties
	                                .Builder()
	                                .type(SystemConstants.POST)
	                                .correlationId(corrId)
	                                .replyTo(callbackQueueName)
	                                .build();
	    System.out.println("Client Queue Server post");
		channel.basicPublish("", SystemConstants.INBOUND_QUEUE, props, message.getImageData().toByteArray());
		while (true) {
			System.out.println("---client queue while true---;");
	        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
	            String key = new String(delivery.getBody());
	            return key;
	        }
	    }
	}
	
	public String postMessage(String key, byte[] data) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		String corrId = key;

	    BasicProperties props = new BasicProperties
	                                .Builder()
	                                .type(SystemConstants.POST)
	                                .correlationId(corrId)
	                                .replyTo(callbackQueueName)
	                                .build();
	    System.out.println("Client Queue Server post");
		channel.basicPublish("", SystemConstants.INBOUND_QUEUE, props, data);
		while (true) {
			System.out.println("---client queue while true---;");
	        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
	            String returnKey = new String(delivery.getBody());
//	            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	            return returnKey;
	        }
	    }
	}
	
	public void deleteMessage(String key) throws IOException {
		String corrId = java.util.UUID.randomUUID().toString();
		BasicProperties props = new BasicProperties
		         .Builder()
		         .type(SystemConstants.DELETE)
		         .correlationId(corrId)
		         .build();
		
		channel.basicPublish("", SystemConstants.INBOUND_QUEUE, props, key.getBytes());
	}
	
	public byte[] getMessage(String filename) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {		

		String corrId = java.util.UUID.randomUUID().toString();

	    BasicProperties props = new BasicProperties
	                                .Builder()
	                                .type(SystemConstants.GET)
	                                .correlationId(corrId)
	                                .replyTo(get_callbackQueueName)
	                                .build();

		
		while (true) {
			channel.basicPublish("", SystemConstants.GET_QUEUE, props, filename.getBytes());
	        QueueingConsumer.Delivery delivery = get_consumer.nextDelivery();
	        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
	            byte[] data = delivery.getBody();
//	            Logger.DEBUG("in ClientQueueService.getMessage(), the data is:  " + data.toString());
	            return data;
	        }
	    }		
	}

	public void putMessage(String key, byte[] data) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		// TODO Auto-generated method stub
		BasicProperties props = new BasicProperties
                .Builder()
                .type(SystemConstants.PUT)
                .correlationId(key)
                .build();

		 channel.basicPublish("", SystemConstants.INBOUND_QUEUE, props, data);

	}
	
}
