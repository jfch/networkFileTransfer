package gash.router.server.db;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.Bytes;

import gash.router.util.Logger;

public class MyCassandraDB implements DatabaseClient{

	
	Cluster cluster;
	Session session;
	
	public MyCassandraDB(String url, String db) {
		cluster = Cluster.builder().addContactPoint(url).build();
		session = cluster.connect("db275");
		if(cluster ==null)	
			Logger.DEBUG("------------cluster ==null---------");
		if(session ==null)			
			Logger.DEBUG("------------session ==null---------");
			Logger.DEBUG("-----------cassandra connected---------");
	}
	
	
	@Override
	public List<Record> get(String filename) {
		List<Record> list = new ArrayList<Record>();
		int chunkId = 0; 
		byte[] data = null;
		PreparedStatement ps = null;
		com.datastax.driver.core.ResultSet rs = null;
		
		try {
			String s[] = filename.split(":");
			if(s.length > 1){
				Logger.DEBUG("This is for check item");
				String f_name = s[0];
				ps=session.prepare("Select * FROM tablename WHERE filename = ? AND chunkid = ? ALLOW FILTERING");
				BoundStatement bs=new BoundStatement(ps);
				rs = session.execute(bs.bind(f_name, Integer.parseInt(s[1])));
			}
			else {
				Logger.DEBUG("This is for READ request, filename is: " + filename + " s[0] is: " + s[0]);
				ps=session.prepare("Select * FROM tablename WHERE filename = ? ALLOW FILTERING");
				BoundStatement bs=new BoundStatement(ps);
				rs = session.execute(bs.bind(filename));
			}
			for (Row row : rs) {
				chunkId = row.getInt("chunkId");
				data = Bytes.getArray(row.getBytes("data")); 
				String key = row.getString("key");
				list.add(new Record(key,filename, chunkId, data, row.getLong("timestamp")));
			}
/*			Logger.DEBUG("in MyCassandraDB.get()");
			for(Record rec : list){
				Logger.DEBUG("record is: " + rec );
			} */
			 			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		
		return list;	
	}

	@Override
	public String post(String filename, int chunkId, byte[] data, long timestamp) {
		String key = UUID.randomUUID().toString();
		try {
			System.out.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteBuffer dbData= ByteBuffer.wrap(data);
		
		PreparedStatement ps=session.prepare("INSERT INTO tablename ( key , filename, chunkid, data , timestamp ) VALUES (?, ?, ?, ?, ?);");
		BoundStatement bs=new BoundStatement(ps);
		session.execute(bs.bind(key,filename, chunkId, dbData, timestamp));
		System.out.println("Inserted "+ key + "filename" + filename + "chunkid" + chunkId);
		return (key + ":" +filename + ":" + chunkId);
	}
	
	

	@Override
	public void post(String physicalKey, String filename, int chunkId, byte[] data, long timestamp) {
		// TODO Auto-generated method stub
		ByteBuffer dbData= ByteBuffer.wrap(data);
		
		PreparedStatement ps=session.prepare("INSERT INTO tablename ( key , filename, chunkid, data , timestamp ) VALUES (?, ?, ?, ?, ?);");
		BoundStatement bs=new BoundStatement(ps);
		session.execute(bs.bind(physicalKey,filename, chunkId, dbData, timestamp));
		System.out.println("Inserted "+ physicalKey + "filename" + filename + "chunkid" + chunkId);
	}


	@Override
	public void put(String specificKey, byte[] data, long timestamp) {
		String s[] = specificKey.split(":");
		String filename = s[0];
		int chunkId = Integer.parseInt(s[1]);
		PreparedStatement ps = null;
		try {
			ps = session.prepare("UPDATE tablename SET data = ? , timestamp = ?  WHERE filename = ? AND chunkid = ?");
			BoundStatement bs=new BoundStatement(ps);
			ByteBuffer img= ByteBuffer.wrap(data);
			session.execute(bs.bind(img,timestamp,filename,chunkId));
			
		} finally {
		}
		
	}

	@Override
	public void delete(String key) {
		PreparedStatement ps = null;
		try {
			ps= session.prepare("DELETE FROM tablename WHERE key = ? ;");			
			BoundStatement bs=new BoundStatement(ps);
			
			session.execute(bs.bind(key));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// initiate new everytime
		}
	}
		
	

	@Override
	public long getCurrentTimeStamp() {
		long timestamp = 0; 
		try {
			PreparedStatement ps= session.prepare("Select max(timestamp) FROM tablename");			
			BoundStatement bs=new BoundStatement(ps);
			com.datastax.driver.core.ResultSet rs = session.execute(bs);
	        for (Row row : rs) {
	            timestamp = row.getLong(0);
	            System.out.println(timestamp);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return timestamp;	
	}

	@Override
	public List<Record> getNewEntries(long staleTimestamp) {
		List<Record> list = new ArrayList<Record>();
			PreparedStatement ps= session.prepare("Select key, image, timestamp FROM tablename where timestamp > ?");			
			BoundStatement bs=new BoundStatement(ps);
			com.datastax.driver.core.ResultSet rs = session.execute(bs.bind(staleTimestamp));
			for (Row row : rs) {
				list.add(new Record(row.getString("key"), Bytes.getArray(row.getBytes("image")), row.getLong("timestamp")));
			}
			return list;
	}

	@Override
	public void putEntries(List<Record> list) {
		for (Record record : list) {
			put(record.getKey(), record.getData(), record.getTimestamp());
		}
	}

	@Override
	public List<Record> getAllEntries() {
		List<Record> list = new ArrayList<Record>();
		try {
			PreparedStatement ps= session.prepare("Select key, image, timestamp FROM tablename");			
			BoundStatement bs=new BoundStatement(ps);
			com.datastax.driver.core.ResultSet rs = session.execute(bs);
			for (Row row : rs) {
				list.add(new Record(row.getString("key"), Bytes.getArray(row.getBytes("image")), row.getLong("timestamp")));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return list;	
	}

	@Override
	public void post(String key, byte[] image, long timestamp) {
		PreparedStatement ps = session.prepare("INSERT INTO tablename ( key , image , timestamp ) VALUES (?, ?, ?);");
			BoundStatement bs=new BoundStatement(ps);
			ByteBuffer img= ByteBuffer.wrap(image);
			session.execute(bs.bind(key,img,timestamp));
	}

}
