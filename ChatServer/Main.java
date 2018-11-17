
import java.io.IOException;

public class Main {
	public static final int serverPort = 8080;
	
	public static void main(String args[]) {
		try {
			Server server = new Server(serverPort);
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
