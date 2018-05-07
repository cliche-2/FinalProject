import time
import threading

class StoppableThread(threading.Thread):
    
    def __init__(self):
        print(" base init")
        super(StoppableThread, self).__init__()
        self._stopper = threading.Event()

    def stopit(self):
        print( " base stop()")
        self._stopper.set()

    def stopped(self):
        return self._stopper.is_set()



class testTimer(StoppableThread):

    import time

    def __init__(self, tt):
        StoppableThread.__init__(self)
        self.tt =tt
        print(" thread init")

    def run(self):
        print( " thread running" )
        ct = 0
        
        while not self.stopped():
            time.sleep(1)
            ct += 1
            if(ct == self.tt):
                print(" timer finished ")
                break
        print( "thread ending")



class overTimer(StoppableThread):

    import time

    def __init__(self):
        StoppableThread.__init__(self)
        self.cnt = 0
        print( "thread init")

    def run(self):
        print( "thread running")
        
        while not self.stopped():
            time.sleep(1)
            self.cnt += 1
        print( "thread ending")

    def overTime(self):
        return self.cnt

