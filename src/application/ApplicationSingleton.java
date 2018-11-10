package application;

import controllers.HomeControllerView;
import javafx.scene.image.Image;

public class ApplicationSingleton {
	public static final ApplicationSingleton instance = new ApplicationSingleton();
	protected HomeControllerView homeController;
	
	private ApplicationSingleton() {}
	
	public void showImageDialog(Image img) {
		homeController.showImageDialog(img);
	}
}
