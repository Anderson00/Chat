import socketserver
import threading
import json
import sys
import uuid
from builtins import str


HOST = "localhost"
PORT = 8080

STATUS = [
            0, #INITIATED
            1, #PAUSED
            2, #RUNNING
            3, #FINISHED
            4, #ERROR
         ]


class SalaThread(object):
    def __init__(self, salaName):
        self.salaName = salaName
        self.status = STATUS[0]
        self.threadUsuarios = dict({})
        
    def addUsuarios(self, client, name):
        id = uuid.uuid3(uuid.NAMESPACE_DNS, name)
        if(id in self.threadUsuarios):
            return False
        user = UserThread(client, self, name, id)
        self.threadUsuarios[id] = user
        def call(stat, id):
            if(self.stat == STATUS[3]): 
                self.threadUsuarios.pop(id)

        user.registerStatusCallBack(call)
        threading.Thread(target=user.run).start()


        return True  

    def getSalaName(self):
        return self.salaName

    def getSalaStatus(self):
        return self.status
    
    def numbersOfUsers(self):
        return len(self.threadUsuarios)

    def pause(self):
        if(self.status != STATUS[0]):
            self.status = STATUS[1]
    
    def run(self):
        self.status = STATUS[2]
        print("SalaThread: {} running".format(self.salaName))

        while True:
            if(self.status == STATUS[3]):
                self.killUsersThread()
                break
            if(self.status == STATUS[1]):
                self.pauseUsersThread()
                continue

    def pauseUsersThread(self):
        for id, userthr in self.threadUsuarios:
            userthr.stopUser()

    def killUsersThread(self):
        for id, userthr in self.threadUsuarios:
            userthr.killUser()

    def messageAll(self, id, msg):
        for uid, userthr in self.threadUsuarios:
            if(uid == id):
                userthr.sendMsg(msg)


class UserThread(object):
    def __init__(self, client, parent, name, id):
        self.client = client
        self.parent = parent
        self.name = name
        self.id = id
        self.status = STATUS[0]
        self.callback = None
        self.callStatusCallback()
        
    def stopUser(self):  
        self.status = STATUS[1]
        
    def killUser(self):
        self.status = STATUS[3]
        
    def getSocket(self):
        self.client
        
    def sendMsg(self, msg):
        pass
    
    def registerStatusCallBack(self, callback):
        self.callback = callback
        
    def callStatusCallback(self):
        if(self.callback != None):
            self.callback(self.status, self.id)
            
    def run(self):
        self.status = STATUS[2] #RUNNING
        while True:
            if(self.status == STATUS[3]): break
            if(self.status == STATUS[1]):
                continue
            
            
            buff = ""         # Some decent size, to avoid mid-run expansion
            while True:
                data = self.client.recv()                  # Pull what it can
                buff += data                    # Append that segment to the buffer
                if '\n' in data: break              # If that segment had '\n', break
            
            print(buff)

            obj = json.loads(buff)
            obj["name"] = self.name

            self.parent.messageAll(self.id, obj.encode("utf-8"))
            
    
        

class ThreadedTCPRequestHandler(socketserver.StreamRequestHandler):

    def handle(self):
        try:
            msg = self.rfile.readline().decode("utf-8")
            obj = json.loads(msg) #json object
            error = ""
            if("add" in obj and "name" in obj):
                pass
            elif("salaName" in obj and "name" in obj):
                pass
            elif("list" in obj):
                x = {"rooms":[{"room":"teste", "n":0}]}
                msg = json.dumps(x).encode("utf-8")
                self.wfile.write(msg)
            else:
                pass
            
            if(len(error) != 0):
                obj["error"] = error
                self.wfile.write(obj.encode("utf-8"))
                self.wfile.flush()
                return
            else:
                obj["success"] = 1
                self.wfile.write(obj.encode("utf-8"))
                self.wfile.flush()
            
        except error:
            print(error)  
        

class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    pass
        
server = ThreadedTCPServer((HOST, PORT), ThreadedTCPRequestHandler)
threading.Thread(target=server.serve_forever).start()


userthr = UserThread(1,2, "qwe", 55)
userthr.registerStatusCallBack(lambda x,y: print("qweqweqweqwe"))
userthr.callStatusCallback()

id = uuid.uuid3(uuid.NAMESPACE_DNS, "teste")
id2 = uuid.uuid3(uuid.NAMESPACE_DNS, "teste2")

print(id == id2)