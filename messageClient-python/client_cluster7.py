import src.globalmessage_pb2
import src.global_pb2
import src.work_pb2 #trivial ping
import socket               
import time
import random
import struct

def buildGlobalMessage():
    uuID = random.randint(300, 399)
    while True:    
        input = raw_input("Welcome! Kindly select the type of global message: " \
                                "\n1.READ file from server\n2.POST file to server" \
                                "\n3.UPDATE file on server\n4.DELETE a file on Server" \
                                "\n5.Ping with Bool Message to server\n6.Ping with String Message to Server\n")
        if input == "5":    
            globalMess = src.global_pb2.GlobalMessage()
            globalMess.globalHeader.cluster_id = 0  # Own Cluster Id (Team no.3)
            globalMess.globalHeader.time = long(time.time())
            #globalMess.globalHeader.destination_id = 3 # ClusterId who has got Client Request
            globalMess.ping = True

            # Send Ping: Prepare
            pRequest = globalMess.SerializeToString()    
            packed_len = struct.pack('>L', len(pRequest))
            # Send the message   
            # Sending Ping Request to the server's public port
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((host,port))
            # Prepending the length field and sending
            s.sendall(packed_len + pRequest)
            #s.close()
            print("global message(ping message) sent successfully!!");

        if input == "6":    
            globalMess = src.global_pb2.GlobalMessage()
            globalMess.globalHeader.cluster_id = 0  # Own Cluster Id (Team no.3)
            globalMess.globalHeader.time = long(time.time())
            #globalMess.globalHeader.destination_id = 3 # ClusterId who has got Client Request
            globalMess.message = "Hello, this is python client sent String type Global Message from Team/Cluster 3"

            # Send Ping: Prepare
            pRequest = globalMess.SerializeToString()    
            packed_len = struct.pack('>L', len(pRequest))
            # Send the message   
            # Sending Ping Request to the server's public port
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((host,port))
            # Prepending the length field and sending
            s.sendall(packed_len + pRequest)
            #s.close()
            print("global message(string message) sent successfully!!");

        if input == "1":    #1.Read file from server
            globalMess = src.global_pb2.GlobalMessage()
            globalMess.globalHeader.cluster_id = 0  # Own Cluster Id (Team no.3)
            globalMess.globalHeader.time = long(time.time())
            #globalMess.globalHeader.destination_id = 3 # ClusterId who has got Client Request
            
            # build global message body
            globalMess.request.requestId = str(uuID)
            uuID = uuID + 1
            #globalMess.request.requestType = src.global_pb2.RequestType.WRITE
            #globalMess.request.requestType = src.global_pb2.WRITE
            globalMess.request.requestType = src.global_pb2.READ
            print("Please input the file name you want to read: using the default filename is image1.png")
            inputFileName = raw_input("Please input the file name you want to read: \n")
            globalMess.request.fileName = inputFileName
            print("input file name:" + inputFileName)
            #globalMess.request.fileName = "image1.png"
            
            # Send Ping: Prepare
            pRequest = globalMess.SerializeToString()    
            packed_len = struct.pack('>L', len(pRequest))
            #print("packed_len:"+packed_len);
            # Send the message   
            # Sending Ping Request to the server's public port
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            #host = socket.gethostname() # Testing on own computer
            #port = 5570 # Public Port
            s.connect((host,port))
            # Prepending the length field and sending
            s.sendall(packed_len + pRequest)
            #s.close()
            print("global message(read a file from server) sent successfully!!");

            # Receive at s, a message of type Request and close connection
            iCount = 0
            printSwitch = raw_input("Print the response: 1. only Brief info with chunkid; 2. the WHOLE message \n")
            if printSwitch == "1":
                while 1:                
                    print_str= get_message(s, src.global_pb2.GlobalMessage);
                    #print "\n---FOLLOWING is the RECIVED MESSAGE---\n",print_str
                    if print_str.response.file.filename is not None \
                        and print_str.response.file.chunkId is not None \
                        and print_str.response.file.totalNoOfChunks is not None :
                        print "GET Response Filename: " + str(print_str.response.file.filename) \
                            + "; ChunkId: " + str(print_str.response.file.chunkId) \
                            + "; Total No. Chunk: " + str(print_str.response.file.totalNoOfChunks);
                        

                    #save the response to local disk file  
                    if iCount == 0:                    
                        fdisk = open(str(print_str.response.file.filename), "wb")                      
                    fdisk.write(print_str.response.file.data);

                    iCount = iCount +1
                    if iCount == print_str.response.file.totalNoOfChunks:
                        fdisk.close()
                        break

            if printSwitch == "2":
                while 1:                
                    print_str= get_message(s, src.global_pb2.GlobalMessage);
                    #print "\n---FOLLOWING is the RECIVED MESSAGE---\n",print_str                    
                    print print_str

                    #save the response to local disk file  
                    if iCount == 0:                    
                        fdisk = open(str(print_str.response.file.filename), "wb")                      
                    fdisk.write(print_str.response.file.data);

                    iCount = iCount + 1
                    if iCount == print_str.response.file.totalNoOfChunks:
                        fdisk.close()
                        break
                    
                # ifsrc.global_pb2.GlobalMessage.response. type() =
                # if( src.global_pb2.GlobalMessage.response.file.totalNoOfChunks)

            #s.close()              

        if input == "2":    #2.Write(POST) file to server   
            
            # fileNameToWrite = raw_input("Please input the file name you want to read: the default filename is image1.png")
            # f = open(fileNameToWrite, 'rb')
            # globalMess.request.file.filename = fileNameToWrite
            fileNameToWrite = "textfile1.txt"            
            # fileNameToWrite = "image1.png"     
            fileSwitch = raw_input("Please input the file name you want to write to server: \n1.team7.txt[2k]\n2.textfile2.txt[3.12M]\n" \
                                                                                            "3.image1.png[22k]\n");
            if fileSwitch == "1":
                fileNameToWrite = "team7.3.txt"
            if fileSwitch == "2":
                fileNameToWrite = "textfile2.txt"
            if fileSwitch == "3":
                fileNameToWrite = "image1.png"
            f = open(fileNameToWrite, 'rb')
            # READ and CHUNK the file(chunk size is), and then SEND it with the message file to server
            counterOfChunks = 0
            # chunk size 1 M (1024,1024); 
            counterOfChunks = 0 
            while True:
                piece = f.read(1024*1024)
                if not piece:
                    break
                counterOfChunks = counterOfChunks + 1
            f.close()
            print("counterOfChunks: ")
            print(counterOfChunks)

            # Every CHUNK will be a wrapped into an SINGLE Global Message
            localChunkId = 0            
            f = open(fileNameToWrite, 'rb')
            while True:
                piece = f.read(1024*1024)
                if not piece:
                    break
                # build Global Message - Header
                globalMess = src.global_pb2.GlobalMessage()
                globalMess.globalHeader.cluster_id = 0  # Own Cluster Id (Team no.3)
                globalMess.globalHeader.time = long(time.time())
                #globalMess.globalHeader.destination_id = 3 # ClusterId who has got Client Request
                # build Global Message - Body
                globalMess.request.requestId = str(uuID)
                uuID = uuID + 1         
                #globalMess.request.requestType = src.global_pb2.RequestType.READ
                #globalMess.request.requestType = src.global_pb2.READ
                globalMess.request.requestType = src.global_pb2.WRITE

                # build Global Message - Body - FILE
                globalMess.request.file.chunkId = localChunkId
                localChunkId = localChunkId + 1
                globalMess.request.file.filename = fileNameToWrite
                globalMess.request.file.totalNoOfChunks = counterOfChunks
                globalMess.request.file.data = piece

                # Send Ping: Prepare
                pRequest = globalMess.SerializeToString()    
                packed_len = struct.pack('>L', len(pRequest))
                #print("packed_len:"+packed_len);
                # Send the message   
                # Sending Ping Request to the server's public port
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                #host = socket.gethostname() # Testing on own computer
                #port = 5570 # Public Port
                s.connect((host,port))
                # Prepending the length field and sending
                s.sendall(packed_len + pRequest)
                #s.close()
                print("global message(upload/write a file to server) sent successfully!!");
                print("---FOLLOWING is the Constructed GLOBAL MESSAGE.---");
                print(globalMess);
            f.close()

            # message File { 
            #     optional int32 chunkId = 1;
            #     optional bytes data = 2;
            #     required string filename = 3; 
            #     optional int32 totalNoOfChunks = 5; // total number of chunks of a requested file
            # }
            # message Request {
            #     required string requestId = 4; // UUID which maps to a client request
            #     required RequestType requestType = 1; //READ for now
            #     oneof payload {
            #         string fileName = 2 ; // Will be Sent when requestType is READ/DELETE
            #         File file = 3; // Will be Sent when requestType is WRITE/UPDATE
            #     } 
            # }

        if input == "3":    #2.Write(UPDATE) file to server   
            
            # fileNameToWrite = raw_input("Please input the file name you want to read: the default filename is image1.png")
            # f = open(fileNameToWrite, 'rb')
            # globalMess.request.file.filename = fileNameToWrite
            fileNameToUpdate = "textfile1.txt"            
            # fileNameToWrite = "image1.png"     
            fileSwitch = raw_input("Please input the file name you want to UPDATE to server: \n1.textfile1.txt[2k]\n2.textfile2.txt[3.12M]\n");
            if fileSwitch == "1":
                fileNameToUpdate = "textfile1.txt"
            if fileSwitch == "2":
                fileNameToUpdate = "textfile2.txt"
            f = open(fileNameToUpdate, 'rb')
            # READ and CHUNK the file(chunk size is), and then SEND it with the message file to server
            counterOfChunks = 0
            # chunk size 1 M (1024,1024); 
            counterOfChunks = 0 
            while True:
                piece = f.read(1024*1024)
                if not piece:
                    break
                counterOfChunks = counterOfChunks + 1
            f.close()
            print("counterOfChunks: ")
            print(counterOfChunks)

            # Every CHUNK will be a wrapped into an SINGLE Global Message
            localChunkId = 1            
            f = open(fileNameToUpdate, 'rb')
            while True:
                piece = f.read(1024*1024)
                if not piece:
                    break
                # build Global Message - Header
                globalMess = src.global_pb2.GlobalMessage()
                globalMess.globalHeader.cluster_id = 0  # Own Cluster Id (Team no.3)
                globalMess.globalHeader.time = long(time.time())
                #globalMess.globalHeader.destination_id = 3 # ClusterId who has got Client Request
                # build Global Message - Body
                globalMess.request.requestId = str(uuID)
                uuID = uuID + 1         
                #globalMess.request.requestType = src.global_pb2.RequestType.READ
                #globalMess.request.requestType = src.global_pb2.READ
                globalMess.request.requestType = src.global_pb2.UPDATE

                # build Global Message - Body - FILE
                globalMess.request.file.chunkId = localChunkId
                localChunkId = localChunkId + 1
                globalMess.request.file.filename = fileNameToUpdate
                globalMess.request.file.totalNoOfChunks = counterOfChunks
                globalMess.request.file.data = piece

                # Send Ping: Prepare
                pRequest = globalMess.SerializeToString()    
                packed_len = struct.pack('>L', len(pRequest))
                #print("packed_len:"+packed_len);
                # Send the message   
                # Sending Ping Request to the server's public port
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                #host = socket.gethostname() # Testing on own computer
                #port = 5570 # Public Port
                s.connect((host,port))
                # Prepending the length field and sending
                s.sendall(packed_len + pRequest)
                #s.close()
                print("global message(upload/UPDATE a file to server) sent successfully!!");
                print("---FOLLOWING is the Constructed GLOBAL MESSAGE.---");
                print(globalMess);
            f.close()

        if input == "4":    #1.Write(DELETE) file on server
            globalMess = src.global_pb2.GlobalMessage()
            globalMess.globalHeader.cluster_id = 0  # Own Cluster Id (Team no.3)
            globalMess.globalHeader.time = long(time.time())
            #globalMess.globalHeader.destination_id = 3 # ClusterId who has got Client Request
            
            # build global message body
            globalMess.request.requestId = str(uuID)
            uuID = uuID + 1
            #globalMess.request.requestType = src.global_pb2.RequestType.WRITE
            #globalMess.request.requestType = src.global_pb2.WRITE
            globalMess.request.requestType = src.global_pb2.DELETE
            #print("Please input the file name you want to delete: the default filename is image1.png")
            toDeleteFileName = raw_input("Please input the file name you want to delete: \n")
            globalMess.request.fileName = toDeleteFileName
            print("deleting file name:" + toDeleteFileName)
            #globalMess.request.fileName = "image1.png"
            
            # Send Ping: Prepare
            pRequest = globalMess.SerializeToString()    
            packed_len = struct.pack('>L', len(pRequest))
            #print("packed_len:"+packed_len);
            # Send the message   
            # Sending Ping Request to the server's public port
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            #host = socket.gethostname() # Testing on own computer
            #port = 5570 # Public Port
            s.connect((host,port))
            # Prepending the length field and sending
            s.sendall(packed_len + pRequest)
            #s.close()
            print("global message(delete a file from server) sent successfully!!");

            # Receive at s, a message of type Request and close connection
            print_str= get_message(s, src.global_pb2.GlobalMessage);
            print "\n---FOLLOWING is the RECIVED MESSAGE---\n",print_str
            s.close()    

        if input != "2" and input != "3":   
            # print the global message before sending it
            print("---FOLLOWING is the Constructed GLOBAL MESSAGE.---");
            print(globalMess);

        # todo: send the message and handle the response


        # finish and send another message
        sendSwitch = raw_input("Send Message again? : " \
                                "\n1.Send next message\n2.I am done\n")
        if sendSwitch == "2":    #2.Write file to server   
            break

    # Cassandra fields.
    # filename, chunkID, data, time
    return "See you next time!"
    #return globalMess


