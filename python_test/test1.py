import socket
import sys
from thread import * 
HOST = '' #meaning all available interface
PORT = 8988 #arbitrary non-priviledged port

s=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print 'Socket Created'

#Bind socket to local host and port 
try:
	s.bind((HOST, PORT))
except socket.error as msg:
		print 'Bind failed. Error code: '+str(msg[0]) + 'Message '+msg[1]
		sys.exit
		
print 'Socket bind complete'

#start listening on socket
s.listen(10)
print  'Socket now listening'

#handling function
def clientthread(conn):
	conn.send('welcome to the server. Type something and hit enter\n')
	
	while 1 :
		
		#recive from client 
		data = conn.recv(1024)
		reply = 'OK...'+ data
		if not data:
			break
		conn.sendall(reply)
	
	conn.close()

#now keep talking with the client 
while 1:
	#wait to accept a connection - blocking call
	conn, addr = s.accept()
	print 'Connect with ' + addr[0] + ':' +str(addr[1])
	
	start_new_thread(clientthread, (conn,))
	
s.close()
