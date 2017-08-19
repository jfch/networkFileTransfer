package test.client.api;

public interface ByteClientAPI {
	
	byte[] get(String key);

	void put(String key, byte[] image);

	String post(byte[] image);
	String post(String key, byte[] image);

	void delete(String key);

	String post(String filename, int chunkId, byte[] data);	
}