def buildPingMessage(clientIP,clentPort):

    ping = src.work_pb2.WorkMessage()
    ping.unixTimeStamp = 1000
    ping.trivialPing.nodeId = 0
    ping.trivialPing.IP = clientIP
    ping.trivialPing.port = clentPort

    # Send Ping: Prepare
    pRequest = ping.SerializeToString()    
    packed_len = struct.pack('>L', len(pRequest))
    #print("packed_len:"+packed_len);

    # Sending Ping Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() # Testing on own computer
    #port = 5570 # Public Port
    s.connect((host,port))
    # Prepending the length field and sending
    s.sendall(packed_len + pRequest)
    print("ping sent successfully!!");

    # Receive at s, a message of type Request and close connection
    print_str= get_message(s, src.work_pb2.WorkMessage);
    print "\n---FOLLOWING is the RECIVED MESSAGE---\n",print_str
    s.close()
    
    print("---FOLLOWING is the Constructed GLOBAL MESSAGE.---");
    return ping

def buildMessage(name,email):
    person = src.globalmessage_pb2.Person()
    person.id = 1234
    person.name = name
    person.email = email
    phone = person.phone.add()
    phone.number = "555-4321"
    phone.type = src.globalmessage_pb2.Person.HOME

    # msg = person.SerializeToString()
    # return msg

    pRequest = person.SerializeToString()    
    packed_len = struct.pack('>L', len(pRequest))
    print("IP:"+packed_len);

    # Sending Ping Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #host = socket.gethostname() # Testing on own computer
    #port = 5570 # Public Port
    s.connect((host,port))
    # Prepending the length field and sending
    s.sendall(packed_len + pRequest)
    s.close()
    print("iam here!!"+phone.number);

    return person

