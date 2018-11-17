
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

import org.json.JSONObject;

public class UserThread extends Thread{
	
	private Socket user;
	private String name;
	private UUID uuid;
	private Status status;
	private SalaThread parent;
	
	private OutputStream out;
	private InputStream in;
	
	public UserThread(Socket socket, SalaThread parent,String name, UUID uuid) {
		this.user = socket;
		this.name = name;
		this.uuid = uuid;		
		this.parent = parent;
		status = Status.INITIATED;
	}
	
	public void stopUser() {
		status = Status.PAUSED;
	}
	
	public void killUser() {
		status = Status.FINISHED;
	}
	
	public Socket getSocket() {
		return user;
	}
	
	public boolean sendMsg(String msg) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(out);		
			writer.write(msg+"\r\n");
			writer.flush();
			System.out.println(name + " msg enviada");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			return false;
		}		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		status = Status.RUNNING;		
		try {
			this.out = user.getOutputStream();
			this.in = user.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			System.out.println("UserThread running");
			while(true) {				
				if(status == Status.FINISHED) break;
				if(status == Status.PAUSED) {
					Thread.sleep(100);
					continue;
				}				
				
				String msg = reader.readLine();
				System.out.println("[ " + name + " ]"+msg);
				JSONObject obj = new JSONObject(msg);
				obj.put("user", name);
				
				parent.messageAll(uuid, obj.toString());
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}

}
