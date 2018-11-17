package controllers;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import application.ApplicationSingleton;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import javafx.scene.PointLight;

public class MessagesController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    
    @FXML
    private HBox rootMessage;
    
    @FXML
    private VBox polygonLeft, polygonRight;

    @FXML
    private BorderPane bodyMessage;

    @FXML
    private Label message, horaLabel, nomeLabel;

    @FXML
    void initialize() {
        assert bodyMessage != null : "fx:id=\"bodyMessage\" was not injected: check your FXML file 'messageModel.fxml'.";
        assert message != null : "fx:id=\"message\" was not injected: check your FXML file 'messageModel.fxml'.";

        bodyMessage.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        
        
    }
    
    public void setMessage(JSONObject obj, boolean userMessage){
    	
    	message.setText(obj.getString("msg"));
    	SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    	horaLabel.setText(format.format(Calendar.getInstance().getTime()));
    	nomeLabel.setText(obj.getString("user"));
    	
    	if(obj.has("img")) {
    		byte decodedImg[] = DatatypeConverter.parseBase64Binary(obj.getString("img"));
    		
    		setMessage(new Image(new ByteArrayInputStream(decodedImg)), userMessage);
    	}
    	
    	if(!userMessage){
    		polygonRight.setVisible(true);
    		polygonRight.getChildren().get(0).setStyle("-fx-fill: #ccc");
    		bodyMessage.setStyle("-fx-background-color:#ccc;-fx-background-radius:0px 10px 10px 10px");
    		bodyMessage.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    		rootMessage.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    	}else {
    		polygonRight.setVisible(true);
    		polygonRight.setStyle("-fx-fill:rgba(0,200,0,0.5)");
    		bodyMessage.setStyle("-fx-background-color: rgba(0,200,0,0.5);-fx-background-radius:0px 10px 10px 10px");
    		rootMessage.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    	}
    	if(obj.has("error")){
    		Label msgError = new Label( (obj.getString("error") == null)? "Não enviado": obj.getString("error") );
    		msgError.setTextFill(Color.RED);
    		msgError.setUnderline(true);    		
    		bodyMessage.setBottom(msgError);
    	}
    }
    
    public void setMessage(Image img, boolean userMessage){
    	if(img == null)
    		return;
    	ImageView imgView = new ImageView(img);
    	imgView.setPreserveRatio(true);
    	imgView.setFitWidth(200);
    	imgView.setCursor(Cursor.HAND);
    	imgView.setOnMouseClicked(event -> {
    		ApplicationSingleton.instance.showImageDialog(img);
    	});
    	
    	bodyMessage.setTop(imgView);
    }
	
}
