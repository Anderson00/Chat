package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jfxtras.labs.scene.control.BigDecimalField;


public class HomeControllerView {
	@FXML
    private ResourceBundle resources;
	
	@FXML
	private StackPane stackPane;
	
	@FXML
	private SplitPane splitPane;

    @FXML
    private URL location;

    @FXML
    private JFXButton btNewContact;

    @FXML
    protected ListView<HBox> contactList;

    @FXML
    private TextField msgField;

    @FXML
    private JFXButton btSendMsg, btAddImage;

    @FXML
    protected StackPane rootMessages;
    
    boolean b = false;
    protected ContactController selectedContact = null;
    
    private Stage stage;

    protected ServerSocket server;
    private Thread connection = null;
    
    private String nome = null;
    private String msgError = "";
    
    private File imgFile = null;//Imagem associada com a mensagem
    

    @FXML
    void initialize() {
    	System.out.println(contactList.getChildrenUnmodifiable().size());
    	    	    	
    	setServerPort();//Dialogo para inserir o nome e a porta do cliente

        SplitPane.setResizableWithParent(splitPane.getItems().get(0), false);
               
        //Campo de mensagem
        msgField.setOnKeyPressed(event -> {
        	if(event.getCode() == KeyCode.ENTER){// se apertar enter a msg é enviada
        		if(!msgField.getText().equals(""))
        			btSendMsg.fire();
        	}
        });
        
        //Botão para enviar Mensagem
        btSendMsg.setOnAction(event -> {
        	if(selectedContact != null){
        		if(imgFile != null)
        			selectedContact.sendMessage(msgField.getText(),imgFile);
        		else
        			selectedContact.sendMessage(msgField.getText());
        		this.imgFile = null;
        		msgField.setText("");
        	}
        });
        
        //Adiciona novo Contato
        btNewContact.setOnAction(e -> {
        	newContactDialog();
        });
        
        //Adiciona imagem
        btAddImage.setOnAction(event -> {
        	FileChooser fileChooser = new FileChooser();
        	FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Images (*.jpg,*.png)", "*.jpg","*.png");
        	fileChooser.getExtensionFilters().add(filter);
        	imgFile = fileChooser.showOpenDialog(this.stage.getScene().getWindow());
        	//System.out.println(imgFile.length());
        });
        
    }
    
    public void setStage(Stage stage){
    	this.stage = stage;
    }
    
    public String getUserName(){
    	return this.nome;
    }
    
    public void showImageDialog(Image img) {
    	StackPane stack = new StackPane();
    	ImageView imgView = new ImageView(img);
    	imgView.setPreserveRatio(true);
    	imgView.setFitWidth(800);
    	stack.getChildren().add(imgView);
    	
    	
    	JFXButton btOk = new JFXButton("Ok");
    	JFXButton btCancel = new JFXButton("Cancel");
    	
    	JFXDialogLayout layout = new JFXDialogLayout();
    	layout.setBody(stack);
    	JFXDialog dialog = new JFXDialog(stackPane, layout, JFXDialog.DialogTransition.CENTER);    	
    	imgView.setOnMouseClicked(event -> dialog.close());
    	dialog.show();
    }
        
    private void setServerPort(){
    	    	
    	JFXTextField nameField = new JFXTextField();
    	nameField.setPromptText("Nome");
    	nameField.setLabelFloat(true);
    	nameField.setFocusColor(Color.GREEN);
    	nameField.setUnFocusColor(Color.GREEN);
    	
    	BigDecimalField portField = new BigDecimalField();
    	portField.setPromptText("Porta");
    	portField.setNumber(BigDecimal.ZERO);
    	portField.setFormat(new DecimalFormat("#", new DecimalFormatSymbols(Locale.ENGLISH)));
    	portField.setMinValue(BigDecimal.ZERO);
    	portField.setMaxValue(new BigDecimal(65335));
    	portField.setPrefWidth(100);
    	
    	
    	JFXButton btOk = new JFXButton("Ok");
    	JFXButton btCancel = new JFXButton("Cancel");
    	
    	Text title = new Text("Set server port");
    	title.setFont(new Font(14));
    	title.setFill(Color.GREEN);
    	    	
    	
    	HBox box = new HBox(nameField,portField);
    	box.setPadding(new Insets(10, 0, 0, 0));
    	box.setSpacing(10);
    	HBox.setHgrow(nameField, Priority.ALWAYS);
    	
    	Label msgError = new Label(this.msgError);
    	msgError.setTextFill(Color.RED);
    	
    	VBox vbox = new VBox(box,msgError);
    	
    	JFXDialogLayout layout = new JFXDialogLayout();
    	layout.setHeading(title);
    	layout.setBody(vbox);
    	layout.setActions(btOk);
    	JFXDialog dialog = new JFXDialog(stackPane, layout, JFXDialog.DialogTransition.TOP);
    	btOk.setOnAction(event -> {   
    		nome = nameField.getText();
    		try {
    			server = new ServerSocket(new Integer(portField.getText()));
    			if(stage!=null){
        			stage.setTitle(nameField.getText()+"@"+server.getInetAddress().getHostAddress()+":"+portField.getText());
        		}
    			
    			connection = new Thread(new ConnectionTask());
        		connection.start();
        		
        		dialog.close();
    		}catch(BindException e){
    			dialog.close();
    			this.msgError = e.getMessage();
    			this.setServerPort();
    		}
    		catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	});
    	dialog.setOverlayClose(false);
    	dialog.show();
    }
    
