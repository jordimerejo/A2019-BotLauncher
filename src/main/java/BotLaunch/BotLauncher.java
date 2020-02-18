/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package BotLaunch;



import java.io.BufferedReader;

import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;

import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;


import BotUtils.BotMessage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.control.ComboBox;

import javafx.scene.control.Label;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.control.TextField;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javafx.stage.Stage;

import javafx.util.Duration;





/**
 * @author Stefan Karsten
 *
 */

public class BotLauncher extends Application {
	


    private Button start; 
	private Text crtext;
	private Text usertext;
	private Text foldertext;

	private ComboBox botscombo;
	private ComboBox devicescombo;
	
	private TableView<BotMessage> list;
	private HashMap<String,String> devices;
	private HashMap<String,String> bots;
	private HashMap<String,String> automations;
	
	private TextField device;
	
	private String botId ;
	private String deviceId;
	private String deploymentId;
    
    
    private String cr;
    private Scene scene;
	private String apiKey;
	private String botFolder;
	private String user;
	private String token ;
	private Integer userID;

	private  Duration PROBE_FREQUENCY ;

    	

	@Override
	public void start(Stage stage) throws Exception {
		
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.automations = new HashMap<String,String>();
		
		String config = new String(Files.readAllBytes(Paths.get("C://ProgramData//AutomationAnywhere//BotLaunch//launcher.json"))); 
        JSONObject object = new JSONObject(config);
        this.cr = object.getString("cr");
        this.user = object.getString("user");
        this.apiKey = object.getString("apiKey");
        this.botFolder = object.getString("botFolder");
        
        Integer interval = Integer.parseInt(object.getString("interval"));
        PROBE_FREQUENCY = Duration.seconds(interval);

		  URL url = Paths.get("/fxml/launchform.fxml").toUri().toURL();
		  FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/fxml/launchform.fxml"));
		  fxmlloader.setController(this); 
	      Parent root = fxmlloader.load();
		  this.scene = new Scene(root,Color.WHITE);
	      url = this.getClass().getResource("/css/styles.css");
	 	  scene.getStylesheets().add(url.toExternalForm());
		  stage.setTitle("Bot Launcher");
          stage.setScene(this.scene);
          stage.setResizable(false);
	      
          list = ( TableView<BotMessage>)this.scene.lookup("#logview");
	      list.setEditable(false);
	      list.widthProperty().addListener(new ChangeListener<Number> () {
	              @Override
	              public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
	                  // Get the table header
	                  Pane header = (Pane)list.lookup("TableHeaderRow");
	                  if(header!=null && header.isVisible()) {
	                    header.setMaxHeight(0);
	                    header.setMinHeight(0);
	                    header.setPrefHeight(0);
	                    header.setVisible(false);
	                    header.setManaged(false);
	                  }
	              }
	          });
	      list.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
          
	      stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icons/logo.png")));
          stage.show();
          
      	  foldertext  = (Text)this.scene.lookup("#folder");
      	  usertext  = (Text)this.scene.lookup("#user");
      	  crtext  = (Text)this.scene.lookup("#cr");
      	  
