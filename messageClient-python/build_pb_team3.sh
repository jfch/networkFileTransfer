#!/bin/bash
#
# creates the python classes for our .proto
#

project_base="/mnt/messageClient"

#work message
rm ${project_base}/src/globalmessage_pb2.py
rm ${project_base}/src/Ping_pb2.py
rm ${project_base}/src/AppendEntriesRPC_pb2.py
rm ${project_base}/src/HeartBeatRPC_pb2.py
rm ${project_base}/src/VoteRPC_pb2.py
rm ${project_base}/src/work_pb2.py

#global message
rm ${project_base}/src/common_pb2.py
rm ${project_base}/src/global_pb2.py

protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/globalmessage.proto 
protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/Ping.proto 
protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/AppendEntriesRPC.proto 
protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/HeartBeatRPC.proto 
protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/VoteRPC.proto 
protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/work.proto 

protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/common.proto 
protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/global.proto 

echo "protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/ping.proto "
echo "protoc -I=${project_base}/resources --python_out=${project_base}/src ${project_base}/resources/globalmessage.proto "
echo "work.proto built! "