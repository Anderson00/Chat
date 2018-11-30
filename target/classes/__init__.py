import socketserver
import socket
import threading
import json
import sys
import uuid
from builtins import str
from uuid import UUID
#from pylint.lint import multiprocessing


HOST = "localhost"
PORT = 8080
MSGLEN = 8192

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
    salaThr.start()
    return True

def getSala(id):
    if(id in salas):
        return salas[id]
    return None

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


class MySocket:
    """demonstration class only
      - coded for clarity, not efficiency
    """

    def __init__(self, sock):
            self.sock = sock

    def sendReply(self, msg):
        sent = self.sock.send(msg)
        if sent == 0:
            raise RuntimeError("socket connection broken")

    def getRequest(self):
        chunks = []
        msg = ""
        while not(("}\r\n" in msg) or "}" in msg):
            rec = self.sock.recv(1024)
            if(rec == 0):
                print("Error")
                raise RuntimeError("socket connection broken")
            msg += rec.decode("utf-8")
            print("received: "+msg)   
        print("received: "+msg)            
        return msg.encode("utf-8")

class SalaThread(threading.Thread):
    def __init__(self, salaName):
        threading.Thread.__init__(self)
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
        user = UserThread(MySocket(client), self, name, id)
        self.threadUsuarios[id] = user
        def call(stat, id):
            if(stat == STATUS[3]): 
                print("retirado")
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
            if(uid != id):
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
        
    def stopUser(self):  
        self.status = STATUS[1]
        self.callStatusCallback()
        
    def killUser(self):
        self.status = STATUS[3]
        self.callStatusCallback()
        
    def getSocket(self):
        return self.client.sock
        
    def sendMsg(self, msg):
        try:
            #self.client.wfile.write(msg)
            #self.client.send((msg+"\r\n").encode("utf-8"))
            self.client.sendReply((msg+"\r\n").encode("utf-8"))
            print(self.name + " msg enviada")
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
        try:
            while True:
                if(self.status == STATUS[3]): break
                if(self.status == STATUS[1]):
                    continue
                #print("<><><><><> "+type(self.client.rfile))
                print("esperando msg do usuario")
                
                buff = ""
                print(type(self.client))
                #buff = self.client.rfile.readline().decode("utf-8")
                try:
                    #buff = self.client.recv(4096).decode("utf-8")
                    buff = self.client.getRequest().decode("utf-8")
                except BaseException as error:
                    print(error)
                    print(self.client)
                    self.killUser()
                    break
    
                print("["+self.name+"]:"+buff) 
                if(buff != ""):
                    obj = json.loads(buff)
                    obj["user"] = self.name
                    
                self.parent.messageAll(self.id, json.dumps(obj))
        except BaseException as error:
            print(error)
        self.client.sock.close()

        
SalaThread("teste") #Sala para realizar testes
print(getSalasList());

def ServerThread(sock):
    msg = sock.recv(2048).decode("utf-8")
    obj = json.loads(msg) #json object
    error = ""
    if("add" in obj and "name" in obj):
        st = SalaThread(obj["add"])
        if(st.getSalaStatus() == STATUS[4]):
            error = "Sala já existe"
        else:
            st.addUsuarios(sock, obj["name"])
    elif("salaName" in obj and "name" in obj):
        if(not entrarNaSala(sock, obj)):
            error = "Sala não existe"
        else:
            getSala(uuid.uuid3(uuid.NAMESPACE_DNS, obj["salaName"]))
    elif("list" in obj):     
        sock.send(getSalasList().encode("utf-8"))
    else:
        pass

    if(len(error) != 0):
        obj["error"] = error
        sock.send((json.dumps(obj)+"\r\n").encode("utf-8"))
        return
    else:
        obj["success"] = 1
        sock.send((json.dumps(obj)+"\r\n").encode("utf-8"))


s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
s.bind((HOST, PORT))

s.listen()

while True:
    conn, addr = s.accept()
    print("{} conectou".format(addr))
    threading.Thread(target=ServerThread, args=[conn]).start()