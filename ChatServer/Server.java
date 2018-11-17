
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {
	ServerSocket socket;
	
	
	public Server(int serverPort) throws IOException {
		socket = new ServerSocket(serverPort);
	}
	
	public void start() {
		SalaThread sala = new SalaThread("teste");
		sala.start();
		BufferedReader reader;
		OutputStreamWriter writer;
		
		try {
			while(true) {		
				System.out.println("esperando...");
				Socket client = socket.accept();
				reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				writer = new OutputStreamWriter(client.getOutputStream());
				String msg = reader.readLine();
				JSONObject obj = new JSONObject(msg);
				String error = "";
				
				try {
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
						boolean stat = sala.addUsuarios(client, name);
						error = (stat)? "" : "Não foi possivel entrar na sala";
					}
					
					if(error.length() != 0) {
						obj.put("error", error);
						writer.write(obj+"\r\n");
						writer.flush();
						client.close();
					}
				
				}catch(JSONException e) {
					client.close();
				}
				
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
