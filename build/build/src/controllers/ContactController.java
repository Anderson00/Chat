package controllers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

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
    
    private Socket client;
    private ServerSocket server;
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
    
    public void setUser(HomeControllerView controller,String username, String ip, String porta){
    	
    	this.controller = controller;
    	this.parent = controller.rootMessages;
    	usernameLB.setText(username);
    	this.ip = ip;
    	this.porta = porta;
    	this.server = controller.server;
    	
    	try {
			client = new Socket(this.ip, new Integer(this.porta));
			System.out.println(client.isConnected());	
						
			//Envia nome do usuario			
			OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
			BufferedWriter wBuffer = new BufferedWriter(writer);
			wBuffer.write(controller.getUserName()+"\r\n");
			wBuffer.flush();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	connection.start();
    }
    
    public void setUser(HomeControllerView controller,String username, String ip, Socket client){
    	
    	this.controller = controller;
    	this.parent = controller.rootMessages;
    	usernameLB.setText(username);
    	this.ip = ip;
    	this.server = controller.server;
    	this.client = client;
    	    			    	
    	connection.start();
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
    		
    	try{
    		OutputStream out = client.getOutputStream();
    		OutputStreamWriter writer = new OutputStreamWriter(out);
    		BufferedWriter buffer = new BufferedWriter(writer);
    		buffer.write("imgFile:"+imgFile.length()+";"+"msg:"+msg+"\r\n");    		
    		buffer.flush();
    		
    		BufferedImage imageBuffer = ImageIO.read(imgFile);    		
    		ImageIO.write(imageBuffer, formato, out);
    		
    		System.out.println("Enviou Tamanho da imagem e mensagem");
    		printMessage(msg,SwingFXUtils.toFXImage(imageBuffer, null), userMessage);   
    	}catch(SocketException e){
    		connected = false;
    		this.conectionStatus.setFill(Color.RED);
    	}
    	catch(IOException e){
    		e.printStackTrace();    		
    	}
    }
    
    private void sendMessage(String msg,boolean userMessage){    	
    	try{
    		OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
    		BufferedWriter buffer = new BufferedWriter(writer);
    		buffer.write("msg:"+msg+"\r\n");    		
    		buffer.flush();
    		
    		System.out.println("Enviou Mensagem");
    	}catch(SocketException e){
    		connected = false;
    		this.conectionStatus.setFill(Color.RED);
    	}
    	catch(IOException e){
    		e.printStackTrace();    		
    	}
    	
    	printMessage(msg,userMessage);    	
    }
    
    private void printMessage(String msg, boolean userMessage){
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/messageModel.fxml"));
    	try{
    		HBox box = loader.load();
    		allMessages.getItems().add(box);
    		MessagesController msgController = loader.getController();
    		msgController.setMessage(msg, Calendar.getInstance().getTime(), !connected, userMessage);   
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    private void printMessage(String msg, Image img,boolean userMessage){
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/messageModel.fxml"));
    	try{
    		HBox box = loader.load();
    		allMessages.getItems().add(box);
    		MessagesController msgController = loader.getController();
    		msgController.setMessage(msg, Calendar.getInstance().getTime(), img, !connected, userMessage);   
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    
    private class escutaTrasmissao extends Task<String>{

		@Override
		protected String call() throws Exception {
			// TODO Auto-generated method stub	
			while(true){
				System.out.println(">>>>>>>>>> "+client.isConnected());
				if(client.isConnected()){
					conectionStatus.setFill(Color.GREEN);
					connected = true;
				}
				InputStream input = client.getInputStream(); 
				InputStreamReader reader = new InputStreamReader(client.getInputStream());
				BufferedReader buffer = new BufferedReader(reader);				
				String msg = buffer.readLine();
				//padrao imgFile:1231;msg:kweifkweifkwef
				String[] str = msg.split(";");
				if(str[0].matches("imgFile:\\d*")){
					System.out.println(">> "+str[1].replaceFirst("msg:", ""));
					
					BufferedImage imageBuffer = ImageIO.read(input);
					Image img = SwingFXUtils.toFXImage(imageBuffer,null);
					
					if(str[1].startsWith("msg:")){
						System.out.println("Mensagem valida");		
						System.out.println(">>>>>>>>>>>>>>>>>> " + str[1]);
						printMessage(str[1].replaceFirst("msg:", ""),img);
					}else{
						//Requisi��o invalida
					}					
				}else{
					System.out.println(">>>>>>>>>>>>>>>>>> " + msg);
					String str2[] = str[0].split("msg:");
					
					printMessage(str2[str2.length-1]);
				}
				
			}
		}
		
		private void printMessage(String message){
			Platform.runLater(() -> {
				ContactController.this.printMessage(message, false);
			});
		}
		
		private void printMessage(String message,Image img){
			Platform.runLater(() -> {
				ContactController.this.printMessage(message, img, false);
			});
		}
		@Override
		protected void updateMessage(String message) {
			// TODO Auto-generated method stub
			super.updateMessage(message);
			Platform.runLater(() -> {
				ContactController.this.printMessage(message, false);
			});
		}
    	
    }
    
}
