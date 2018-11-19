import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import model.Status;

public class ServerThread extends Thread {
	
	Socket client;
	
	public ServerThread(Socket client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
			String msg = reader.readLine();
			JSONObject obj = new JSONObject(msg);
			String error = "";
			if(obj.has("add") && obj.has("name")) { //Adiciona uma nova sala e o dono da sala
				SalaThread st = new SalaThread(obj.getString("add"));
				if(st.getSalaStatus() == Status.ERROR) {
					error = "Sala já existe";
				}else {
					st.start();
					st.addUsuarios(client, obj.getString("name"));	
				}											
			}else if(obj.has("salaName") && obj.has("name")){ //Entra em uma sala existente 
				if(!ApplicationSingleton.getInstance().entrarNaSala(client, obj))
					error = "Sala não existe";
			}
			else if(obj.has("list")) { //Lista todas as salas e a quantidade de membros
				JSONObject oo = ApplicationSingleton.getInstance().getSalasList();
				System.out.println(oo.toString());
				writer.write(oo.toString()+"\r\n");
				writer.flush();
			}else {
				String name = obj.getString("name");
				boolean stat = ApplicationSingleton.getInstance().getSalaTeste().addUsuarios(client, name);
				error = (stat)? "" : "Não foi possivel entrar na sala";
			}
			
			if(error.length() != 0) {
				obj.put("error", error);
				writer.write(obj+"\r\n");
				writer.flush();
				client.close();
			}else {
				obj.put("success", 1);
				writer.write(obj+"\r\n");
				writer.flush();
			}
		
		} catch(JSONException e) {
			try {
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch(IOException e) {
			e.printStackTrace();
			try {
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
