package test.client.api.impl;

import java.io.File;
import java.io.IOException;

import com.google.protobuf.ByteString;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

import gash.router.queue.ClientQueueService;
import gash.router.server.ConfigurationReader;
import gash.router.server.db.Record;
import raft.proto.ImageTransfer;
import test.client.api.ByteClientAPI;

public class ByteClient implements ByteClientAPI {

	private ClientQueueService queue = null;
	
	public ByteClient(String fileName) throws Exception {
//		if (fileName == null) {
//			throw new Exception("Queue Configurataion file not found");
//		}

		if (fileName != null) {
			ConfigurationReader.getInstance().loadProperties(new File(fileName));
		}
		
		queue = ClientQueueService.getInstance();
	}

	@Override
	public byte[] get(String filename) {
		try {
			return queue.getMessage(filename);
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void put(String key, byte[] data) {		
		try {
			queue.putMessage(key, data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/*	@Override
	public String post(byte[] image) {
		try {
			ImageTransfer.ImageMsg.Builder imageMsg = ImageTransfer.ImageMsg.newBuilder();
			imageMsg.setVersion(1);
			imageMsg.setImageData(ByteString.copyFrom(image));
			return queue.postMessage(imageMsg.build());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	} */
	
	@Override
	public String post(String filename, int chunkId, byte[] data) {
		try {
			String combileKey = filename + ":" + chunkId; 
			return queue.postMessage(combileKey, data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void delete(String key) {
		try {
			queue.deleteMessage(key);
		} catch (IOException e) {
			e.printStackTrace();
		};
	}

	@Override
	public String post(byte[] image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String post(String key, byte[] image) {
		// TODO Auto-generated method stub
		return null;
	}

}
