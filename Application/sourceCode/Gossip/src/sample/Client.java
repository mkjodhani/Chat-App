package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements Runnable
{
    private static Font msgFont = Font.loadFont("file:src/font/TeachersStudent-Regular.ttf",30);
    private ScrollPane scrollPane = null;
    private  String hostname;
    private VBox vBoxMessages;
    private Stage newWindow =null;
    private ComboBox<String> client = new ComboBox<String>();
    private ClientSocket clientSocket;

    public Client(String hostname)
    {
        this.hostname = hostname;
        clientSocket = new ClientSocket(hostname.trim());
        if (!clientSocket.getServerUp())
        {
            Button OK  = new Button("OK");
            Text errorMessage = new Text("Server is not accepting your request at this moment!");
            VBox vBox = new VBox(20,errorMessage,OK);
            BorderPane borderPane1 = new BorderPane();
            vBox.setAlignment(Pos.CENTER);
            borderPane1.setCenter(vBox);
            Scene Error = new Scene(borderPane1,500,100);
            Stage ErrorStage = new Stage();
            OK.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    ErrorStage.close();
                }
            });
            ErrorStage.setScene(Error);
            ErrorStage.show();
        }
    }

    @Override
    public void run()
    {
        if (clientSocket.getServerUp())
        {
            vBoxMessages = new VBox(5);
            Scene chat = null;

            scrollPane = new ScrollPane();
            Image chatImage = new Image("file:src/img/CHATBACK3.jpeg");
            scrollPane.setBackground(Background.EMPTY);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVvalue(1.0d);
            scrollPane.setContent(vBoxMessages);
            scrollPane.setFitToWidth(true);
            Button send = new Button("Send!");
            Button sendFile = new Button("Send File");
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; ");

            Text text = new Text("Name :");
            text.setTextAlignment(TextAlignment.CENTER);
            TextField msg= new TextField();
            Label message = new Label("Messgae :");
            text.setTextAlignment(TextAlignment.CENTER);

            HBox hBox = new HBox(text,client,message,msg,send);
            hBox.setStyle("-fx-background: white; -fx-background-color: white; ");
            hBox.setAlignment(Pos.CENTER);
            VBox vBox = new VBox();

            vBox.getChildren().addAll(scrollPane,hBox);
            BackgroundSize backgroundSize = new BackgroundSize(100,100,false,false,true,true);
            vBox.setBackground(new Background(new BackgroundImage(chatImage,BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,backgroundSize)));
            chat = new Scene(vBox,450 ,500);
            //allMessage.setWrappingWidth(chat.getWidth());
            chat.getStylesheets().add("sample/chat.css");
            newWindow = new Stage();
            newWindow.setTitle(hostname);
            newWindow.setScene(chat);
            newWindow.widthProperty().addListener(event -> {
                scrollPane.setPrefWidth(newWindow.getWidth());
            });
            newWindow.heightProperty().addListener(event -> {
                scrollPane.setPrefHeight(newWindow.getHeight());
            });
            newWindow.widthProperty().addListener(event -> {
                hBox.setPrefWidth(newWindow.getWidth());
            });

            newWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent)
                {
                    clientSocket.sendData(EncoderDecoder.makeService("Close",clientSocket.getHostname()));
                    try {
                        clientSocket.getSocket().close();
                    } catch (IOException e) {
                    }
                }
            });

            newWindow.show();

            send.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent actionEvent)
                {
                    try
                    {
                        String To = "To : "+ client.getSelectionModel().getSelectedItem().trim();
                        String mssg = msg.getText().trim();
                        clientSocket.sendData(EncoderDecoder.encoder(hostname.trim(),client.getSelectionModel().getSelectedItem().trim(),msg.getText()));

                        Text nameTo = new Text(To);
                        nameTo.setWrappingWidth(420);
                        nameTo.setTextAlignment(TextAlignment.RIGHT);
                        nameTo.setFont(msgFont);
                        Text Mssg = new Text(mssg);
                        Mssg.setWrappingWidth(420);
                        Mssg.setFont(msgFont);
                        Mssg.setTextAlignment(TextAlignment.RIGHT);

                        VBox MessageBox = new VBox(5,nameTo,Mssg);
                        MessageBox.setAlignment(Pos.CENTER_RIGHT);
                        VBox.setMargin(MessageBox,new Insets(5,5,5,5));
                        MessageBox.getStyleClass().add("custom-dashed-border");
                        vBoxMessages.getChildren().add(MessageBox);
                        VBox.setMargin(MessageBox,new Insets(2.5,10,2.5,10));

                        scrollPane.setVvalue(1.0d);
                    }
                    catch (Exception e)
                    {
                        Button CANCEL = new Button("OK");
                        Text errorMessage = new Text("Enter The Valid Name for Username...");
                        VBox vBox = new VBox(20,errorMessage,CANCEL);
                        BorderPane borderPane1 = new BorderPane();
                        vBox.setAlignment(Pos.CENTER);
                        borderPane1.setCenter(vBox);
                        Scene Error = new Scene(borderPane1,500,100);
                        Stage ErrorStage = new Stage();
                        ErrorStage.setScene(Error);
                        CANCEL.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent)
                            {
                                ErrorStage.close();
                            }
                        });
                        ErrorStage.show();
                    }
                }
            });
            sendFile.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent actionEvent)
                {

                }
            });
            UpdateMessage();
        }
    }
    void UpdateMessage()
    {
        Runnable task = new Runnable() {
            @Override
            public void run()
            {
                while (newWindow.isShowing())
                {
                    if(clientSocket.getInput().hasNextLine())
                    {
                        String line = clientSocket.getInput().nextLine();
                        switch (EncoderDecoder.getTypeMessgae(line))
                        {
                            case "Message":
                                String finalLine = line;
                                Platform.runLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        String To = EncoderDecoder.getFromIndex(finalLine);
                                        String mssg = EncoderDecoder.decoder(finalLine);
                                        Text nameTo = new Text(To);
                                        nameTo.setFont(msgFont);
                                        nameTo.setWrappingWidth(420);
                                        nameTo.setTextAlignment(TextAlignment.LEFT);
                                        Text Mssg = new Text(mssg);
                                        Mssg.setFont(msgFont);
                                        Mssg.setWrappingWidth(420);
                                        Mssg.setTextAlignment(TextAlignment.LEFT);

                                        VBox MessageBox = new VBox(nameTo,Mssg);
                                        MessageBox.setAlignment(Pos.CENTER_LEFT);
                                        VBox.setMargin(MessageBox,new Insets(5,5,5,5));
                                        vBoxMessages.getChildren().add(MessageBox);
                                        VBox.setMargin(MessageBox,new Insets(2.5,10,2.5,10));
                                        //MessageBox.setStyle("-fx-background: grey; -fx-background-color: grey; -fx-opacity :0.5;-fx-border-radius : 5px");
                                        MessageBox.getStyleClass().add("custom-dashed-border");
                                        scrollPane.setVvalue(1.0d);
                                    }
                                });
                                break;
                            case "Error":
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        Button OK  = new Button("OK");
                                        Text ErrorMessage = new Text(EncoderDecoder.getErrorMessgae(line));
                                        VBox vBox = new VBox(20,ErrorMessage,OK);
                                        BorderPane errorpan = new BorderPane();
                                        vBox.setAlignment(Pos.CENTER);
                                        errorpan.setCenter(vBox);
                                        Scene ErrorScene = new Scene(errorpan,500,100);
                                        Stage UserError = new Stage();
                                        OK.setOnAction(new EventHandler<ActionEvent>() {
                                            @Override
                                            public void handle(ActionEvent actionEvent) {
                                                UserError.close();
                                            }
                                        });
                                        UserError.setScene(ErrorScene);
                                        UserError.show();
                                    }
                                });
                                break;
                            case "Service":
                                switch (EncoderDecoder.getTypeServiceResponce(line))
                                {
                                    case "clientList":
                                        Platform.runLater(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                if(!EncoderDecoder.getServiceMessage(line).isBlank())
                                                {
                                                    for(String name : EncoderDecoder.getServiceMessage (line).split(" "))
                                                    {
                                                        client.getItems().add(name);
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    case "Echo":
                                        clientSocket.sendData(EncoderDecoder.makeService("Echo",clientSocket.getHostname()));
                                        break;
                                    case "addMember":
                                        String newMemeber = EncoderDecoder.getServiceMessage(line);
                                        try {
                                            client.getItems().add(newMemeber);
                                        } catch (IllegalStateException e) {
                                        }
                                        break;
                                    case "removeMember":
                                        String removedMember = EncoderDecoder.getServiceMessage(line);
                                        try {
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try
                                                    {
                                                        if(client.getSelectionModel().getSelectedItem().equals(removedMember))
                                                        {
                                                            client.setValue("Default");
                                                        }
                                                        client.getItems().remove(removedMember);
                                                    }
                                                    catch (Exception exception)
                                                    {
                                                        System.out.println(" Member removed from the Drop Down List.");
                                                    }
                                                }
                                            });
                                        }
                                        catch (IllegalStateException e)
                                        {
                                            System.out.println("Member can not be removed from the Drop Down List.\"");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
    public Socket getSocket()
    {
        return clientSocket.getSocket();
    }

}
