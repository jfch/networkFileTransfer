package gash.router.raft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import gash.router.queue.ServerQueueService;
import gash.router.server.ServerUtils;
import gash.router.server.db.DatabaseService;
import gash.router.server.db.GetResult;
import gash.router.server.edges.EdgeInfo;
import gash.router.util.Logger;
import io.netty.channel.ChannelFuture;
import raft.proto.AppendEntriesRPC.AppendEntries.RequestType;
import raft.proto.VoteRPC.ResponseVoteRPC;
import raft.proto.Work.WorkMessage;

public class FollowerService extends Service implements Runnable {

	public static Boolean isHeartBeatRecieved = Boolean.FALSE;
	NodeTimer timer;

	private static FollowerService INSTANCE = null;
	Thread fThread = null;
	private FollowerService() {
		// TODO Auto-generated constructor stub
	}

	public static FollowerService getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FollowerService();

		}
		return INSTANCE;
	}

	@Override
	public void run() {
		//Logger.DEBUG("----------------------FOLLOWER SERVICE STARTED ----------------------------");
		System.out.println("----------FollwerService.run()-----------where I am"+": FOLLOWER state ----------------------------");
		if( NodeState.getInstance().getServerState()!=null){
			java.util.Date date = new java.util.Date();
			gash.router.util.Logger.DEBUG("--------FollwerServeice.run()--------------"
					+ "-Node " + NodeState.getInstance().getServerState().getConf().getNodeId() 
					+  ": FOLLOWER SERVICE STARTED -------------------------"+" date:" + date);
		}
		initFollower();
		
		fThread = new Thread(){
		    public void run(){
				while (running) {
					while (NodeState.getInstance().getState() == NodeState.FOLLOWER) {
					}
				}

		    }
		 };

		fThread.start();
		ServerQueueService.getInstance().createGetQueue();
	}

	private void initFollower() {
		// TODO Auto-generated method stub

		timer = new NodeTimer();

		timer.schedule(new Runnable() {
			@Override
			public void run() {
				NodeState.getInstance().setState(NodeState.CANDIDATE);
			}
		}, ServerUtils.getElectionTimeout());

	}

	public void onReceivingHeartBeatPacket() {
		timer.reschedule(ServerUtils.getElectionTimeout());
	}

	@Override
	public WorkMessage handleRequestVoteRPC(WorkMessage workMessage) {

		if (workMessage.getVoteRPCPacket().getRequestVoteRPC().getTimeStampOnLatestUpdate() < NodeState.getTimeStampOnLatestUpdate()) {
			Logger.DEBUG(NodeState.getInstance().getServerState().getConf().getNodeId() + " has replied NO");
			return ServiceUtils.prepareResponseVoteRPC(ResponseVoteRPC.IsVoteGranted.NO);

		}
		Logger.DEBUG(NodeState.getInstance().getServerState().getConf().getNodeId() + " has replied YES");
		return ServiceUtils.prepareResponseVoteRPC(ResponseVoteRPC.IsVoteGranted.YES);

	}

	public void handleHeartBeat(WorkMessage wm) {
		Logger.DEBUG("HeartbeatPacket received from leader :" + wm.getHeartBeatPacket().getHeartbeat().getLeaderId());
		NodeState.currentTerm = wm.getHeartBeatPacket().getHeartbeat().getTerm();
		onReceivingHeartBeatPacket();
		WorkMessage heartBeatResponse = ServiceUtils.prepareHeartBeatResponse();

		for (EdgeInfo ei : NodeState.getInstance().getServerState().getEmon().getOutboundEdges().getMap().values()) {

			if (ei.isActive() && ei.getChannel() != null
					&& ei.getRef() == wm.getHeartBeatPacket().getHeartbeat().getLeaderId()) {

				Logger.DEBUG("Sent HeartBeatResponse to " + ei.getRef());
				ChannelFuture cf = ei.getChannel().writeAndFlush(heartBeatResponse);
				if (cf.isDone() && !cf.isSuccess()) {
					Logger.DEBUG("failed to send message (HeartBeatResponse) to server");
				}
			}
		}

	}

	@Override
	public void handleAppendEntries(WorkMessage wm) {
		String key = wm.getAppendEntriesPacket().getAppendEntries().getImageMsg().getKey();
		byte[] data = wm.getAppendEntriesPacket().getAppendEntries().getImageMsg().getImageData().toByteArray();
		long unixTimeStamp = wm.getAppendEntriesPacket().getAppendEntries().getTimeStampOnLatestUpdate();
		RequestType type = wm.getAppendEntriesPacket().getAppendEntries().getRequestType();
		
		if (type == RequestType.GET) {
			DatabaseService.getInstance().getDb().get(key);
		} else if (type == RequestType.POST) {
			NodeState.setTimeStampOnLatestUpdate(unixTimeStamp);
			String s[] = key.split(":");
			String physicalKey = s[0];
			String filename = s[1];
			int chunkId = Integer.parseInt(s[2]);
			DatabaseService.getInstance().getDb().post(physicalKey, filename, chunkId, data, unixTimeStamp);
		} else if (type == RequestType.PUT) {
			NodeState.setTimeStampOnLatestUpdate(unixTimeStamp);
			DatabaseService.getInstance().getDb().put(key, data, unixTimeStamp);
		} else if (type == RequestType.DELETE) {
			NodeState.setTimeStampOnLatestUpdate(System.currentTimeMillis());
			DatabaseService.getInstance().getDb().delete(key);
		}
		
		Logger.DEBUG("Inserted entry with key " + key + " received from "
				+ wm.getAppendEntriesPacket().getAppendEntries().getLeaderId());
	}
	
	@Override
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


	@Override
	public void startService(Service service) {

		running = Boolean.TRUE;
		cthread = new Thread((FollowerService) service);
		cthread.start();

	}

	@Override
	public void stopService() {
		running = Boolean.FALSE;
	}

}