          foldertext.setText(this.botFolder);
      	  usertext.setText(this.user);
      	  crtext.setText(this.cr);
          TableColumn<BotMessage, String> statusColumn = new TableColumn<BotMessage,String>();
          statusColumn.setPrefWidth(40);
          statusColumn.setResizable(false);
          statusColumn.setEditable(false);
          statusColumn.setSortable(false);
          statusColumn.setStyle("-fx-alignment: CENTER");
          statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
          statusColumn.setCellFactory(column -> {
         	    return new TableCell<BotMessage,String>() {
         	        @Override
         	        protected void updateItem(String item, boolean empty) {
         	            super.updateItem(item, empty);
         	            if (item == null || empty) {
         	                setText(null);
         	                setStyle("");
         	                setGraphic(null);
         	            } else {
         	             Image image; 
         	            String[] items = item.split(",");
         	            String type = items[0];
         	            String timestamp = items[1];
         	             switch (type) {
         	             case "ERROR":
         	            	 image = new Image(this.getClass().getResourceAsStream("/icons/error.png"));
         	            	 break;
          	             case "WARNING":
         	            	 image = new Image(this.getClass().getResourceAsStream("/icons/warning.png"));
         	            	 break;
          	             case "QUESTION":
         	            	 image = new Image(this.getClass().getResourceAsStream("/icons/question.png"));
         	            	 break;
          	             case "START":
         	            	 image = new Image(this.getClass().getResourceAsStream("/icons/start.png"));
         	            	 break;
          	             case "STOP":
         	            	 image = new Image(this.getClass().getResourceAsStream("/icons/stop.png"));
         	            	 break;
          	             default:
         	            	 image = new Image(this.getClass().getResourceAsStream("/icons/info.png"));
         	             }
         	             
        	             VBox box = new VBox();
        	             box.setAlignment(Pos.CENTER);
        	             ImageView imageView = new ImageView(image);
        	             Label dateLabel = new Label(timestamp);
        	             dateLabel.setStyle("-fx-font-size:6pt");
        	             box.getChildren().add(imageView);	   
        	             box.getChildren().add(dateLabel);     	     
        		    	 setGraphic(box);

         	              }
                 }
             };
         });
          
          
          TableColumn<BotMessage,String> messageColumn = new TableColumn<BotMessage,String>();
          messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
          messageColumn.setResizable(false);
          messageColumn.setPrefWidth(380);
          messageColumn.setEditable(false);
          messageColumn.setSortable(false);
          messageColumn.setStyle("-fx-alignment: CENTER-LEFT");
          messageColumn.setCellFactory(column -> {
         	    return new TableCell<BotMessage,String>() {
         	        @Override
         	        protected void updateItem(String item, boolean empty) {
         	            super.updateItem(item, empty);
               //     logger.info("Cell {}",item);
         	            if (item == null || empty) {
         	                setText(null);
         	                setStyle("");
         	                setGraphic(null);
         	            } else {
         	                 Text text = new Text(item);
         	                 text.setStyle("-fx-text-alignment:justify;");                      
         	                 text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
         	                 setGraphic(text);

         	              }
                 }
             };
         });
          list.getColumns().addAll(statusColumn, messageColumn);
          
