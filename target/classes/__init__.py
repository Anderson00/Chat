import socketserver
import threading
import json
import sys
import uuid
from builtins import str
from uuid import UUID
from pylint.lint import multiprocessing


HOST = "localhost"
PORT = 8080

STATUS = [
            0, #INITIATED
            1, #PAUSED
            2, #RUNNING
            3, #FINISHED
            4, #ERROR
         ]

## Anteriormente Singleton

salas = dict()

def adicionarSala(id, salaThr):
    if(id in salas): 
        return False    
    salas[id] = salaThr
    return True

def entrarNaSala(client, jsobj):
    if("salaName" in jsobj and "name" in jsobj):
        id = uuid.uuid3(uuid.NAMESPACE_DNS, jsobj["salaName"])
        if(id in salas):
            salas[id].addUsuarios(client, jsobj["name"])
            return True
    return False
        

def getSalasList():
    aux = {"rooms":[]}    
    for id in salas:
        salathr = salas[id]
        print(salathr.getSalaName())
        aux["rooms"].append({"room": salathr.getSalaName(), "n": salathr.numbersOfUsers()})    
    
    return json.dumps(aux) #retorna JSON object

## Anteriormente Singleton


class SalaThread(object):
    def __init__(self, salaName):
        self.salaName = salaName
        self.status = STATUS[0]
        self.id = uuid.uuid3(uuid.NAMESPACE_DNS, salaName)
        print(self.id)
        if(not adicionarSala(self.id, self)):
            self.status = STATUS[4]
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
        user.start()

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
        for id in self.threadUsuarios:
            self.threadUsuarios[id].stopUser()

    def killUsersThread(self):
        for id in self.threadUsuarios:
            self.threadUsuarios[id].killUser()

    def messageAll(self, id, msg):
        for uid in self.threadUsuarios:
            if(uid == id):
                self.threadUsuarios[uid].sendMsg(msg)


class UserThread(threading.Thread):
    def __init__(self, client, parent, name, id):
        threading.Thread.__init__(self)
        self.client = client
        self.parent = parent
        self.name = name
        self.id = id
        self.status = STATUS[0]
        self.callback = None 
        self.callStatusCallback()
        self.daemon = True
        
    def stopUser(self):  
        self.status = STATUS[1]
        
    def killUser(self):
        self.status = STATUS[3]
        
    def getSocket(self):
        self.client
        
    def sendMsg(self, msg):
        try:
            self.client.wfile.write(msg)
            print(self.name + " msg enviada");
            return True
        except BaseException as error:
            pass
        return False
    
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
            print("<><><><><>")
            
            buff = ""
            buff = self.client.rfile.readline().decode("utf-8")
            print("["+self.name+"]:"+buff)
            
            obj = json.loads(buff)
            obj["user"] = self.name

            self.parent.messageAll(self.id, obj.encode("utf-8"))


class ThreadedTCPRequestHandler(socketserver.StreamRequestHandler):

    def handle(self):
        #try:
            msg = self.rfile.readline().decode("utf-8")
            obj = json.loads(msg) #json object
            error = ""
            if("add" in obj and "name" in obj):
                st = SalaThread(obj["add"]);
                if(st.getSalaStatus() == STATUS[4]):
                    error = "Sala já existe"
                else:
                    st.addUsuarios(self, obj["name"])
                    threading.Thread(target=[st.run], args=[st]).start()
            elif("salaName" in obj and "name" in obj):
                if(not entrarNaSala(self, obj)):
                    error = "Sala não existe"
            elif("list" in obj):     
                self.wfile.write(getSalasList().encode("utf-8"))
            else:
                pass

            if(len(error) != 0):
                obj["error"] = error
                self.wfile.write(json.dumps(obj).encode("utf-8"))
                self.wfile.flush()
                return
            else:
                obj["success"] = 1
                self.wfile.write(json.dumps(obj).encode("utf-8"))
                self.wfile.flush()            
            
        #except BaseException as error:
        #    print(error)  
        

class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    pass
        
SalaThread("teste") #Sala para realizar testes
print(getSalasList());
server = ThreadedTCPServer((HOST, PORT), ThreadedTCPRequestHandler)
threading.Thread(target=server.serve_forever).start()




