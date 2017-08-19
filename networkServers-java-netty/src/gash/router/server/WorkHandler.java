/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server;


import gash.router.util.Logger;
import gash.router.raft.NodeState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raft.proto.Work.WorkMessage;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class WorkHandler extends SimpleChannelInboundHandler<WorkMessage> {
	protected ServerState state;
	protected boolean debug = true;

	public WorkHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
	}

	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 */
	public void handleMessage(WorkMessage msg, Channel channel) {
		System.out.println("Messsage Received"+ msg);
		
		if (msg == null) {
			// TODO add logging
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}

		if (debug){
			System.out.println("Print Work here");
			PrintUtil.printWork(msg);
			}
		

		// TODO How can you implement this without if-else statements?
		try {
			if (msg.hasTrivialPing()) {
				System.out.println("John:---------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+msg);
				Logger.DEBUG(" The node: " + msg.getTrivialPing().getNodeId() + " Is Active to this IP: "
						+ msg.getTrivialPing().getIP());
				Logger.DEBUG("Currrent Term " + NodeState.currentTerm);
				NodeState.getInstance().getServerState().getEmon().getOutboundEdges()
						.getNode(msg.getTrivialPing().getNodeId()).setChannel(channel);

			} else if (msg.hasHeartBeatPacket() && msg.getHeartBeatPacket().hasHeartbeat()) {
				System.out.println(
						"Heart Beat Packet recieved from " + msg.getHeartBeatPacket().getHeartbeat().getLeaderId());

				WorkMessage.Builder work = WorkMessage.newBuilder();
				work.setUnixTimeStamp(ServerUtils.getCurrentUnixTimeStamp());
				NodeState.getInstance().getService().handleHeartBeat(msg);

				// channel.write(work.build());
				
			} else if (msg.hasHeartBeatPacket() && msg.getHeartBeatPacket().hasHeartBeatResponse()) {
				Logger.DEBUG(
						"Response is Received from " + msg.getHeartBeatPacket().getHeartBeatResponse().getNodeId());
				NodeState.getService().handleHeartBeatResponse(msg);
			}

			else if (msg.hasVoteRPCPacket() && msg.getVoteRPCPacket().hasRequestVoteRPC()) {
					System.out.println("John:----------hasRequestVoteRPC");
					System.out.println("John:-----msg!!!!!-----msg!!!---"+ msg);
					WorkMessage voteResponse = NodeState.getInstance().getService().handleRequestVoteRPC(msg);
					channel.writeAndFlush(voteResponse);
					//channel.write(voteResponse);
				
			} else if (msg.hasVoteRPCPacket() && msg.getVoteRPCPacket().hasResponseVoteRPC()) {
				System.out.println("John:----------hasResponseVoteRPC");
				//Xing Yang added
				NodeState.getInstance().getService().handleResponseVoteRPCs(msg);
				
			} else if (msg.hasAppendEntriesPacket() && msg.getAppendEntriesPacket().hasAppendEntries()) {

				NodeState.getInstance().getService().handleAppendEntries(msg);
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		System.out.flush();

	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		System.out.println(msg);
		System.out.println("John:----------channelRead0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
		handleMessage(msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}