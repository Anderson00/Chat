
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
		ApplicationSingleton.getInstance().setSalaTeste(sala);
		
		while(true) {		
			System.out.println("esperando...");				
			try {
				new ServerThread(socket.accept()).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
}
