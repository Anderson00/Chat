
import java.util.HashMap;
import java.util.UUID;

public class ApplicationSingleton {	
	private static final ApplicationSingleton instance = new ApplicationSingleton();
	HashMap<UUID, SalaThread> salas;
	
	private ApplicationSingleton() {
		salas = new HashMap<UUID, SalaThread>();
	}
	
	public boolean adicionarSala(UUID uuid, SalaThread sala) {
		if(salas.put(uuid, sala) == null)
			return false;
		return true;
	}
	
	public static ApplicationSingleton getInstance() {
		return instance;
	}
}