    private void newContactDialog(){
    	   	
    	JFXTextField nameField = new JFXTextField();
    	JFXTextField ipField = new JFXTextField();
    	JFXTextField portaField = new JFXTextField();
    	
    	nameField.setPromptText("name");
    	nameField.setLabelFloat(true);
    	nameField.setUnFocusColor(Color.GREEN);
    	nameField.setFocusColor(Color.GREEN);
    	portaField.setPromptText("port");
    	portaField.setLabelFloat(true);
    	portaField.setUnFocusColor(Color.GREEN);
    	portaField.setFocusColor(Color.GREEN);
    	ipField.setPromptText("ex:127.0.0.1");
    	ipField.setLabelFloat(true);
    	ipField.setUnFocusColor(Color.GREEN);
    	ipField.setFocusColor(Color.GREEN);
    	        	
    	
    	HBox hbox = new HBox(ipField,portaField);
    	hbox.setSpacing(10);
    	HBox.setHgrow(ipField, Priority.ALWAYS);
    	
    	VBox vbox = new VBox(nameField,hbox);
    	vbox.setSpacing(20);
    	vbox.setPadding(new Insets(10));
    	
    	Text title = new Text("Add Contact");
    	title.setFont(new Font(14));
    	title.setFill(Color.GREEN);
    	
    	JFXButton btOk = new JFXButton("Ok");
    	JFXButton btCancel = new JFXButton("Cancel");
    	
    	JFXDialogLayout layout = new JFXDialogLayout();
    	layout.setHeading(title);
    	layout.setBody(vbox);
    	layout.setActions(btOk, btCancel);
    	JFXDialog dialog = new JFXDialog(stackPane, layout, JFXDialog.DialogTransition.TOP);
    	btOk.setOnAction(event -> {
    		createContact(nameField.getText(), ipField.getText(), portaField.getText());

    		dialog.close();
    	});
    	btCancel.setOnAction(event -> dialog.close());
    	dialog.show();
    }
    
    private void createContact(String name, String ip, String porta){
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/UserList.fxml"));    		
		try {
			contactList.getItems().add(loader.load());
			ContactController controller = loader.getController();
			selectedContact = controller;
			controller.setUser(this,name, ip, porta);
			controller.contactSelected();				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void createContact(String name, String ip, Socket client){
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/UserList.fxml"));    		
		try {
			contactList.getItems().add(loader.load());
			ContactController controller = loader.getController();
			selectedContact = controller;
			controller.setUser(this,name, ip, client);
			controller.contactSelected();				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private class ConnectionTask extends Task<String>{

    	private Socket client;
    	
		@Override
		protected String call() {
			// TODO Auto-generated method stub	
			try{
				while(true){
					Socket client = server.accept();
					System.out.println("Conectou");	
					this.client = client;					
					InputStreamReader reader = new InputStreamReader(client.getInputStream());
					BufferedReader buffer = new BufferedReader(reader);
					String msg = buffer.readLine();
					updateMessage(msg);
					System.out.println("Novo contato");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void updateMessage(String message) {
			// TODO Auto-generated method stub
			super.updateMessage(message);
			
			InetAddress address = client.getInetAddress();
			Platform.runLater(()->{
				createContact(message, address.getHostAddress(), client);
			});
		}
    	
    }
    
}
