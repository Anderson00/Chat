package controllers;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class MessagesController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    
    @FXML
    private HBox rootMessage;

    @FXML
    private BorderPane bodyMessage;

    @FXML
    private Label message, horaLabel;

    @FXML
    void initialize() {
        assert bodyMessage != null : "fx:id=\"bodyMessage\" was not injected: check your FXML file 'messageModel.fxml'.";
        assert message != null : "fx:id=\"message\" was not injected: check your FXML file 'messageModel.fxml'.";

        bodyMessage.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        
        
    }
    
    public void setMessage(String msg, Date data, boolean error, boolean userMessage){
    	message.setText(msg);
    	SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    	horaLabel.setText(format.format(data));
    	if(!userMessage){
    		bodyMessage.setStyle("-fx-background-color:#ccc;-fx-background-radius:10px");
    		rootMessage.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    	}
    	if(error){
    		Label msgError = new Label("Não enviado, offline");
    		msgError.setTextFill(Color.RED);
    		msgError.setUnderline(true);    		
    		bodyMessage.setBottom(msgError);
    	}
    }
    
    public void setMessage(String msg, Date data, Image img,boolean error, boolean userMessage){
    	if(img == null)
    		return;
    	ImageView imgView = new ImageView(img);
    	imgView.setPreserveRatio(true);
    	imgView.setFitWidth(200);
    	System.out.println("apareceu Imagem");
    	
    	bodyMessage.setTop(imgView);
    	setMessage(msg,data,error,userMessage);
    }
	
}
