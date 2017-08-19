package gash.router.server.db;

import java.io.Serializable;
import java.util.Arrays;

public class Record implements Serializable{
	private String key = null;
	private String filename = null;
	private byte[] data = null;
	private long timestamp = 0;
	private int chunkId = 0;
	
	public Record(String key, byte[] data, long timestamp) {
		this.key = key;
		this.data = data;
		this.timestamp = timestamp;
	}
	
	public Record(String key, String filename, int chunkId, byte[] data, long timestamp){
		this.key = key;
		this.filename = filename;
		this.chunkId = chunkId;
		this.data = data;
		this.timestamp = timestamp;	
	}
	
	public Record(String filename, int chunkId, byte[] data){
		this.filename = filename;
		this.chunkId = chunkId;
		this.data = data;	
	}
	

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] image) {
		this.data = image;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getChunkId() {
		return chunkId;
	}

	public void setChunkId(int chunkId) {
		this.chunkId = chunkId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String toString() {
		return "Record [key=" + key + ", filename=" + filename + ", data=" + Arrays.toString(data) + ", timestamp="
				+ timestamp + ", chunkId=" + chunkId + "]";
	}

			
}
