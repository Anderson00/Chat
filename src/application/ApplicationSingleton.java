package application;

import controllers.HomeControllerView;
import javafx.scene.image.Image;

public class ApplicationSingleton {
	public static final ApplicationSingleton instance = new ApplicationSingleton();
	protected HomeControllerView homeController;
	private String ip; 
	private int porta;
	
	private ApplicationSingleton() {}
	
	public void showImageDialog(Image img) {
		homeController.showImageDialog(img);
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPorta() {
		return porta;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setPorta(int porta) {
		this.porta = porta;
	}
}
