package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

import javax.swing.plaf.SeparatorUI;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;

import application.ApplicationSingleton;
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
import javafx.scene.shape.Circle;



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
    private JFXButton btNewContact, btAddContact, btShowRooms;

    @FXML
    protected ListView<HBox> contactList;

    @FXML
    private TextField msgField;
    
    @FXML
    private Label userName, userStatus, salaName, salaStatus;

    @FXML
    private JFXButton btSendMsg, btAddImage;

    @FXML
    protected StackPane rootMessages;
    
    boolean b = false;
    protected ContactController selectedContact = null;
    
    private Stage stage;

    protected Socket server;
    private Thread connection = null;
    
    private String nome = null;
    private String msgError = "";
    
    private File imgFile = null;//Imagem associada com a mensagem
    

    @FXML
    void initialize() {
    	System.out.println(contactList.getChildrenUnmodifiable().size());
    	    	    	
    	ApplicationSingleton.instance.setIp("127.0.0.1");
    	setServerPort();//Dialogo para inserir o ip e a porta do Server

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
        
        btAddContact.setOnAction(e -> {
        	addContactDialog();
        });
        
        btShowRooms.setOnAction(e -> {
        	showRoomsDialog();
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
    	Circle c = new Circle();
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
    		userName.setText(nome);
    		userStatus.setText("Conectado");
    		//ApplicationSingleton.instance.setIp(nameField.getText());
    		ApplicationSingleton.instance.setPorta(portField.getNumber().intValue());
    		dialog.close();
    	});
    	dialog.setOverlayClose(false);
    	dialog.show();
    }
    
    private void addContactDialog() {
    	JFXTextField salaNameField = new JFXTextField();
    	
    	salaNameField.setPromptText("name");
    	salaNameField.setLabelFloat(true);
    	salaNameField.setUnFocusColor(Color.GREEN);
    	salaNameField.setFocusColor(Color.GREEN);
    	        	
    	
    	VBox vbox = new VBox(salaNameField);
    	vbox.setPadding(new Insets(10));
    	
    	Text title = new Text("ADD Room");
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
    		createContact(salaNameField.getText());
    		dialog.close();
    	});
    	btCancel.setOnAction(event -> dialog.close());
    	dialog.show();
    }
    
    void showRoomsDialog() {
    	ListView<String> list = new ListView<String>();      	
    	list.getStylesheets().add(getClass().getResource("../resources/style/listView2.css").toExternalForm());
    	list.setStyle("-fx-background-color:#222;");
    	
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
		    		
					Socket s = new Socket(ApplicationSingleton.instance.getIp(), ApplicationSingleton.instance.getPorta());
					OutputStreamWriter ss = new OutputStreamWriter(s.getOutputStream());
					BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
					JSONObject obj = new JSONObject();
					obj.put("list", 0);
					
					ss.write(obj+"\r\n");
					ss.flush();
					
					JSONObject ob = new JSONObject(reader.readLine());
					System.out.println(ob.toString());
					JSONArray arr = ob.getJSONArray("rooms");
					for(int i = 0; i < arr.length(); i++) {
						JSONObject obb = arr.getJSONObject(i);
						list.getItems().add(obb.getString("room")+"("+obb.getInt("n")+")");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
    	
    	VBox vbox = new VBox(list);
    	vbox.setPadding(new Insets(10));
    	
    	Text title = new Text("ADD Room");
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
    		dialog.close();
    	});
    	btCancel.setOnAction(event -> dialog.close());
    	dialog.show();
    }
    
    private void newContactDialog(){
    	JFXTextField salaNameField = new JFXTextField();
    	
    	salaNameField.setPromptText("name");
    	salaNameField.setLabelFloat(true);
    	salaNameField.setUnFocusColor(Color.GREEN);
    	salaNameField.setFocusColor(Color.GREEN);
    	        	
    	
    	VBox vbox = new VBox(salaNameField);
    	vbox.setPadding(new Insets(10));
    	
    	Text title = new Text("ADD Room");
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
    		createContact(salaNameField.getText(), true);
    		dialog.close();
    	});
    	btCancel.setOnAction(event -> dialog.close());
    	dialog.show();
    }
    
    public void showErrorDialog(String error) {    	
    	Text title = new Text("Error");
    	title.setFont(new Font(14));
    	title.setFill(Color.GREEN);
    	
    	JFXButton btOk = new JFXButton("Ok");
    	
    	JFXDialogLayout layout = new JFXDialogLayout();
    	layout.setHeading(title);
    	layout.setBody(new Label(error));
    	layout.setActions(btOk);
    	JFXDialog dialog = new JFXDialog(stackPane, layout, JFXDialog.DialogTransition.TOP);
    	btOk.setOnAction(event -> {
    		dialog.close();
    	});
    	dialog.show();
    }
    
    private void createContact(String sala, boolean novo) {
    	if(novo) {
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/UserList.fxml"));    		
			try {
				contactList.getItems().add(loader.load());
				ContactController controller = loader.getController();
				selectedContact = controller;
				controller.setUser(this, sala,novo);
				controller.contactSelected();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else {
    		createContact(sala);
    	}
    }
    
    private void createContact(String sala){
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/layout/UserList.fxml"));    		
		try {
			contactList.getItems().add(loader.load());
			ContactController controller = loader.getController();
			selectedContact = controller;
			controller.setUser(this, sala);
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
					InputStreamReader reader = new InputStreamReader(server.getInputStream());
					OutputStreamWriter writer = new OutputStreamWriter(server.getOutputStream());
					JSONObject obj = new JSONObject();
					obj.put("name", nome);
					writer.write(obj.toString()+"\r\n");
					writer.flush();
					
					BufferedReader breader = new BufferedReader(reader);					
					updateMessage(breader.readLine());
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
			JSONObject obj = new JSONObject(message);
			String str = obj.getString("sala");
			Platform.runLater(()->{
				createContact(str);
			});
		}
    	
    }
}
