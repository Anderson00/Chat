
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import model.Status;

public class SalaThread extends Thread{
	
	private Status salaStatus;
	private String salaName;
	private UUID id;
	
	private HashMap<UUID, UserThread> threadUsuarios;
	
	public SalaThread(String salaName) {
		salaStatus = Status.INITIATED;		
		this.salaName = salaName;
		id = UUID.nameUUIDFromBytes(salaName.getBytes());
		if(!ApplicationSingleton.getInstance().adicionarSala(id, this)) {
			salaStatus = Status.ERROR;
		}
		
		threadUsuarios = new HashMap<UUID, UserThread>();
	}
	
	public boolean addUsuarios(Socket user, String name) {
		UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
		UserThread usr = new UserThread(user, this, name, uuid);
		usr.registerStatusCallBack( (stat, id) ->{
			if(stat == Status.FINISHED) {
				threadUsuarios.remove(id);
			}
		});
		boolean stat = threadUsuarios.put(uuid, usr) == null;
		if(stat)
			usr.start();
		else
			try {
				user.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return stat;
	}
	
	public String getSalaName() {
		return salaName;
	}
	
	public Status getSalaStatus() {
		return salaStatus;
	}
	
	public int numberOfUsers() {
		return threadUsuarios.size();
	}
	
	public void pause() {
		if(salaStatus != Status.INITIATED)
			salaStatus = Status.PAUSED;
	}
	
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
		if(salaStatus != Status.INITIATED)
			salaStatus = Status.RUNNING;
	}
	
	
	private void startUsersThread() {
		threadUsuarios.forEach((uuid,userThread) ->{
			System.out.println(">> "+uuid.toString());
			userThread.start();
		});
	}
	
	private void pauseUsersThread() {
		threadUsuarios.forEach((uuid, userThread) -> {
			userThread.stopUser();
		});
	}
	
	private void killUsersThread() {
		threadUsuarios.forEach((uuid, userThread) -> {
			userThread.killUser();
		});
	}
	
	public void messageAll(UUID id, String msg) { // uuid identifica o usuario q enviou a msg
		threadUsuarios.forEach((uuid, userThread) -> {
			if(uuid.compareTo(id) != 0) {
				userThread.sendMsg(msg);
			}			
		});
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		salaStatus = Status.RUNNING;
		System.out.println("SalaThread Running");
		//startUsersThread();
		try {	
			while(true) {					
				if(salaStatus == Status.FINISHED) {
					killUsersThread();
					break;
				}
				if(salaStatus == Status.PAUSED) {
					pauseUsersThread();					
					Thread.sleep(100);					
					continue;
				}
				
				
				
			}			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
