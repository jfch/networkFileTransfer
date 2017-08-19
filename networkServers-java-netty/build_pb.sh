#!/bin/bash
#
# build the protobuf classes from the .proto. Note tested with 
# protobuf 2.4.1. Current version is 2.5.0.
#
# Building: 
# 
# Running this script is only needed when the protobuf structures 
# have change.
#

project_base="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# which protoc that you built (if not in your path)
#PROTOC_HOME=/usr/local/protobuf-2.5.0/

if [ -d ${project_base}/generated ]; then
  echo "removing contents of ${project_base}/generated"
  rm -r ${project_base}/generated/*
else
  echo "creating directory ${project_base}/generated"
  mkdir ${project_base}/generated
fi


/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/common.proto
/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/Ping.proto
/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/pipe.proto
/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/AppendEntriesRPC.proto

/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/HeartBeatRPC.proto

/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/VoteRPC.proto

/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/work.proto

/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/imageTransfer.proto
/usr/local/bin/protoc --proto_path=${project_base}/resources --java_out=${project_base}/generated ${project_base}/resources/Global.proto