import socket
import sys
import time
#from thread import * 
import thread
import RPi.GPIO as GPIO
HOST = '' #meaning all available interface
PORT = 8888 #arbitrary non-priviledged port

flag_record=False

#set pin/key definition| high for yes, low for no
record = 29 #Board pin, BCM GPIO 5
GPIO.setmode(GPIO.BOARD)
GPIO.setup(record, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)




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
	#confirm the connection-- refer to the document 2017/10/27
	conn.send('C')
	
	while 1 :
		#test if recording
		if flag_record:
			conn.send('R') #let's record
			while flag_record:
				pass
			conn.send('T') #srop record
		'''
		#recive from client 
		data = conn.recv(1024)
		reply = 'OK...'+ data
		if not data:
			break
		conn.sendall(reply)
		'''
	
	conn.close()
	
conn, addr = s.accept()

print 'Connect with ' + addr[0] + ':' +str(addr[1])
thread.start_new_thread(clientthread, (conn,))

while 1:
	flag_record=	GPIO.input(record)

s.close()