#Read a receiving message from a socket. msgtype is a subclass of protobuf Message.       
def get_message(sock, msgtype):
    len_buf = socket_read_n(sock, 4)
    msg_len = struct.unpack('>L', len_buf)[0]
    msg_buf = socket_read_n(sock, msg_len)
   
    msg = msgtype()
    msg.ParseFromString(msg_buf)
    return msg

# """ Read exactly n bytes from the socket.
#         Raise RuntimeError if the connection closed before
#         n bytes were read.
#     """

def socket_read_n(sock, n):
    buf = ''
    while n > 0:
        data = sock.recv(n)
        #label JIONGFENG CHEN
        ##print("received:len(data) " + str(len(data)) + "\nn: " + str(n))
        if data == '':
            raise RuntimeError('unexpected connection close')
        buf += data
        n -= len(data)
    return buf

if __name__ == '__main__':
    # msg = buildPing(1, 2)
    # UDP_PORT = 8080
    # serverPort = getBroadcastMsg(UDP_PORT)     
   
    print("Host name: "+socket.gethostname());

    #host = raw_input("Host IP:")
    global host   
    #host = socket.gethostname() # Testing on own computer
    inputIP = raw_input("\nInput the host IP:\n1.Using default 169.254.203.72 \n2.Using 169.254.203.73"
                        "\n3.Using 169.254.203.51\n4.Input another IP\n")
    if inputIP == "1":
        host = "169.254.203.72"
    if inputIP == "2":    
        host = "169.254.203.73"
    if inputIP == "3":    
        host = "169.254.203.51"
    if inputIP == "4":    
        host = raw_input("HostIP: ")
    #host = "169.254.6.52"
    
    #port = 5000
    #jf@jf:/mnt/project_base4_without_chunk$ ./startGlobalServer.sh runtime/global.conf runtime/queue.conf
    #port = 4167
    #jf@jf:/mnt/project_base4$ ./startServer.sh runtime/route-1.conf runtime/queue.conf
    global port 
    #port = raw_input("Port:")
    port = 5000 # 
    #port = 5000 # project_base4_without_chunk
    #port = 4167 # project_base4

    port = int(port)
    whoAmI = 1;
    input = raw_input("\nWhat message you want to send to the server:\n1.TrivialPing Message (one of Work Message)\n2.Global Message\n")
    if input == "1":
        print(buildPingMessage("127.0.0.1",1000))   #sender's(python client) IP and Port
    if input == "2":       
        print(buildGlobalMessage())
    
#    listcourseReq = buildListCourse(name_space, comm_pb2.JobOperation.ADDJOB, ownerId)
#    sendMsg(listcourseReq, 5573)