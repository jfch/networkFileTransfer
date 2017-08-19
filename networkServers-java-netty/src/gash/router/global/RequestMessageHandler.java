package gash.router.global;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import gash.router.global.GlobalServerConf.RoutingEntry;
import gash.router.server.ServerUtils;
import gash.router.server.db.GetResult;
import gash.router.server.db.Record;
import gash.router.util.Logger;
import global.Global;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import test.client.api.impl.ByteClient;

public class RequestMessageHandler {
	
	private static EventLoopGroup group;

	public static Boolean toBeSentToOtherCluster = Boolean.TRUE;

	public static GlobalMessage postHandler(GlobalMessage globalMessage, Channel channel) {

		int chunkId = globalMessage.getRequest().getFile().getChunkId();
		String filename = globalMessage.getRequest().getFile().getFilename();
		byte[] dataStore = globalMessage.getRequest().getFile().getData().toByteArray();
//		int totalNoOfChunks = globalMessage.getRequest().getFile().getTotalNoOfChunks();

		if(globalMessage.getGlobalHeader().hasDestinationId()&& 
				(globalMessage.getGlobalHeader().getDestinationId() == GlobalServer.conf.getNodeId())){
			Logger.DEBUG("File could not FOUND in all clusters!");
			return GlobalUtils.ResponseBuilderForPostFail(globalMessage.getRequest());
		}
		else {
			ByteClient byteClient;
			try {
				byteClient = new ByteClient(null);
				String key = globalMessage.getRequest().getFile().getFilename() + ":" + 
						globalMessage.getRequest().getFile().getChunkId();
				byte[] dataBytes = byteClient.get(key);
				
				ByteArrayInputStream bi = new ByteArrayInputStream(dataBytes); 
		        ObjectInputStream oi;
		        GetResult getResult = null;
				oi = new ObjectInputStream(bi);
				getResult = (GetResult)oi.readObject(); 
				
				List<Record> resList = getResult.getList();
				
				if(!globalMessage.getGlobalHeader().hasDestinationId()){
					ClientList.getInstance().map.put(globalMessage.getRequest().getRequestId(), channel);
					if(resList == null || resList.size() == 0){
						//Xing Yang: The file and chunkId has not been found, we can write it into our cluster.
						Logger.DEBUG("Our client request writing file is NOT FOUND in our cluster! Write it");
						key = byteClient.post(filename, chunkId, dataStore);

					//	return GlobalUtils.ResponseBuilderForPOST(GlobalServer.conf.getNodeId(), globalMessage.getRequest(), key);
					}
					 //Forward this write request to neighbor clusters
					Logger.DEBUG("Forward this write request to neighbor clusters!");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(GlobalServer.conf.getNodeId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
				else {
					if(resList == null || resList.size() == 0){//Xing Yang: Write the file in our cluster. Send the write response to neighbor clusters.
						Logger.DEBUG("Request from other cluster and file is wrote in our cluster! Response to next cluster.");
						key = byteClient.post(filename, chunkId, dataStore);
//						for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
//							try {
//								Global.GlobalMessage globalMessageToBeSent = GlobalUtils
//										.ResponseBuilderForPOST(globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest(), key);
//								sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
//										routingEntry.getPort());
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
					}
					//Forward this write request to neighbor clusters
					Logger.DEBUG("Forward this write request to neighbor clusters!");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //Xing Yang: I have changed the code in ByteClient.java

		}
		return null;
	}

	public static List<GlobalMessage> getHandler(GlobalMessage globalMessage, Channel channel) {

		try {
			ByteClient byteClient = new ByteClient(null); //Xing Yang: I have changed the code in ByteClient.java
			Logger.DEBUG(globalMessage.getRequest().getFileName());
			
			byte[] dataBytes = byteClient.get(globalMessage.getRequest().getFileName());
			ByteArrayInputStream bi = new ByteArrayInputStream(dataBytes); 
	        ObjectInputStream oi;
	        GetResult getResult = null;
			try {
				oi = new ObjectInputStream(bi);
				getResult = (GetResult)oi.readObject(); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			List<Record> resList = getResult.getList();
			
			if(!globalMessage.getGlobalHeader().hasDestinationId()){

				ClientList.getInstance().map.put(globalMessage.getRequest().getRequestId(), channel);
				
				if(resList == null || resList.size() == 0){
					//Xing Yang: if the file has not been found, we need to send request to neighbor clusters.
					Logger.DEBUG("Our client request file NOT FOUND in our cluster!");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(GlobalServer.conf.getNodeId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						return null;
					}
				}
				else {
					Logger.DEBUG("imageBytes IS: " + dataBytes);
					Logger.DEBUG("Our client request file FOUND in our cluster! Response to client");
					return GlobalUtils.ResponseBuilderForGET(GlobalServer.conf.getNodeId(), globalMessage.getRequest(), resList);
				}
			}
			else if(globalMessage.getGlobalHeader().hasDestinationId()&& 
					(globalMessage.getGlobalHeader().getDestinationId() == GlobalServer.conf.getNodeId())){
				Logger.DEBUG("File could not FOUND in all clusters!");
				return GlobalUtils.ResponseBuilderForGETFail(globalMessage.getRequest());
			}
			else{
				if(resList == null || resList.size() == 0){
					Logger.DEBUG("Request from other cluster: file NOT FOUND in our cluster! ");
					//Xing Yang: if the file has not been found, we need to send request to neighbor clusters.
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}
				else {//Xing Yang: Send the response to neighbor clusters.
					Logger.DEBUG("Request from other cluster and file FOUND in our cluster! Response to next cluster.");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							List<GlobalMessage> globalMessageList = GlobalUtils.ResponseBuilderForGET(
									globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest(), resList);
							sendGlobalCommandMessage(globalMessageList, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}
				return null;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}


	public static GlobalMessage deleteHandler(GlobalMessage globalMessage, Channel channel) {
		String filename = globalMessage.getRequest().getFileName();

		if(globalMessage.getGlobalHeader().hasDestinationId()&& 
				(globalMessage.getGlobalHeader().getDestinationId() == GlobalServer.conf.getNodeId())){
			Logger.DEBUG("File does NOT EXIST in all clusters!");
			return GlobalUtils.ResponseBuilderForDELETEFail(globalMessage.getRequest());
		}
		else {
			ByteClient byteClient;
			try {
				byteClient = new ByteClient(null);
				byte[] dataBytes = byteClient.get(filename);
				
				ByteArrayInputStream bi = new ByteArrayInputStream(dataBytes); 
		        ObjectInputStream oi;
		        GetResult getResult = null;
				oi = new ObjectInputStream(bi);
				getResult = (GetResult)oi.readObject(); 
				
				List<Record> resList = getResult.getList();
				
				if(!globalMessage.getGlobalHeader().hasDestinationId()){
					ClientList.getInstance().map.put(globalMessage.getRequest().getRequestId(), channel);
					if(resList != null && resList.size() != 0){
						//Xing Yang:We can delete it in our cluster.
						Logger.DEBUG("Our client request deleting file is FOUND in our cluster! Delete it");
						for(Record rec : resList){
							byteClient.delete(rec.getKey());
						}
					}
					//The file has not been found. Forward this delete request to neighbor clusters
					Logger.DEBUG("Forward this delete request to neighbor clusters!");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(GlobalServer.conf.getNodeId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
				else {
					//Forward this delete request to neighbor clusters
					Logger.DEBUG("Forward this write request to neighbor clusters!");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(resList != null && resList.size() != 0){
						//Xing Yang: Delete the file in our cluster. Send the write response to neighbor clusters.
						Logger.DEBUG("Request from other cluster and file is deleted in our cluster! Response to next cluster.");
						for(Record rec : resList){
							byteClient.delete(rec.getKey());
						}
//						for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
//							try {
//								Global.GlobalMessage globalMessageToBeSent = GlobalUtils
//										.ResponseBuilderForDelete(globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest(), filename);
//								sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
//										routingEntry.getPort());
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
					}
					return null;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //Xing Yang: I have changed the code in ByteClient.java

		}
		return null;
	}

	public static GlobalMessage updateHandler(GlobalMessage globalMessage, Channel channel) {
		int chunkId = globalMessage.getRequest().getFile().getChunkId();
		String filename = globalMessage.getRequest().getFile().getFilename();
		byte[] dataStore = globalMessage.getRequest().getFile().getData().toByteArray();
		
		if(globalMessage.getGlobalHeader().hasDestinationId()&& 
				(globalMessage.getGlobalHeader().getDestinationId() == GlobalServer.conf.getNodeId())){
			Logger.DEBUG("File does NOT EXIST in all clusters!");
			return GlobalUtils.ResponseBuilderForUpdateFail(globalMessage.getRequest());
		}
		else {
			ByteClient byteClient;
			try {
				byteClient = new ByteClient(null);
				String key = globalMessage.getRequest().getFile().getFilename() + ":" + 
						globalMessage.getRequest().getFile().getChunkId();
				byte[] dataBytes = byteClient.get(key);
				
				ByteArrayInputStream bi = new ByteArrayInputStream(dataBytes); 
		        ObjectInputStream oi;
		        GetResult getResult = null;
				oi = new ObjectInputStream(bi);
				getResult = (GetResult)oi.readObject(); 
				
				List<Record> resList = getResult.getList();
				
				if(!globalMessage.getGlobalHeader().hasDestinationId()){
					ClientList.getInstance().map.put(globalMessage.getRequest().getRequestId(), channel);

					//The file has not been found. Forward this update request to neighbor clusters
					Logger.DEBUG("Forward this update request to neighbor clusters!");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(GlobalServer.conf.getNodeId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						return null;
					}
				
					if(resList != null && resList.size() != 0){ 
						//Xing Yang:We can update it in our cluster.
						Logger.DEBUG("Our client request update file is FOUND in our cluster! Update it");
						byteClient.put(key, dataStore);

//						return GlobalUtils.ResponseBuilderForUpdate(GlobalServer.conf.getNodeId(), globalMessage.getRequest(), key);
					}
				}
				else {
					//Forward this update request to neighbor clusters
					Logger.DEBUG("Forward this update request to neighbor clusters!");
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequest(globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest());
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				
					if(resList != null && resList.size() != 0){
						//Xing Yang: Update the file in our cluster. Send the Update response to neighbor clusters.
						Logger.DEBUG("Request from other cluster and file is updated in our cluster! Response to next cluster.");
						byteClient.put(key, dataStore);;
//						for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
//							try {
//								Global.GlobalMessage globalMessageToBeSent = GlobalUtils
//										.ResponseBuilderForUpdate(globalMessage.getGlobalHeader().getDestinationId(), globalMessage.getRequest(), key);
//								sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
//										routingEntry.getPort());
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
					}
					return null;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //Xing Yang: I have changed the code in ByteClient.java

		}
		return null;
	}
	

	public static void askTheLeader(RoutingEntry routingEntry) {
		// TODO Auto-generated method stub
		Global.GlobalMessage.Builder whoIsLeaderMessage = Global.GlobalMessage.newBuilder();

		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId( GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());


		whoIsLeaderMessage.setGlobalHeader(header);

		Global.WhoIsLeader.Builder whoIsLeader = Global.WhoIsLeader.newBuilder();
		whoIsLeader.setRequesterIp(GlobalServer.localSocketAddress.getHostName());
		whoIsLeader.setRequesterPort(GlobalServer.localSocketAddress.getPort());

		whoIsLeaderMessage.setWhoIsClusterLeader(whoIsLeader);
		
		Logger.DEBUG("--> initializing connection to " + routingEntry.getHost() + ":" + routingEntry.getPort());

		group = new NioEventLoopGroup();
		try {
			GlobalInit si = new GlobalInit(false);
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(si);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			ChannelFuture channel = b.connect(routingEntry.getHost(), routingEntry.getPort()).syncUninterruptibly();

			channel.channel().writeAndFlush(whoIsLeaderMessage.build());
			// want to monitor the connection to the server s.t. if we loose the
			// connection, we can try to re-establish it.
			// ClientClosedListener ccl = new ClientClosedListener(this);
			// channel.channel().closeFuture().addListener(ccl);

			System.out.println(channel.channel().localAddress() + " -> open: " + channel.channel().isOpen()
					+ ", write: " + channel.channel().isWritable() + ", reg: " + channel.channel().isRegistered());

		} catch (Throwable ex) {
			System.out.println("failed to initialize the client connection " + ex.toString());
			ex.printStackTrace();
		}
	}
	
	public static void sendGlobalCommandMessage(Global.GlobalMessage globalMessage, String host,
			int port) {

		Logger.DEBUG("--> initializing connection to " + host + ":" + port);

		group = new NioEventLoopGroup();
		try {
			GlobalInit si = new GlobalInit(false);
			Bootstrap b = new Bootstrap();
			//b.handler(new GlobalHandler());
			b.group(group).channel(NioSocketChannel.class).handler(si);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			Thread.sleep(3000);			
			ChannelFuture channel;
			//channel = b.connect(host, port).syncUninterruptibly();
			
			//for (int i =0; i<10; i++){
				Logger.DEBUG("---------BEFORE CHANNEL CONNECTING------------"+host+"  "+port);
				channel = b.connect(host, port).syncUninterruptibly();
				Logger.DEBUG("---------CHANNEL CONNECTING------------");
				Thread.sleep(10000);
				channel.channel().writeAndFlush(globalMessage);
				
				System.out.println(channel.channel().localAddress() + " -> open: " + channel.channel().isOpen()
						+ ", write: " + channel.channel().isWritable() + ", reg: " + channel.channel().isRegistered());
			
				
			// want to monitor the connection to the server s.t. if we loose the
			// connection, we can try to re-establish it.
			// ClientClosedListener ccl = new ClientClosedListener(this);
			// channel.channel().closeFuture().addListener(ccl);

			

		} catch (Throwable ex) {
			System.out.println("failed to initialize the client connection " + ex.toString());
			ex.printStackTrace();
		}

	}
	
	private static void sendGlobalCommandMessage(List<GlobalMessage> globalMessageList, String host, int port) {
		// TODO Auto-generated method stub
		Logger.DEBUG("--> initializing connection to " + host + ":" + port);

		group = new NioEventLoopGroup();
		try {
			GlobalInit si = new GlobalInit(false);
			Bootstrap b = new Bootstrap();
			//b.handler(new GlobalHandler());
			b.group(group).channel(NioSocketChannel.class).handler(si);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			Thread.sleep(3000);			
			ChannelFuture channel;
			//channel = b.connect(host, port).syncUninterruptibly();

			Logger.DEBUG("---------BEFORE CHANNEL CONNECTING------------"+host+"  "+port);
			channel = b.connect(host, port).syncUninterruptibly();
			Logger.DEBUG("---------CHANNEL CONNECTING------------");
			Thread.sleep(10000);
			for(GlobalMessage globalMessage : globalMessageList){
				channel.channel().writeAndFlush(globalMessage);
			}
			
			System.out.println(channel.channel().localAddress() + " -> open: " + channel.channel().isOpen()
					+ ", write: " + channel.channel().isWritable() + ", reg: " + channel.channel().isRegistered());
			

		} catch (Throwable ex) {
			System.out.println("failed to initialize the client connection " + ex.toString());
			ex.printStackTrace();
		}
	}
}
