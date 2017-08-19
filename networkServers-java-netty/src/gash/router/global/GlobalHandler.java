/*
 * copyright 2016, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.global;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import com.google.protobuf.ByteString;

import gash.router.server.ServerUtils;
import gash.router.util.Logger;
import global.Global;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import global.Global.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class GlobalHandler extends SimpleChannelInboundHandler<Global.GlobalMessage> {

	// protected ConcurrentMap<String, MonitorListener> listeners = new
	// ConcurrentHashMap<String, MonitorListener>();
	String outputPath = "/home/vinit";

	public GlobalHandler() {
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Global.GlobalMessage globalMessage)
			throws Exception {
		// TODO Auto-generated method stub
		Logger.DEBUG("---GLOBAL MESSAGE RECEIVED from---\n " + globalMessage);
		
		if (globalMessage.hasGlobalHeader()) {
			Logger.DEBUG("Inter-Cluster message recieved from " + globalMessage.getGlobalHeader().getClusterId());
		}
		if (globalMessage.hasPing()) {
			Logger.DEBUG("---GLOBAL MESSAGE is PING: ---\n " + globalMessage);
			Logger.DEBUG("PING from " + globalMessage.getGlobalHeader().getClusterId());
			if(!globalMessage.getGlobalHeader().hasDestinationId()){
				makePing(GlobalServer.conf.getNodeId());
			}
//			else if()
			makePing(globalMessage.getGlobalHeader().getDestinationId());
/*			NodeState.getInstance().getServerState().getEmon().getOutboundEdges()
					.getNode(msg.getTrivialPing().getNodeId()).setChannel(ctx.channel());*/

		}/* else if (globalMessage.hasWhoIsClusterLeader()) {
			String socketHost = GlobalServer.localSocketAddress.getHostName();
		    int socketPort = GlobalServer.localSocketAddress.getPort();
		    
		    GlobalMessage leaderInfo = prepareLeaderInfo(globalMessage, socketHost, socketPort);
		    ctx.channel().writeAndFlush(leaderInfo);
		    
		} */else if (globalMessage.hasClusterLeaderInfo()){
			String hostname = globalMessage.getClusterLeaderInfo().getLeaderIp();
			int port = globalMessage.getClusterLeaderInfo().getLeaderPort();
			GlobalServer.neighborLeader = new InetSocketAddress(hostname,port);

		} else if (globalMessage.hasRequest()) {

			if(globalMessage.getRequest().getRequestType() == Global.RequestType.READ){
				List<GlobalMessage> response = RequestMessageHandler.getHandler(globalMessage, ctx.channel());

				if(response != null || response.size() != 0){
					Channel ch = ClientList.getInstance().map.get(globalMessage.getRequest().getRequestId());
					ClientList.getInstance().map.remove(globalMessage.getRequest().getRequestId());
					for(GlobalMessage gm : response){
						Logger.DEBUG("GlobalMessage for get request is: " + gm.toString());
						ch.writeAndFlush(gm);
					}
					
				}
			
			}else if (globalMessage.getRequest().getRequestType() == Global.RequestType.WRITE) {
				GlobalMessage response = RequestMessageHandler.postHandler(globalMessage, ctx.channel());

				if(response != null){
					Channel ch = ClientList.getInstance().map.get(globalMessage.getRequest().getRequestId());
//					ClientList.getInstance().map.remove(globalMessage.getRequest().getRequestId());
					ch.writeAndFlush(response);			
				}				
			}else if (globalMessage.getRequest().getRequestType() == Global.RequestType.UPDATE) {
				GlobalMessage response = RequestMessageHandler.updateHandler(globalMessage, ctx.channel());

				if(response != null){
					Channel ch = ClientList.getInstance().map.get(globalMessage.getRequest().getRequestId());
					ClientList.getInstance().map.remove(globalMessage.getRequest().getRequestId());
					ch.writeAndFlush(response);			
				}
				
			}else if (globalMessage.getRequest().getRequestType() == Global.RequestType.DELETE) {
				GlobalMessage response = RequestMessageHandler.deleteHandler(globalMessage, ctx.channel());

				if(response != null){
					Channel ch = ClientList.getInstance().map.get(globalMessage.getRequest().getRequestId());
					ClientList.getInstance().map.remove(globalMessage.getRequest().getRequestId());
					ch.writeAndFlush(response);	
				}
			}
			
		} else if (globalMessage.hasResponse()) {
			Logger.DEBUG("Inter-Cluster message response from " + globalMessage.getGlobalHeader().getClusterId());
			Response res = globalMessage.getResponse();

			if(globalMessage.getGlobalHeader().getDestinationId() == GlobalServer.conf.getNodeId()){
				
				Channel ch = ClientList.getInstance().map.get(globalMessage.getResponse().getRequestId());
				//ClientList.getInstance().map.remove(globalMessage.getResponse().getRequestId());
				Logger.DEBUG("res---:"+res);
				ch.writeAndFlush(res);
			
			}
			else {
				/* This response is not needed by our cluster. We need to send this globalMessage to 
				neighbor clusters. */
				GlobalMessage newMessage = buildForwardResponse(globalMessage);
				for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
					try {
						RequestMessageHandler.sendGlobalCommandMessage(newMessage, routingEntry.getHost(),
								routingEntry.getPort());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
			
		}
	}

/*	private GlobalMessage prepareLeaderInfo(GlobalMessage globalMessage, String socketHost, int socketPort) {
		Global.GlobalMessage.Builder leaderInfoMessage = Global.GlobalMessage.newBuilder();

		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId( GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());


		leaderInfoMessage.setGlobalHeader(header);

		Global.LeaderInfo.Builder leaderInfo = Global.LeaderInfo.newBuilder();
		leaderInfo.setLeaderIp(socketHost);
		leaderInfo.setLeaderPort(socketPort);

		leaderInfoMessage.setClusterLeaderInfo(leaderInfo);

		return leaderInfoMessage.build();
	} */
	
	private GlobalMessage buildForwardResponse(GlobalMessage old_message) {
		// TODO Auto-generated method stub
		GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		header.setDestinationId(old_message.getGlobalHeader().getDestinationId());
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(old_message.getResponse());
		return globalmsg.build();
	}

	private void makePing(int destinationId) {
		// TODO Auto-generated method stub
		for (GlobalServerConf.RoutingEntry routingEntry : GlobalServer.conf.getRouting()) {
			try {
				Global.GlobalMessage globalMessageToBeSent = GlobalUtils
						.prepareClusterRoutePing(destinationId);
			
				Logger.DEBUG("HOST: ---"+routingEntry.getHost()+ "---"+routingEntry.getPort());
				RequestMessageHandler.sendGlobalCommandMessage(globalMessageToBeSent, routingEntry.getHost(),
						routingEntry.getPort());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	/*
	 * public void addListener(MonitorListener listener) { if (listener == null)
	 * return;
	 * 
	 * listeners.putIfAbsent(listener.getListenerID(), listener); }
	 */

}