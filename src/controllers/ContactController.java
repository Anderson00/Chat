package controllers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import application.ApplicationSingleton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ContactController {
	
	@FXML
	private HBox root;

	@FXML
    private Circle conectionStatus;

    @FXML
    private Label usernameLB;

    @FXML
    private Label userStatusLB;

    @FXML
    private ImageView removeContact;
    
    @FXML
    private StackPane stackPane1, stackPane2;
    
    private String ip,porta;
    private ListView<HBox> allMessages;
    private StackPane parent;
    private HomeControllerView controller;
    private ApplicationSingleton application;
    
    private Socket server;
    private String sala;
    private Thread connection = null;
    
    public boolean executed = false;
    
    private boolean connected = true;
    

    @FXML
    void initialize() {
        assert conectionStatus != null : "fx:id=\"conectionStatus\" was not injected: check your FXML file 'UserList.fxml'.";
        assert usernameLB != null : "fx:id=\"usernameLB\" was not injected: check your FXML file 'UserList.fxml'.";
        assert userStatusLB != null : "fx:id=\"userStatusLB\" was not injected: check your FXML file 'UserList.fxml'.";
        assert removeContact != null : "fx:id=\"removeContact\" was not injected: check your FXML file 'UserList.fxml'.";
        
        allMessages = new ListView<HBox>();
        application = ApplicationSingleton.instance;
                      
        EventHandler<Event> mouseClicked = event->{
        	contactSelected();
        };
        
        stackPane1.setOnMouseClicked(mouseClicked);
        stackPane2.setOnMouseClicked(mouseClicked);
        
        removeContact.setOnMouseClicked(event -> {
        	this.controller.contactList.getItems().remove(root);
        	if(this.controller.selectedContact == this)
        		this.controller.rootMessages.getChildren().clear();
        	this.allMessages = null;
        });
      
        connection = new Thread(new escutaTrasmissao()); 
    }
    
    public void contactSelected(){
    	controller.selectedContact = this;
    	root.getStyleClass().clear();
    	root.getStyleClass().add("selecionado");
    	for(HBox box : controller.contactList.getItems()){
    		if(box != root){
    			box.getStyleClass().clear();
            	box.getStyleClass().add("nao-selecionado");
    		}
    	}
    	parent.getChildren().clear();
    	parent.getChildren().add(allMessages);
    }
    
    public void setUser(HomeControllerView controller, String sala, boolean novo) {
    	if(novo) {
    		ContactController.this.controller = controller;
			ContactController.this.parent = controller.rootMessages;
        	usernameLB.setText(sala);
    		    			
    			new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							server = new Socket(application.getIp(), application.getPorta());
							OutputStreamWriter writer = new OutputStreamWriter(server.getOutputStream());
							BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
							JSONObject obj = new JSONObject();
							obj.put("add", sala);
							obj.put("name", controller.getUserName());
							
							writer.write(obj.toString()+"\r\n");
							writer.flush();
							
							JSONObject received = new JSONObject(reader.readLine());
							System.out.println("Received: " + received);
							if(received.has("error")) {
								application.showErrorDialog(received.getString("error"));
							}else {
								connection.start();
								conectionStatus.setFill(Color.GREEN);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("Error");
							e.printStackTrace();
						}  	 
					}
				}).start();    		
    	}else {
    		setUser(controller, sala);
    	}
    }
    
    public void setUser(HomeControllerView controller, String sala){    	
    	this.controller = controller;
    	this.parent = controller.rootMessages;
    	usernameLB.setText(sala);
    	
    	try {
			server = new Socket(application.getIp(), application.getPorta());
			enterRoom(sala);
			connection.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void sendMessage(String msg){
    	sendMessage(msg,true);
    }
    
    public void sendMessage(String msg, File imgFile){
    	sendMessage(msg,imgFile,true);
    }
    
    private void sendMessage(String msg, File imgFile, boolean userMessage){
    	String formato = imgFile.getName().split("[.]")[1];
    	if( !(formato.equals("jpg") || formato.equals("png")) )
    		return;
    		
    	new Thread(new enviaTrasmissao(msg, imgFile, userMessage)).start();
    }
    
    private void sendMessage(String msg,boolean userMessage){   
    	JSONObject obj = new JSONObject();
    	try{
    		OutputStreamWriter writer = new OutputStreamWriter(server.getOutputStream());
    		//BufferedWriter buffer = new BufferedWriter(writer);
    		
    		obj.put("msg", msg);
    		obj.put("user", controller.getUserName());
    		
    		writer.write(obj.toString()+"\r\n");
    		writer.flush();
    		
    		System.out.println("Enviou Mensagem");
    		   
    	}catch(SocketException e){
    		connected = false;
    		obj.put("error", e.getMessage());
    		this.conectionStatus.setFill(Color.RED);
    	}
    	catch(IOException e){
    		e.printStackTrace();    		
    	}
    	
    	printMessage(obj, userMessage); 
    }
    
    private void printMessage(JSONObject obj, boolean userMessage){
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/messageModel.fxml"));
    	try{
    		HBox box = loader.load();
    		allMessages.getItems().add(box);
    		MessagesController msgController = loader.getController();
    		msgController.setMessage(obj, userMessage);   
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    private void enterRoom(String room) {
    	try {
			OutputStreamWriter wrt = new OutputStreamWriter(server.getOutputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
			JSONObject obj = new JSONObject();
			obj.put("salaName", room);
			obj.put("name", controller.getUserName());
			wrt.write(obj.toString()+"\r\n");
			wrt.flush();
			
			JSONObject msg = new JSONObject(reader.readLine());
			if(msg.has("error")) {
				server.close();
				conectionStatus.setFill(Color.RED);
				application.showErrorDialog(msg.getString("error"));
			}else {
				conectionStatus.setFill(Color.GREEN);
			}
			System.out.println(msg.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private class escutaTrasmissao extends Task<String>{

		@Override
		protected String call() throws Exception {
			// TODO Auto-generated method stub	
			System.out.println("TASK INICIADO");
			while(true){
				System.out.println("eiorgkoerkgoerkgoekrg " + server);
				System.out.println(">>>>>>>>>> "+server.isConnected());
				if(server.isConnected()){
					conectionStatus.setFill(Color.GREEN);
					connected = true;
				}				
				InputStreamReader reader = new InputStreamReader(server.getInputStream());
				BufferedReader buffer = new BufferedReader(reader);			
				
				String msg = buffer.readLine();
				System.out.println(">>> " + msg);
				JSONObject obj = new JSONObject(msg);
				printMessage(obj);
				System.out.println(obj.toString());
				
			}
		}
		
		private void printMessage(JSONObject message){
			Platform.runLater(() -> {
				ContactController.this.printMessage(message, false);
			});
		}
		
		@Override
		protected void updateMessage(String message) {//CODIGO inutil
			// TODO Auto-generated method stub
			super.updateMessage(message);
			Platform.runLater(() -> {
				ContactController.this.printMessage(new JSONObject(message), false);
			});
		}
    	
    }
    
    private class enviaTrasmissao extends Task<String>{
    	
    	private String msg;
    	private File imgFile;
    	private boolean userMessage;
    	
    	public enviaTrasmissao(String msg, File imgFile, boolean userMessage) {
    		this.msg = msg;
    		this.imgFile = imgFile;
    		this.userMessage = userMessage;
    	}

		@Override
		protected String call() throws Exception {
			// TODO Auto-generated method stub	
			JSONObject obj;
			System.out.println("TASK INICIADO");
			try{
	    		OutputStream out = server.getOutputStream();
	    		OutputStreamWriter writer = new OutputStreamWriter(out);
	    		
	    		obj = new JSONObject();
	    		JSONArray arr = new JSONArray();
	    		InputStream imgStream = new FileInputStream(imgFile);
	    		byte bytes[] = Files.readAllBytes(imgFile.toPath());  		
	    		    		
	    		obj.put("msg", msg);
	    		obj.put("user", controller.getUserName());
	    		obj.put("img", DatatypeConverter.printBase64Binary(bytes));
	    		
	    		System.out.println(obj.toString());
	    		writer.write(obj.toString()+"\r\n");
	    		writer.flush();
	    		
	    		printMessage(obj);		
	    		
	    		return obj.toString();
	    	}catch(SocketException e){
	    		connected = false;
	    		ContactController.this.conectionStatus.setFill(Color.RED);
	    		return "N�o enviado";
	    	}
	    	catch(IOException e){
	    		e.printStackTrace();    		
	    	}
			return null;
		}
		
		private void printMessage(JSONObject message){
			Platform.runLater(() -> {
				ContactController.this.printMessage(message, true);
			});
		}
		
		@Override
		protected void updateMessage(String message) {//CODIGO inutil
			// TODO Auto-generated method stub
			super.updateMessage(message);
			Platform.runLater(() -> {
				ContactController.this.printMessage(new JSONObject(message), false);
			});
		}
    	
    }
}
