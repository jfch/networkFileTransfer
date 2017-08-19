package gash.router.global;

import gash.router.server.ServerUtils;
import global.Global;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import test.client.api.impl.ByteClient;

public class RequestMessageHandler2 {
	
	private static EventLoopGroup group;

	public static Boolean toBeSentToOtherCluster = Boolean.TRUE;

	public static GlobalMessage postHandler(GlobalMessage postGlobalRequest) {

		byte[] dataStore = postGlobalRequest.getRequest().getFile().getData().toByteArray();
		String keyToBeSent = postGlobalRequest.getRequest().getFileName();

		ByteClient byteClient = null;
		try {
			byteClient = new ByteClient(null);

/*			if (RequestMessageHandler.toBeSentToOtherCluster.equals(Boolean.TRUE)) {
				//Xing Yang: we need to send this update global message to other clusters.
				for (AdapterServerConf.RoutingEntry routingEntry : AdapterServer.conf.getRouting()) {
					try {
						// Global.GlobalCommandMessage
						// globalCommandMessageToBeSent = AdapterUtils
						// .prepareClusterRouteRequestForPOST(routingEntry.getId(),
						// keyToBeSent);
						AdapterClient.sendGlobalCommandMessage(postGlobalRequest, routingEntry.getHost(),
								routingEntry.getPort());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;

			}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		String key = byteClient.post(keyToBeSent, dataStore);

		return GlobalUtils.ResponseBuilderForPOST(key);

	}

	public static GlobalMessage getHandler(GlobalMessage globalMessage) {

		try {
			ByteClient byteClient = new ByteClient("/home/vinit/workspace/RAFT/resources/queue.conf"); //Xing Yang: need to change

			byte[] imageBytes = byteClient.get(globalMessage.getRequest().getFileName());
			String keyToBeSent = globalMessage.getRequest().getFileName();

			if (RequestMessageHandler2.toBeSentToOtherCluster.equals(Boolean.TRUE))
				if (imageBytes == null) {

					//Xing Yang: if the file has not been found, we need to find it in other clusters.
					for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
						try {
							Global.GlobalMessage globalMessageToBeSent = GlobalUtils
									.prepareClusterRouteRequestForGET(globalMessage.getGlobalHeader().getDestinationId(), keyToBeSent);
							sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
									routingEntry.getPort());
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						return GlobalUtils.ResponseBuilderForGETFail(keyToBeSent);
				//		return null;
					}

				} else
					return GlobalUtils.ResponseBuilderForGET(keyToBeSent, imageBytes);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static void deleteHandler() {

	}

	public static void updateHandler() {

	}
	
	public static void sendGlobalCommandMessage(Global.GlobalMessage globalMessage, String host,
			int port) {

		System.out.println("--> initializing connection to " + host + ":" + port);

		group = new NioEventLoopGroup();
		try {
			GlobalInit si = new GlobalInit(false);
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(si);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			ChannelFuture channel = b.connect(host, port).syncUninterruptibly();

			channel.channel().writeAndFlush(globalMessage);
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
}
