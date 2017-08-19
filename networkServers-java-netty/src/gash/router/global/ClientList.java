package gash.router.global;

import java.util.HashMap;

import gash.router.server.edges.EdgeInfo;
import io.netty.channel.Channel;

public class ClientList {

	private static ClientList instance = new ClientList();  
	
	protected HashMap<String, Channel> map = new HashMap<String, Channel>();
	
	private ClientList (){}
	public static ClientList getInstance() {  
		return instance;  
	}
	
}
