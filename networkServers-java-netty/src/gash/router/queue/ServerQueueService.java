package gash.router.queue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import gash.router.raft.NodeState;
import gash.router.server.ConfigurationReader;
import gash.router.server.SystemConstants;

@SuppressWarnings("deprecation")
public class ServerQueueService {
	
	private static final String INBOUND_QUEUE = SystemConstants.INBOUND_QUEUE;
	private static final String GET_QUEUE = SystemConstants.GET_QUEUE;
	private static final String QUEUE_URL = ConfigurationReader.getInstance().getQueueURL();
	
	private static ServerQueueService instance = null;	
	private Channel channel = null;
	private Channel get_channel = null;
	private QueueingConsumer consumer = null;
	private QueueingConsumer get_consumer = null;
	private static boolean auto_ack = false; 
	public static ServerQueueService getInstance() {
		if (instance == null) {
			instance = new ServerQueueService();
		}
		return instance;
	}
	
	private ServerQueueService() {		
	}
	
	public void createQueue() {			
		    try {
		    	ConnectionFactory factory = new ConnectionFactory();
			    //factory.setUri(QUEUE_URL);
		    	   factory.setHost("localhost");
		    	   //factory.setPort(5672);
		    	   //factory.setUsername("guest");
		    	   //factory.setPassword("guest");
		    	   
		    	  // System.out.println("gxxxxxxxxx");
		    	   
			    Connection connection = factory.newConnection();
				channel = connection.createChannel();
			    channel.queueDeclare(INBOUND_QUEUE, true, false, false, null);	    
			    channel.basicQos(1);

			    consumer = new QueueingConsumer(channel);
			    channel.basicConsume(INBOUND_QUEUE, auto_ack, consumer);
			    processQueue();

			/*} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();*/
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ShutdownSignalException e) {
				e.printStackTrace();
			} catch (ConsumerCancelledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void createGetQueue() {			
	    try {
	    	ConnectionFactory factory = new ConnectionFactory();
			//factory.setUri(QUEUE_URL);
			factory.setHost("localhost");
	    	  // factory.setPort(5672);
	    	   //factory.setUsername("guest");
	    	   //factory.setPassword("guest");
	    	   System.out.println("gxxxxxxxxx");
	    	   
		    Connection connection = factory.newConnection();
			get_channel = connection.createChannel();		    
		    get_channel.queueDeclare(GET_QUEUE, true, false, false, null);	    
		    get_channel.basicQos(1);

		    get_consumer = new QueueingConsumer(get_channel);
		    get_channel.basicConsume(GET_QUEUE, auto_ack, get_consumer);
		    processGetQueue();

		} 
	    /*catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} */
		catch (IOException e) {
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
}

	private void processGetQueue() throws ShutdownSignalException, ConsumerCancelledException, InterruptedException, IOException {
	    while (true) {
	        QueueingConsumer.Delivery delivery = get_consumer.nextDelivery();	 
	        BasicProperties props = delivery.getProperties();
	        String request = props.getType();
	        System.out.println(request);

        	if (request != null && request.equals(SystemConstants.GET))  {
        		String filename = new String(delivery.getBody());	        		        	
	        	BasicProperties replyProps = new BasicProperties
	        	                                     .Builder()
	        	                                     .correlationId(props.getCorrelationId())
	        	                                     .build();
	        	byte[] getResult = NodeState.getService().handleGetMessage(filename); 		        			
	        	get_channel.basicPublish( "", props.getReplyTo(), replyProps, getResult);		        	
	        }
        	get_channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	    }	        		
	}

	public synchronized void processQueue() throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException, SQLException {
	    while (true) {
	        QueueingConsumer.Delivery delivery = consumer.nextDelivery();	 
	        BasicProperties props = delivery.getProperties();
	        String request = props.getType();
	        System.out.println(request);

	        if (request != null) {
	        	if (request.equals(SystemConstants.GET))  {
	        		String filename = new String(delivery.getBody());	        		        	
		        	BasicProperties replyProps = new BasicProperties
		        	                                     .Builder()
		        	                                     .correlationId(props.getCorrelationId())
		        	                                     .build();
		        	byte[] data = NodeState.getService().handleGetMessage(filename); 		        			
		        	channel.basicPublish( "", props.getReplyTo(), replyProps, data);
		        	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		        } 
		        
		        if (request.equals(SystemConstants.PUT))  {
		        	NodeState.getService().handlePutMessage(props.getCorrelationId(), delivery.getBody(), System.currentTimeMillis());
		        	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		        }
		        
		        if (request.equals(SystemConstants.POST))  {
		        	String key = NodeState.getService().handlePostMessage(props.getCorrelationId(), delivery.getBody(), System.currentTimeMillis());
		        	BasicProperties replyProps = new BasicProperties
		        	                                     .Builder()
		        	                                     .correlationId(props.getCorrelationId())
		        	                                     .build();
		        	
		        	channel.basicPublish( "", props.getReplyTo(), replyProps, key.getBytes());
		        	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		        }
		        
		        if (request.equals(SystemConstants.DELETE))  {
	        		String key = new String(delivery.getBody());
	        		NodeState.getService().handleDelete(key);
	        		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		        }
	        }	
	    }	        
	 }

}
