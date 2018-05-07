import socket

class TCPClient:
    def __init__(self):
        self.socket=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
           ## usage ## socket.connect(('ip',port)) 
#        self.socket.connect(('192.168.219.104',35358))
        self.socket.connect(('192.168.219.103',35358))

    def sendAll(self,message):
        print("sent to admin >> ", message.decode())
        self.socket.send(message)