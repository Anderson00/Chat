
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApplicationSingleton {	
	private static final ApplicationSingleton instance = new ApplicationSingleton();
	
	private HashMap<UUID, SalaThread> salas;
	private SalaThread salaTeste;
	
	private ApplicationSingleton() {
		salas = new HashMap<UUID, SalaThread>();
	}
	
	public boolean adicionarSala(UUID uuid, SalaThread sala) {
		if(salas.put(uuid, sala) == null)
			return true;
		return false;
	}
	
	public SalaThread getSalaTeste() {
		return salaTeste;
	}
	
	public void setSalaTeste(SalaThread salaTeste) {
		this.salaTeste = salaTeste;
	}
	
	public boolean entrarNaSala(Socket client,JSONObject obj) {		
		if(obj.has("salaName") && obj.has("name")) {
			for(Map.Entry<UUID, SalaThread> mp : salas.entrySet()) {
				if(mp.getValue().getSalaName().equals(obj.getString("salaName"))) {
					mp.getValue().addUsuarios(client, obj.getString("name"));
					return true;
				}
			}
		}
		return false;
	}
	
	public JSONObject getSalasList() {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();		
		salas.forEach( (uuid, salaThread) -> {
			JSONObject oo = new JSONObject();
			oo.put("room", salaThread.getSalaName());
			oo.put("n", salaThread.numberOfUsers());
			arr.put(oo);
		});
		obj.put("rooms", arr);
		
		return obj;
	}
	
	public static ApplicationSingleton getInstance() {
		return instance;
	}
}
