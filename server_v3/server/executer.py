from random import *
import time

class Executer:
    def __init__(self, tcpServer):
        self.andRaspTCP = tcpServer
        self.tm = 0;
        self.al = 0;
        self.money = 0;
        self.pw = 0;
        self.chkPW = False;
 
    def startCommand(self, command):

########## setting information
        if command[0] == "S":
            if command == "S1\n":
                self.tm = 10;
                self.money = 500;
                print '10sec 500 saved.'

            elif command == "S2\n":
                self.tm = 60;
                self.money = 5000;
                print '1min 5000 saved.'

            elif command == "S3\n":
                self.tm = 120;
                self.money = 7000;
                print '2min 7000 saved.'

########## setting alarm
        elif command[0] == "T":
            self.al = command[1:]
            print ' alarm saved'

########## checking password
        elif command[0] == "W":
            tmp=int(command[1:])
            print tmp
            if(self.pw == tmp):
                print 'password matched'
                self.chkPW = True;

########## making password
        else:
            # make rand pw
            self.pw = randint(0,100)
            print('pw {} saved'.format(self.pw))

            # bitwise pw X andRand
            andRand = command
            msg = str(self.pw^int(andRand))
            msg = "P" + msg + "\n"
         
            print(msg)
            # send
            time.sleep(3)
            self.andRaspTCP.sendAll(msg)

