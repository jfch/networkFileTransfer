package gash.router.raft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import gash.router.queue.ServerQueueService;
import gash.router.server.db.DatabaseService;
import gash.router.server.db.GetResult;
import gash.router.server.edges.EdgeInfo;
import gash.router.util.Logger;
import io.netty.channel.ChannelFuture;
import raft.proto.AppendEntriesRPC.AppendEntries.RequestType;
import raft.proto.Work.WorkMessage;

public class LeaderService extends Service implements Runnable {

	private static LeaderService INSTANCE = null;
	Thread heartBt = null;
	private LeaderService() {
		// TODO Auto-generated constructor stub

	}

	public static LeaderService getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LeaderService();
		}
		return INSTANCE;
	}

	@Override
	public void run() {
		java.util.Date date = new java.util.Date();
		Logger.DEBUG("-----------------------Node " + NodeState.getInstance().getServerState().getConf().getNodeId() 
				+  ": LEADER SERVICE STARTED ----------------------------" +" date:" + date);
		//Logger.DEBUG("----------running-------------LEADER SERVICE STARTED ----------------------------");

		//initLatestTimeStampOnUpdate();

		heartBt = new Thread(){
		    public void run(){
		    	System.out.println("John:running!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+running);
				while (running) {
					try {						
						Thread.sleep(NodeState.getInstance().getServerState().getConf().getHeartbeatDt());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					sendHeartBeat();
				}
		    }
		 };

		heartBt.start();
		ServerQueueService.getInstance().createQueue();
	}

	private void initLatestTimeStampOnUpdate() {

		NodeState.setTimeStampOnLatestUpdate(DatabaseService.getInstance().getDb().getCurrentTimeStamp());

	}

	private void sendAppendEntriesPacket(WorkMessage workMessage) {

			for (EdgeInfo ei : NodeState.getInstance().getServerState().getEmon().getOutboundEdges().getMap()
					.values()) {

				if (ei.isActive() && ei.getChannel() != null) {

					Logger.DEBUG("Sent AppendEntriesPacket to " + ei.getRef() + "for the key " + workMessage.getAppendEntriesPacket().getAppendEntries().getImageMsg().getKey());

					ChannelFuture cf = ei.getChannel().writeAndFlush(workMessage);
					if (cf.isDone() && !cf.isSuccess()) {
						Logger.DEBUG("failed to send message (AppendEntriesPacket) to server");
					}
				}
			}
	}

	public void handleHeartBeatResponse(WorkMessage wm) {
		/*
		long timeStampOnLatestUpdate = wm.getHeartBeatPacket().getHeartBeatResponse().getTimeStampOnLatestUpdate();

		if (DatabaseService.getInstance().getDb().getCurrentTimeStamp() > timeStampOnLatestUpdate) {
			List<Record> laterEntries = DatabaseService.getInstance().getDb().getNewEntries(timeStampOnLatestUpdate);

			for (EdgeInfo ei : NodeState.getInstance().getServerState().getEmon().getOutboundEdges().getMap()
					.values()) {

				if (ei.isActive() && ei.getChannel() != null
						&& ei.getRef() == wm.getHeartBeatPacket().getHeartBeatResponse().getNodeId()) {

					for (Record record : laterEntries) {
						WorkMessage workMessage = ServiceUtils.prepareAppendEntriesPacket(record.getKey(),
								record.getImage(), record.getTimestamp(), RequestType.POST);
						Logger.DEBUG("Sent AppendEntriesPacket to " + ei.getRef() + "for the key (later Entries) "
								+ record.getKey());
						ChannelFuture cf = ei.getChannel().writeAndFlush(workMessage);
						if (cf.isDone() && !cf.isSuccess()) {
							Logger.DEBUG("failed to send message (AppendEntriesPacket) to server");
						}
					}
				}
			}

		}
		*/

	}
	
	public void handleHeartBeat(WorkMessage wm) {
		Logger.DEBUG("HeartbeatPacket received from leader :" + wm.getHeartBeatPacket().getHeartbeat().getLeaderId());
		//onReceivingHeartBeatPacket();
		WorkMessage heartBeatResponse = ServiceUtils.prepareHeartBeatResponse();
		
		for (EdgeInfo ei : NodeState.getInstance().getServerState().getEmon().getOutboundEdges().getMap().values()) {

			if (ei.isActive() && ei.getChannel() != null
					&& ei.getRef() == wm.getHeartBeatPacket().getHeartbeat().getLeaderId()) {
					if(wm.getHeartBeatPacket().getHeartbeat().getTerm()>=NodeState.currentTerm) {
						NodeState.getInstance().setState(NodeState.FOLLOWER);
					}
//				Logger.DEBUG("Sent HeartBeatResponse to " + ei.getRef());
//				ChannelFuture cf = ei.getChannel().writeAndFlush(heartBeatResponse);
//				if (cf.isDone() && !cf.isSuccess()) {
//					Logger.DEBUG("failed to send message (HeartBeatResponse) to server");
//				}
			}
		}

	}

	@Override
	public void sendHeartBeat() {
		for (EdgeInfo ei : NodeState.getInstance().getServerState().getEmon().getOutboundEdges().getMap().values()) {
			if (ei.isActive() && ei.getChannel() != null) {
				WorkMessage workMessage = ServiceUtils.prepareHeartBeat();
				Logger.DEBUG("Sent HeartBeatPacket to " + ei.getRef());
				ChannelFuture cf = ei.getChannel().writeAndFlush(workMessage);
				if (cf.isDone() && !cf.isSuccess()) {
					Logger.DEBUG("failed to send message (HeartBeatPacket) to server");
				}
			}
		}
//		if (ConfigurationReader.getInstance().getMonitorHost() != null && ConfigurationReader.getInstance().getMonitorPort() != null) {
//			sendClusterMonitor(ConfigurationReader.getInstance().getMonitorHost(), ConfigurationReader.getInstance().getMonitorPort());
//		}		
	}
	
//	public void sendClusterMonitor(String host, int port) {
//		try {
//			MonitorClient mc = new MonitorClient(host, port);
//			MonitorClientApp ma = new MonitorClientApp(mc);
//			// do stuff w/ the connection
//			System.out.println("Creating message");
//			ClusterMonitor msg = ma.sendDummyMessage(countActiveNodes(),NodeState.getupdatedTaskCount());
//			System.out.println("Sending generated message");
//			mc.write(msg);	
//		}catch(Exception e) {
//			e.printStackTrace();
//		}	
//	}
	
	public int countActiveNodes() {
		int count = 0;
		for (EdgeInfo ei : NodeState.getInstance().getServerState().getEmon().getOutboundEdges().getMap()
				.values()) {

			if (ei.isActive() && ei.getChannel() != null) {				
				count++;
				
			}
		}
		return count;
	}

	public byte[] handleGetMessage(String filename) {
		System.out.println("GET Request Processed by Node: " + NodeState.getInstance().getServerState().getConf().getNodeId());
		NodeState.updateTaskCount();
		
		// make Recode and GetResult Serializable and put all the get result into GetResult object.
		GetResult getRes = new GetResult(DatabaseService.getInstance().getDb().get(filename));
		
		//Then transform it to byte[]
        ByteArrayOutputStream bo = new ByteArrayOutputStream(); 
        ObjectOutputStream oo;
		try {
			oo = new ObjectOutputStream(bo);
			oo.writeObject(getRes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return bo.toByteArray(); 
	}
	
	public String handlePostMessage(byte[] image, long timestamp) {
		System.out.println("POST Request Processed by Node: " + NodeState.getInstance().getServerState().getConf().getNodeId());
		NodeState.updateTaskCount();
		NodeState.setTimeStampOnLatestUpdate(timestamp);
/* Xing Yang temporary comment for read test
		String key = DatabaseService.getInstance().getDb().post(filename, chunkId, data, timestamp);
		WorkMessage wm = ServiceUtils.prepareAppendEntriesPacket(key, image, timestamp, RequestType.POST);
		sendAppendEntriesPacket(wm);
		return key; */
		return null;
	}

	@Override
	public String handlePostMessage(String key, byte[] data, long timestamp) {
		System.out.println("POST Request Processed by Node: " + NodeState.getInstance().getServerState().getConf().getNodeId());
		NodeState.updateTaskCount();
		NodeState.setTimeStampOnLatestUpdate(timestamp);
		String s[] = key.split(":");
		String filename = s[0];
		int chunkId = Integer.parseInt(s[1]);
		String specificKey = DatabaseService.getInstance().getDb().post(filename, chunkId, data, timestamp);
		WorkMessage wm = ServiceUtils.prepareAppendEntriesPacket(specificKey, data, timestamp, RequestType.POST);
		sendAppendEntriesPacket(wm);
		return key;
	}

	public void handlePutMessage(String key, byte[] data, long timestamp) {
		System.out.println("PUT Request Processed by Node: " + NodeState.getInstance().getServerState().getConf().getNodeId());
		NodeState.updateTaskCount();
		NodeState.setTimeStampOnLatestUpdate(timestamp);
		DatabaseService.getInstance().getDb().put(key, data, timestamp);
		WorkMessage wm = ServiceUtils.prepareAppendEntriesPacket(key, data, timestamp, RequestType.PUT);
		sendAppendEntriesPacket(wm);
	}
	
	@Override
	public void handleDelete(String key) {
		System.out.println("DELETE Request Processed by Node: " + NodeState.getInstance().getServerState().getConf().getNodeId());
		NodeState.updateTaskCount();
		NodeState.setTimeStampOnLatestUpdate(System.currentTimeMillis());
		DatabaseService.getInstance().getDb().delete(key);
		WorkMessage wm = ServiceUtils.prepareAppendEntriesPacket(key, null, 0 ,RequestType.DELETE);
		sendAppendEntriesPacket(wm);
	}	

	public void startService(Service service) {
		running = Boolean.TRUE;
		cthread = new Thread((LeaderService) service);
		cthread.start();
	}

	public void stopService() {
		running = Boolean.FALSE;

	}

}
