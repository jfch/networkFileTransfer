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
package gash.router.server.edges;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.container.RoutingConf.RoutingEntry;
import gash.router.server.ServerState;
import gash.router.server.ServerUtils;
import gash.router.server.WorkHandler;
import gash.router.server.WorkInit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import raft.proto.Ping.PingMessage;
import raft.proto.Work.WorkMessage;


public class EdgeMonitor implements EdgeListener, Runnable {

	protected static Logger logger = LoggerFactory.getLogger("edge monitor");

	private EdgeList outboundEdges;
	private EdgeList inboundEdges;
	private long dt = 2000;
	private ServerState state;
	private boolean forever = true;
	ChannelFuture channelFuture;
	
	public EdgeList getOutboundEdges() {
		return outboundEdges;
	}

	public EdgeList getInboundEdges() {
		return inboundEdges;
	}

	public EdgeMonitor(ServerState state) {
		if (state == null)
			throw new RuntimeException("state is null");

		this.outboundEdges = new EdgeList();
		this.inboundEdges = new EdgeList();
		this.state = state;
		this.state.setEmon(this);

		if (state.getConf().getRouting() != null) {
			for (RoutingEntry e : state.getConf().getRouting()) {
				outboundEdges.addNode(e.getId(), e.getHost(), e.getPort());
			}
		}

		// cannot go below 2 sec
		if (state.getConf().getHeartbeatDt() > this.dt)
			this.dt = state.getConf().getHeartbeatDt();
	}

	public void createInboundIfNew(int ref, String host, int port) {
		inboundEdges.createIfNew(ref, host, port);
	}

	private WorkMessage createPing(EdgeInfo ei) {
		WorkMessage.Builder wb = WorkMessage.newBuilder();
		PingMessage.Builder pingMessage = PingMessage.newBuilder();

		pingMessage.setNodeId(state.getConf().getNodeId());
		try {
			pingMessage.setIP(InetAddress.getLocalHost().getHostAddress());
			pingMessage.setPort(0000);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wb.setUnixTimeStamp(ServerUtils.getCurrentUnixTimeStamp());
		wb.setTrivialPing(pingMessage);

		return wb.build();
	}

	public void shutdown() {
		forever = false;
	}
	
	//creates channel between servers after which it can send heartbeat 
	public synchronized void createChannel(EdgeInfo ei) {
		try {
			EventLoopGroup group = new NioEventLoopGroup();
			Bootstrap b = new Bootstrap();
			b.handler(new WorkHandler(state));

			b.group(group).channel(NioSocketChannel.class).handler(new WorkInit(state, false));
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			ChannelFuture cf = b.connect(ei.getHost(), ei.getPort()).syncUninterruptibly();

			ei.setChannel(cf.channel());
			ei.setActive(true);
			cf.channel().closeFuture();
		}
		catch (Throwable ex) {
			System.out.println("Unable to create channel!");
			//ex.printStackTrace();
		}

		
	}

	@Override
	public void run() {
		while (forever) {
			try {
				for (EdgeInfo ei : this.outboundEdges.map.values()) {
								
					if (ei.isActive() && ei.getChannel() != null) {
						WorkMessage wm = createPing(ei);						
						ChannelFuture cf = ei.getChannel().writeAndFlush(wm);
						if (cf.isDone() && !cf.isSuccess()) {
							System.out.println("failed to send Ping Message  to server");
						}
					} else {
						// TODO create a client to the node
						createChannel(ei);
						logger.info("trying to connect to node " + ei.getRef());
						logger.info("Connected to node " + ei.getRef() + ei.isActive());
					}
				}

				Thread.sleep(dt);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.info("Unable to connect to a server");
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void onAdd(EdgeInfo ei) {
		// TODO check connection
	}

	@Override
	public synchronized void onRemove(EdgeInfo ei) {
		// TODO ?
	}
}
