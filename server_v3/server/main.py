import Queue
import time
import threading

import tcpServer
import tcpClient
import executer
import nfctest
import ttimer


                    ## TCP with admin ##
        ### S    start
        ### A    returned
        ### W   intrusion
        ### O    overcharged

adminTCP = tcpClient.TCPClient()
print 'TCPclient socket created'



                    ## UID registr ##

nfc = nfctest.NFCtest()
nfc.NFC_UID()
print 'uid saved'



                    ## TCP communicate with User ##
        ### a    pre-alarm
        ### A    Alarm
        ### Q    intrusion

# make public queue
commandQueue = Queue.Queue() 
# init TCP  module
andRaspTCP = tcpServer.TCPServer(commandQueue, "", 35357)
andRaspTCP.start()
# set module to executer
commandExecuter = executer.Executer(andRaspTCP)

                    ## chk Key and get Info ##
while True:
    command = commandQueue.get()
    commandExecuter.startCommand(command)
    if commandExecuter.al != 0:
        break

money = commandExecuter.money
#test
print money




                    ## Chk Money input and Lock ##
    # function call




                    ## Timers start ##
rTime = int(commandExecuter.tm)
aTime = rTime - int(commandExecuter.al)

mainTimer = ttimer.testTimer(rTime)
alarmTimer = ttimer.testTimer(aTime)
mainTimer.start()
alarmTimer.start()

# to admin
adminTCP.sendAll("S1\n")


ck = True
ck2 = True
ck3 = True
                    ## chk NFC ##
#while True:
nfc.start()
while True:
    if nfc.result==0:
        # send Alarm
        if (alarmTimer.isAlive()==False and ck):
            andRaspTCP.sendAll("a\n")
            ck = False
        # mainTimer finished
        if (mainTimer.isAlive()==False and ck2):
            andRaspTCP.sendAll("A\n")
            ck2 = False
            addTimer = ttimer.overTimer()
            addTimer.start()
    # NFC matched!
    elif nfc.result==1:
        break
    # intrusion!
    elif nfc.result==2:
        andRaspTCP.sendAll("Q\n")
        adminTCP.sendAll("W1\n")
        nfc.result = 0
    if (ck2==False and addTimer.overTime()>10 and ck3):
        print 'overtime'
        adminTCP.sendAll("O1\n")
        ck3 = False



                    ## chk PW ##
while True:
    command = commandQueue.get()
    commandExecuter.startCommand(command)
    if commandExecuter.chkPW:
        break

if (mainTimer.isAlive()==True):
    mainTimer.stopit()
if (alarmTimer.isAlive()==True):
    alarmTimer.stopit()


                    ## if overTime ##
if ck2 == False:
    # stop threads
    addTimer.stopit()
    cnt = addTimer.overTime()
    won = int(cnt/10) * 300
                    ## overcharged!
    if won!=0:
        print('overtime: {}sec'.format(cnt))
        print('overcharge: {} won'.format(won))





                    ## Chk Money input and Lock ##
# function call






adminTCP.sendAll("A1\n")

print 'finished all'