         list.widthProperty().addListener(new ChangeListener<Number> () {
              @Override
              public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                  // Get the table header
                  Pane header = (Pane)list.lookup("TableHeaderRow");
                  if(header!=null && header.isVisible()) {
                    header.setMaxHeight(0);
                    header.setMinHeight(0);
                    header.setPrefHeight(0);
                    header.setVisible(false);
                    header.setManaged(false);
                  }
              }
          });
          list.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
      	  
          

          
     	  authenticate();
      	  BotUtils.BotMessage botmessage;
     	  
      	/*  this.devicescombo = (ComboBox)this.scene.lookup("#device");
      	  this.devices = new HashMap<String,String>();
      	  ObservableList<String> deviceoptions = FXCollections.observableArrayList();

      	  JSONArray devicesJson = getDevices();
      	  if (devicesJson != null) {
      	  devicesJson.forEach(item -> {
      	    JSONObject jsonobj = (JSONObject) item;
      	    if (jsonobj.getString("status").equals("CONNECTED")) {
      	    	String name = jsonobj.getString("hostName");
      	    	String id = jsonobj.getString("id");
      	    	this.devices.put(name, id);
      	    	deviceoptions.add(name);
      	    }
       	 });
      	  devicescombo.getItems().addAll(deviceoptions);
      	  devicescombo.getSelectionModel().select(0);
      	  	LocalDateTime now = LocalDateTime.now();  
      	  	botmessage = new BotMessage(new String("INFORMATION"),new String("Init of device options completed"),new String(now.format(formatter)));
      	  	list.getItems().add(0,botmessage);
      	  };
    */

     	  this.botscombo = (ComboBox)this.scene.lookup("#bot");
     	  this.bots = new HashMap<String,String>();
	      ObservableList<String> botsoptions = FXCollections.observableArrayList();
     	  JSONArray botsJson = getBots();
      	  if (botsJson != null) {
     	  botsJson.forEach(item -> {
     	    JSONObject jsonobj = (JSONObject) item;
     	    String name = jsonobj.getString("name");
     	    String id = jsonobj.getString("id");
     	    if (!name.contains(".png")) {
     	    	this.bots.put(name, id);
     	    	botsoptions.add(name);
     	    }
      	    });
     	     botscombo.getItems().addAll(botsoptions);
     	     botscombo.getSelectionModel().select(0);
     	    LocalDateTime now = LocalDateTime.now();  
         	botmessage = new BotMessage(new String("INFORMATION"),new String("Init of bot options completed"),new String(now.format(formatter)));
  			list.getItems().add(0,botmessage);
      	  }
     	  
      	  
      	 Timeline timeline = new Timeline(
      	        new KeyFrame(
      	          Duration.ZERO,
      	          new EventHandler<ActionEvent>() {
      	            @Override public void handle(ActionEvent actionEvent) {
      	            	JSONArray automationJson = null;
						try {
							automationJson = getAutomationStatus();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
      	        	  if (automationJson  != null) {
      	        		  automationJson .forEach(item -> {
      	            	    JSONObject jsonobj = (JSONObject) item;
      	            	    String status = jsonobj.getString("status");
      	            	    String id = jsonobj.getString("deploymentId");
      	            	    String message = jsonobj.getString("message");
      	            	    if (id != null && status != null) {
      	            	    	String automationStatus = getAutomations().get(id);
      	            	    	if (automationStatus != null) {
          	            	    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
          	            	    	LocalDateTime now = LocalDateTime.now();
          	            	    	BotMessage botmessage;
      	            	    		if (status.contains("COMPLETED")) {
                            	        botmessage = new BotMessage( "STOP",new String("Automation '"+id+"' : '"+status+"'"),new String(now.format(formatter)));
      	            				    list.getItems().add(0,botmessage);
        	            				getAutomations().remove(id);
      	            	    		}
          	            	    	if (status.contains("FAILED")) {                           	        
                            	        botmessage = new BotMessage( "ERROR",new String("Automation '"+id+"' : '"+status+"'"),new String(now.format(formatter)));
      	            	    			list.getItems().add(0,botmessage);
      	            	    			if (message.length()>0) {
      	            	    				now = LocalDateTime.now();  
      	            	    				botmessage = new BotMessage(new String("ERROR"),new String("Automation '"+id+"' : '"+message.replace("\n", " ").replace("\r", "")+"'"),new String(now.format(formatter)));
      	            	    				list.getItems().add(0,botmessage);
      	            	    			}
        	            				getAutomations().remove(id);
      	            	    		}
      	            	    	}
      	            	    	getAutomations().replace(id,status);
      	            	    }
      	             	 });
      	        	  }
      	            }
      	          }),
      	        new KeyFrame(
      	          PROBE_FREQUENCY
      	        )
      	    );
      	    timeline.setCycleCount(Timeline.INDEFINITE);
      	    timeline.play();

	}
	
	
	
	
	private JSONArray getDevices() throws Exception {

		JSONArray devicesJson = null;
		URL url = new URL(this.cr+"/v2/devices/list");
		JSONObject body = new JSONObject();
		String result = POSTRequest(url,this.token,body);
		if (!result.contains("POST NOT WORKED")) {
			JSONObject json = new JSONObject(result);
			 devicesJson = (JSONArray)json.get("list");
		}
		return devicesJson;
	}
	
	
	
	private JSONArray getAutomationStatus() throws Exception {
  
		JSONArray statusJson = null;
		URL url = new URL(this.cr+"/v2/activity/list");
		JSONObject body = new JSONObject();
		JSONObject filter = new JSONObject();
		filter.put("operator", "or");
		JSONArray operands = new JSONArray();
		for (Entry<String, String> automation : automations.entrySet()) {
			JSONObject operand = new JSONObject();
			operand.put("operator", "eq");
			operand.put("field","deploymentId");
			operand.put("value", automation.getKey());
			operands.put(operand);
		}
		filter.put("operands", operands);
		body.put("filter",filter);
		String result = POSTRequest(url,this.token,body);
		if (!result.contains("POST NOT WORKED")) {
			JSONObject json = new JSONObject(result);
			 statusJson = (JSONArray)json.get("list");
			 for (int i = 0; i < statusJson.length(); i++) {
				 JSONObject item = (JSONObject)statusJson.get(i);
				 if (automations.containsKey(item.getString("deploymentId"))) {
					 automations.put(item.getString("deploymentId"),item.getString("automationName"));
				 }
				
			}
		}
		return statusJson;
	}
	
	
	private JSONArray getBots() throws Exception {

		JSONArray botsJson = null;
		URL url = new URL(this.cr+"/v2/repository/file/list");
		JSONObject body = new JSONObject(); 
		JSONObject filter = new JSONObject();
		filter.put("operator", "substring");
		filter.put("value", this.botFolder);
		filter.put("field", "path");
		body.put("filter",filter);
		JSONObject page = new JSONObject();
		page.put("offset", "0");
		page.put("length", "2000");
		body.put("page",page);
		String result = POSTRequest(url,this.token,body);
		if (!result.contains("POST NOT WORKED")) {
			JSONObject json = new JSONObject(result);
			botsJson = (JSONArray)json.get("list");
		}
		return botsJson;
	}


	

	private void authenticate() throws Exception {
		URL url = new URL(this.cr+"/v1/authentication");
		JSONObject body = new JSONObject();
		body.put("username", this.user);
		body.put("apikey", this.apiKey);
		String result = POSTRequest(url,null,body);
		if (!result.contains("POST NOT WORKED")) {
			JSONObject Json = new JSONObject(result);
			this.token = Json.get("token").toString();
			this.userID = ((JSONObject)Json.get("user")).getInt("id");
		
		}
	}	 
	
	
	private JSONObject deploy() throws Exception {
		JSONObject automation = null;
		if ((this.userID != null) && (this.botId != null)) {
			URL url = new URL(this.cr+"/v3/automations/deploy");
			JSONObject body = new JSONObject();
			body.put("fileId",this.botId);
			JSONArray userIds = new JSONArray();
			userIds.put(this.userID);
			body.put("runAsUserIds", userIds);
			String result = POSTRequest(url,this.token,body);
			if (!result.contains("POST NOT WORKED")) {
				automation = new JSONObject(result);
			}
			
		}

		return automation;	 
	}
		
	 
	 
	  @FXML
	  protected  void callAction() throws Exception {			
	   //     this.deviceId = this.devices.get(this.devicescombo.getValue().toString());
	        this.botId = this.bots.get(this.botscombo.getValue().toString());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	        authenticate();
	        JSONObject automation = deploy();
	        this.deploymentId = "";
	        if (automation != null) {
	        	LocalDateTime now = LocalDateTime.now(); 
	        	String deploymentId= automation.getString("deploymentId");
	        	automations.put(deploymentId, "");
	        	getAutomationStatus() ;
	          	BotMessage botmessage = new BotMessage(new String("START"),new String("Automation for Bot '"+this.botscombo.getValue()+"' as '"+automations.get(deploymentId)+"' deployed"),new String(now.format(formatter)));
				list.getItems().add(0,botmessage);
	        }

	  }
	  
	
	 private HashMap<String,String> getAutomations() {
	        return this.automations;
	 }
	 

		
    public static void main(String[] args) {
		        launch(args);
	}
		 
	
	
	private String  POSTRequest(URL posturl, String token, JSONObject body) throws IOException  {

		int responseCode = 0;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime now = LocalDateTime.now(); 
	    HttpURLConnection postConnection = null;
		try {
			postConnection = (HttpURLConnection) posturl.openConnection();
		    postConnection.setRequestMethod("POST");
	        if (token != null)
	        {
	    	    postConnection.setRequestProperty("x-authorization", token);
	        }


		    postConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
		    postConnection.setDoOutput(true);
		    OutputStream os = postConnection.getOutputStream();

		    os.write(body.toString().getBytes());
		    
		    os.flush();

		    os.close();

		    responseCode = postConnection.getResponseCode();
		    
		} catch (IOException e) {

        	BotMessage botmessage = new BotMessage(new String("ERROR"),new String("Post failed : " + e.getMessage()),new String(now.format(formatter)));
			list.getItems().add(0,botmessage);
		}

	    if (responseCode == HttpURLConnection.HTTP_OK) { //success

	        BufferedReader in = new BufferedReader(new InputStreamReader(

	            postConnection.getInputStream(),StandardCharsets.UTF_8.name()));

	        String inputLine;

	        StringBuffer response = new StringBuffer();

	        while ((inputLine = in .readLine()) != null) {

	            response.append(inputLine);

	        } in .close();

	        
	        return  response.toString() ;

	    } else {
          	BotMessage botmessage = new BotMessage(new String("ERROR"),new String("API call '"+posturl.toString()+"' failed with status code '"+ new Integer(responseCode).toString()+"'"),new String(now.format(formatter)));
			list.getItems().add(0,botmessage);
			if (responseCode > 0)
			{	
				botmessage = new BotMessage(new String("ERROR"),new String("Error Message :  "+ postConnection.getResponseMessage()),new String(now.format(formatter)));
				list.getItems().add(0,botmessage);
			}
	
	        return "POST NOT WORKED "+ Integer.valueOf(responseCode).toString();

	    }

	}




	  

	  
}



