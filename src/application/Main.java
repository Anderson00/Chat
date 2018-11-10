package application;
	
import java.io.File;

import controllers.HomeControllerView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {		
			//carrega FX xml da interfaçe principal
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/Home.fxml"));
			StackPane root = loader.load();
			//Classe que controla interfaçe principal
			HomeControllerView controller = loader.getController();
			ApplicationSingleton.instance.homeController = controller;
			controller.setStage(primaryStage);
			Scene scene = new Scene(root,800,600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			scene.setOnKeyPressed(event -> {
				if(event.getCode().equals(KeyCode.F11))
					primaryStage.setFullScreen(!primaryStage.fullScreenProperty().get());
			});			
			primaryStage.setScene(scene);
			scene.getWindow().setOnCloseRequest(event -> System.exit(0));
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args); //JAVAFXsd
	}
	
    private static String findJarParentPath(File jarFile)// Nao utilizado
    {
        while (jarFile.getPath().contains(".jar"))
            jarFile = jarFile.getParentFile();
        return jarFile.getPath().substring(6);
}
   /**
     * Return the instalation path of any class.
     * 
     * @param theClass The class to find the installation path.
     * @return The installation path of any class.
     */
    public static String getInstallPath(Class< ? > theClass)// Nao utilizado
    {
        String url = theClass.getResource(theClass.getSimpleName() + ".class").getPath();
        File dir = new File(url).getParentFile();
        if (dir.getPath().contains(".jar"))
        return findJarParentPath(dir).replace("%20", " ");

    return dir.getPath().replace("%20", " ");
}
}
