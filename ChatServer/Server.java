
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
				try {
					String name = obj.getString("name");
					boolean stat = sala.addUsuarios(client, name);
					
					obj.put("error", (stat)? "" : "Não foi possivel entrar na sala");
					writer.write(obj+"\r\n");
					writer.flush();
				
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
