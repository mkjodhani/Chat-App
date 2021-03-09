package sample;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main extends Application
{
    private String URL = "https://github.com/mkjodhani/Chat-App/raw/main/Application/Artifacts/Gossip.jar";
    private String FILE_NAME = "Gossip.jar";
    private boolean already = false;
    public Image backgroundImage = new Image(getClass().getResourceAsStream("/img/BACK.jpeg"));
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Label label = new Label("Installer");
        label.setFont(Font.loadFont(getClass().getResourceAsStream("/font/Quest.otf"),100));
        label.setTextFill(Paint.valueOf("#F2E276"));
        Download downloadFile = null;
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(250);
        VBox vBox = new VBox(50);
        vBox.setPrefWidth(500);
        vBox.setPrefHeight(250);

        BackgroundSize size = new BackgroundSize(vBox.getWidth(),vBox.getHeight(), false, false, true, true);
        BackgroundImage image = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, size);
        Background background = new Background(image);
        vBox.setBackground(background);


        vBox.setAlignment(Pos.CENTER);
        Button button = new Button( "Download");
        Button close = new Button("Close");

        button.setStyle("-fx-background-color: \n" +
                "        #090a0c,\n" +
                "        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\n" +
                "        linear-gradient(#20262b, #191d22),\n" +
                "        radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));\n" +
                "    -fx-background-radius: 5,4,3,5;\n" +
                "    -fx-background-insets: 0,1,2,0;\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\n" +
                "    -fx-font-family: \"Arial\";\n" +
                "    -fx-text-fill: linear-gradient(white, #d0d0d0);\n" +
                "    -fx-font-size: 12px;\n" +
                "    -fx-padding: 10 20 10 20;");

        close.setStyle("-fx-background-color: \n" +
                "        #090a0c,\n" +
                "        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\n" +
                "        linear-gradient(#20262b, #191d22),\n" +
                "        radial-gradient(center 50% 0%, radius 100%, rgba(114,131,148,0.9), rgba(255,255,255,0));\n" +
                "    -fx-background-radius: 5,4,3,5;\n" +
                "    -fx-background-insets: 0,1,2,0;\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\n" +
                "    -fx-font-family: \"Arial\";\n" +
                "    -fx-text-fill: linear-gradient(white, #d0d0d0);\n" +
                "    -fx-font-size: 12px;\n" +
                "    -fx-padding: 10 20 10 20;");
        try
        {
            downloadFile = new Download(URL,progressBar,button);
            Path path = Paths.get(FILE_NAME);
            try
            {
                FileChannel fileChannel  = FileChannel.open(path);
                if(fileChannel.size() == downloadFile.getTotalSize())
                {
                    already = true;
                    button.setText("Launch");
                    HBox hBox = new HBox(50,button,close);
                    hBox.setAlignment(Pos.CENTER);
                    vBox.getChildren().addAll(label, hBox);
                }
            }catch (NoSuchFileException e)
            {
                HBox hBox = new HBox(50, button,close);
                hBox.setAlignment(Pos.CENTER);
                vBox.getChildren().addAll(label,progressBar,hBox);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        Download finalDownloadFile = downloadFile;
        Download finalDownloadFile1 = downloadFile;
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        String type = button.getText().trim();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                if (finalDownloadFile1.getComplete())
                                    button.setText("Launch");
                                switch (type)
                                {
                                    case "Download":
                                    case "Resume":
                                        button.setText("Pause");
                                        break;
                                    case "Pause":
                                        button.setText("Resume");
                                        break;
                                }
                            }
                        });
                        try {
                            switch (type)
                            {
                                case "Download":
                                    finalDownloadFile.downloadFile();
                                    break;
                                case "Pause":
                                    finalDownloadFile.toggle();
                                    break;
                                case "Resume":
                                    finalDownloadFile.toggle();
                                    break;
                                case "Launch":
                                    Runtime.getRuntime().exec("java -jar Gossip.jar");
                                    break;
                            }

                        } catch (InterruptedException | IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                primaryStage.close();
            }
        });
        primaryStage.setScene(new Scene(vBox));
        primaryStage.setFullScreen(true);
        primaryStage.setMaximized(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
