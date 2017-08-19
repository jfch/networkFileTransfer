package gash.router.server.db;

import java.util.List;

public interface DatabaseClient {
	
	List<Record> get(String key);
	
//	String post(byte[] image, long timestamp);
	
	public void put(String key, byte[] image, long timestamp);
	
	public void delete(String key);

	long getCurrentTimeStamp();

	List<Record> getNewEntries(long staleTimestamp);

	void putEntries(List<Record> list);

	List<Record> getAllEntries();

	void post(String key, byte[] image, long timestamp);

	String post(String filename, int chunkId, byte[] data, long timestamp);

	void post(String physicalKey, String filename, int chunkId, byte[] data, long unixTimeStamp);
	
}
