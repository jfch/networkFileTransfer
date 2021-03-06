package test;

import gash.router.raft.CandidateService;
import gash.router.raft.NodeState;
import gash.router.raft.NodeTimer;
import gash.router.server.ServerUtils;

public class Test {
	public static void main(String args[]) {
	
		NodeTimer timer = new NodeTimer();
		
		System.out.println("Before updating state" + System.currentTimeMillis());
		timer.schedule(new Runnable() {
			@Override
			public void run() {				
				NodeState.getInstance().setState(NodeState.CANDIDATE);;
				Thread candidateThread = new Thread(CandidateService.getInstance());
				candidateThread.start();
			}			
		}, ServerUtils.getElectionTimeout());
		
		try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		System.out.println("After updating state" + System.currentTimeMillis());
		timer.reschedule(500);
		
		try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
}
