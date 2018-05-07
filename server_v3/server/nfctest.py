import binascii
import sys
import threading
import Adafruit_PN532 as PN532

class NFCtest(threading.Thread):

	# import nfc_test
	# instance = nfc_test.NFC_test()
	# instance.NFC_UID()

    def __init__(self):

        # Configuration for a Raspberry Pi
        CS   = 18
        MOSI = 23
        MISO = 24
        SCLK = 25

        threading.Thread.__init__(self)
        self.Count = 0
        self.User_uid = 0
        self.result=0

    # Create an instance of the PN532 class.
        self.pn532 = PN532.PN532(cs=CS, sclk=SCLK, mosi=MOSI, miso=MISO)
    # Call begin to initialize communication with the PN532.  Must be done before
    # any other calls to the PN532!
        self.pn532.begin()
    # Configure PN532 to communicate with MiFare cards.
        self.pn532.SAM_configuration()
    # Main loop to detect cards and read a block.
        print('Waiting for User UID...')



    def NFC_UID(self):
        while True:
            # Check if a card is available to read.
            uid = self.pn532.read_passive_target()
            # Try again if no card is available.
            if uid is None:
                continue

            self.User_uid = uid
            print('Found card with UID: 0x{0}'.format(binascii.hexlify(uid)))
            return uid
	
	
	
    def run(self):
        print( 'Wating for User UID... ...')
        while True:
                # Check if a card is available to read.
            uid = self.pn532.read_passive_target()
                # Try again if no card is available.
            if uid is None:
                continue
            print('Found card with UID: 0x{0}'.format(binascii.hexlify(uid)))
		
            if uid == self.User_uid:
                print('It matches the User_UID')
                self.result=1
                break
				
            if uid != self.User_uid:
                self.Count += 1
                print('Wrong Approaches : %d' % (self.Count))
		
            if self.Count >= 3:
                print('There are three incorrect approaches')
                self.Count = 0
                self.result=2
                break

