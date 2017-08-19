package gash.router.global;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.ByteString;

import gash.router.server.ServerUtils;
import gash.router.server.db.DatabaseService;
import gash.router.server.db.GetResult;
import gash.router.server.db.Record;
import gash.router.util.Logger;
import global.Global;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import global.Global.Request;
import pipe.common.Common;

public class GlobalUtils {

	public static Global.GlobalMessage prepareClusterRouteRequest(int destination, Request old_req) {

		Global.GlobalMessage.Builder globalMessage = Global.GlobalMessage.newBuilder();

		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId( GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());

		header.setDestinationId(destination);

		globalMessage.setGlobalHeader(header);

		globalMessage.setRequest(old_req);

		return globalMessage.build();

	}
	
	public static List<GlobalMessage> ResponseBuilderForGET(int destination, Request request, List<Record> resList){

		return ResponseBuilderForSingleGET(destination, request, resList);
		
	}

private static List<GlobalMessage> ResponseBuilderForSingleGET(int destination, Request request, List<Record> getList) {
	List<GlobalMessage> gmList = new LinkedList<GlobalMessage>();
	Logger.DEBUG("in GlobalUtils.ResponseBuilderForSingleGet()");
	for(Record rec:getList){
		Logger.DEBUG("record is: "+ rec.toString());
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setDestinationId(destination);
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		
		Global.Response.Builder response = Global.Response.newBuilder();
		response.setRequestType(Global.RequestType.READ);
		response.setRequestId(request.getRequestId());
		response.setSuccess(true);
		Global.File.Builder file = Global.File.newBuilder();
		file.setChunkId(rec.getChunkId());
		file.setFilename(request.getFileName());
		file.setData(ByteString.copyFrom(rec.getData()));
		file.setTotalNoOfChunks(getList.size());
		response.setFile(file);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		
		gmList.add(globalmsg.build());
	}
	
	return gmList;
}
	public static List<GlobalMessage> ResponseBuilderForGETFail(Request request){
		List<GlobalMessage> gmList = new LinkedList<GlobalMessage>();
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		Global.Response.Builder response = Global.Response.newBuilder();
		response.setRequestId(request.getRequestId());
		response.setRequestType(Global.RequestType.READ);
		response.setSuccess(false);
		Common.Failure.Builder failure = Common.Failure.newBuilder();
		failure.setId(1);
		failure.setMessage("Can not find file: " + request.getFileName());
		response.setFailure(failure);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		
		gmList.add(globalmsg.build());
		return gmList;
	}
		
	public static GlobalMessage ResponseBuilderForPOST(int destination, Request request, String key){
		String s[] = key.split(":");
		String filename = s[0];
		int chunkId = Integer.parseInt(s[1]);
				
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		header.setDestinationId(destination);
		
		Global.Response.Builder response = Global.Response.newBuilder();
		
		response.setRequestType(Global.RequestType.WRITE);
		response.setRequestId(request.getRequestId());
		response.setSuccess(true);
		Global.File.Builder file = Global.File.newBuilder();
		file.setChunkId(chunkId);
		file.setFilename(filename);
		file.setTotalNoOfChunks(request.getFile().getTotalNoOfChunks());
		response.setFile(file);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		
		
		//globalmsg.setHeader(
		return globalmsg.build();
	}

	public static GlobalMessage prepareClusterRoutePing(int nodeId) {
		// TODO Auto-generated method stub
		Global.GlobalMessage.Builder globalMessage = Global.GlobalMessage.newBuilder();

		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId( GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());

		header.setDestinationId(nodeId);

		globalMessage.setGlobalHeader(header);

		globalMessage.setPing(true);

		return globalMessage.build();
	}

	public static GlobalMessage ResponseBuilderForPostFail(Request request) {
		// TODO Auto-generated method stub
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		Global.Response.Builder response = Global.Response.newBuilder();
		response.setRequestId(request.getRequestId());
		response.setRequestType(Global.RequestType.WRITE);
		response.setSuccess(false);
		Common.Failure.Builder failure = Common.Failure.newBuilder();
		failure.setId(1);
		failure.setMessage("Can not write file: " + request.getFileName() + "in any cluster");
		response.setFailure(failure);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		return globalmsg.build();
	}

	public static GlobalMessage ResponseBuilderForDELETEFail(Request request) {
		// TODO Auto-generated method stub
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		Global.Response.Builder response = Global.Response.newBuilder();
		response.setRequestId(request.getRequestId());
		response.setRequestType(Global.RequestType.DELETE);
		response.setSuccess(false);
		Common.Failure.Builder failure = Common.Failure.newBuilder();
		failure.setId(1);
		failure.setMessage("File: " + request.getFileName() + " does NOT EXIST in any cluster");
		response.setFailure(failure);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		return globalmsg.build();
	}

	public static GlobalMessage ResponseBuilderForDelete(int destination, Request request, String filename) {
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setDestinationId(destination);
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		
		Global.Response.Builder response = Global.Response.newBuilder();
		response.setRequestType(Global.RequestType.DELETE);
		response.setRequestId(request.getRequestId());
		response.setSuccess(true);
		response.setFileName(filename);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		return globalmsg.build();
	}

	public static GlobalMessage ResponseBuilderForUpdateFail(Request request) {
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		Global.Response.Builder response = Global.Response.newBuilder();
		response.setRequestId(request.getRequestId());
		response.setRequestType(Global.RequestType.UPDATE);
		response.setSuccess(false);
		Common.Failure.Builder failure = Common.Failure.newBuilder();
		failure.setId(1);
		failure.setMessage("File: " + request.getFileName() + " does NOT EXIST in any cluster");
		response.setFailure(failure);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		return globalmsg.build();
	}

	public static GlobalMessage ResponseBuilderForUpdate(int destination, Request request, String key) {
		// TODO Auto-generated method stub
		String s[] = key.split(":");
		String filename = s[0];
		int chunkId = Integer.parseInt(s[1]);
				
		Global.GlobalMessage.Builder globalmsg = Global.GlobalMessage.newBuilder();
		GlobalHeader.Builder header = GlobalHeader.newBuilder();
		header.setClusterId(GlobalServer.conf.getNodeId());
		header.setTime(ServerUtils.getCurrentUnixTimeStamp());
		header.setDestinationId(destination);
		
		Global.Response.Builder response = Global.Response.newBuilder();
		
		response.setRequestType(Global.RequestType.UPDATE);
		response.setRequestId(request.getRequestId());
		response.setSuccess(true);
		Global.File.Builder file = Global.File.newBuilder();
		file.setChunkId(chunkId);
		file.setFilename(filename);
		file.setTotalNoOfChunks(request.getFile().getTotalNoOfChunks());
		response.setFile(file);
		
		globalmsg.setGlobalHeader(header);
		globalmsg.setResponse(response);
		
		
		//globalmsg.setHeader(
		return globalmsg.build();
	}

}
