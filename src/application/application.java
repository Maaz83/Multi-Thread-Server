/**********************************************
 Workshop 9

 Course: JAC444 - 4
 Last Name: Saiyed
 First Name: Mohammad Maaz
 ID: 113485205
 Section: ZAA
 This assignment represents my own work in accordance with Seneca Academic Policy.
 Signature
 Date: 16-04-2023
 **********************************************/


package application;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class application extends Application {

    Stage window;
    String address;
    int port;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

//			Get the port number and address to join the server
            address = super.getParameters().getRaw().get(0);
            port = Integer.parseInt(super.getParameters().getRaw().get(1));

            // Window
            this.window = primaryStage;
            this.window.setTitle("Client");

            // Grid
            GridPane grid = createGuiControls();

            Scene scene = new Scene(grid, 400, 400);

            // Client Stuff
            new Client(address, port, grid);
            // Finish Window
            this.window.setScene(scene);
            this.window.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GridPane createGuiControls() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        // add columns
        ColumnConstraints column0 = new ColumnConstraints();
        column0.setPercentWidth(80);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);

        // add rows
        RowConstraints row0 = new RowConstraints();
        row0.setPercentHeight(75);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(20);
        RowConstraints row2 = new RowConstraints();
        row1.setPercentHeight(20);

        // add columns and rows to the grid
        grid.getRowConstraints().addAll(row0, row1, row2);
        grid.getColumnConstraints().addAll(column0, column1);
        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Client {

        // Socket Network Communication
        private Socket socket = null;
        private DataInputStream in = null;
        private DataOutputStream out = null;
        private final LinkedBlockingQueue<String> MESSAGES;
        private final LinkedBlockingQueue<String> MESSAGES_BY_CLIENT;
        private String username = "";

        // UI Details
        private final TextFlow CHAT = new TextFlow();
        private final TextArea CHAT_INPUT = new TextArea();
        private final Button BUTTON_SEND = new Button("SEND");
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        //		Client constructor to which creates new socket and connects it to the server port
        public Client(String address, int port, GridPane grid) {
            this.MESSAGES_BY_CLIENT = new LinkedBlockingQueue<>();
            this.MESSAGES = new LinkedBlockingQueue<>();

            try {

//				Create a socket at the address and port given as argument in the command line
                socket = new Socket(address, port);

                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

// 				Server sends "Enter Username: " message on the command prompt, Client acknowledges it
                String serverMessage = in.readUTF();
                System.out.println(serverMessage);

                // user(client) enters the username
                username = input.readLine();
                setUsername(username);

//				username sent to the server
                out.writeUTF(username);

//				Connection succesful to the server and server sends welcome message
                serverMessage = in.readUTF();

//				Display the message on the chatbox
                Text text = new Text(serverMessage);
                CHAT.getChildren().add(text);

//				chat box padding
                CHAT.setPadding(new Insets(5, 5, 5, 5));
                GridPane.setHgrow(CHAT, Priority.ALWAYS);
                GridPane.setVgrow(CHAT, Priority.ALWAYS);
                ScrollPane sp = new ScrollPane();
                sp.setContent(CHAT);
                GridPane.setConstraints(sp, 0, 0);

//				Chat box Status (Circle)
                Circle statusCircle = new Circle(0, 0, 7);
                statusCircle.setFill(Paint.valueOf("#7CFC00"));
//				statusCircle.getStyleClass().add("status-circle");
                GridPane.setValignment(statusCircle, VPos.TOP);
                GridPane.setHgrow(statusCircle, Priority.ALWAYS);
                GridPane.setVgrow(statusCircle, Priority.ALWAYS);
                GridPane.setConstraints(statusCircle, 1, 0);

//				Chat box Status (Label)
                Label statusLabel = new Label("Online");
                GridPane.setValignment(statusLabel, VPos.TOP);
                GridPane.setHalignment(statusLabel, HPos.CENTER);
                GridPane.setHgrow(statusLabel, Priority.ALWAYS);
                GridPane.setVgrow(statusLabel, Priority.ALWAYS);
                GridPane.setConstraints(statusLabel, 1, 0);

//				Chat box UserName
                Label usernameLabel = new Label("User: " + username);
                GridPane.setValignment(usernameLabel, VPos.TOP);
                GridPane.setHalignment(usernameLabel, HPos.LEFT);
                GridPane.setHgrow(usernameLabel, Priority.ALWAYS);
                GridPane.setVgrow(usernameLabel, Priority.ALWAYS);
                usernameLabel.setPadding(new Insets(30, 0, 0, 0));
                GridPane.setConstraints(usernameLabel, 1, 0);

//				Create text Area area to UI
                CHAT_INPUT.setPromptText("Enter message...");
                GridPane.setHgrow(CHAT_INPUT, Priority.ALWAYS);
                GridPane.setVgrow(CHAT_INPUT, Priority.ALWAYS);
                GridPane.setConstraints(CHAT_INPUT, 0, 1);

// 				Create button for send messag
                GridPane.setHgrow(BUTTON_SEND, Priority.ALWAYS);
                GridPane.setVgrow(BUTTON_SEND, Priority.ALWAYS);
                GridPane.setHalignment(BUTTON_SEND, HPos.RIGHT);
                GridPane.setValignment(BUTTON_SEND, VPos.TOP);
                GridPane.setConstraints(BUTTON_SEND, 0, 2);

                grid.getChildren().addAll(sp, CHAT_INPUT, BUTTON_SEND, statusCircle, statusLabel, usernameLabel);

//	*** 		Action listener on Send Button to send message to the server
                setUpButtonSend();

//				Thread 1 to send the messages to the Server
                Thread userInput = new Thread() {

                    public void run() {

                        try {
                            String message = "";
                            while (!message.equals("exit")) {
                                try {
                                    message = MESSAGES_BY_CLIENT.take();
                                    out.writeUTF(message);
                                } catch (IOException | InterruptedException e) {
                                    System.out.println(e);
                                }
                            }

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            in.close();
                            out.close();
                            socket.close();
                            System.exit(0);
                        } catch (IOException e) {
                            System.out.println("Error here " + e.getMessage());
                        }
                    }
                };
                userInput.start();

//				Thread 2 to read the messages from the message queue
                Thread readMessagesToClient = new Thread() {

                    public void run() {
                        String message = "";
                        while (true) {
                            try {
                                message = MESSAGES.take();
                                addMessageToChat(message);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                readMessagesToClient.start();

//				Thread 3 - to read messages from the server into the message queue
                ReadMessagesFromServer server = new ReadMessagesFromServer(socket);
                new Thread(server).start();

            } catch (Exception e) {
                System.out.println("Error Occured " + e.getMessage());
            }
        }

        //		Setter
        public void setUsername(String username) {
            this.username = username;
        }

        //		Action Listener on button
        public void setUpButtonSend() {
            BUTTON_SEND.setOnAction(e -> {
                String message = CHAT_INPUT.getText();
                MESSAGES_BY_CLIENT.add(message);
                CHAT_INPUT.setText("");
            });
        }

        //		Add new message to the chat box
        public void addMessageToChat(String message) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Date date = new Date(System.currentTimeMillis());
                    String dateFormatted = formatter.format(date);
                    Text text = new Text("\n" + dateFormatted + " " + message);
                    CHAT.getChildren().add(text);
                }
            });
        }

        //		read message from the server and add it to the message queue using thread
        private class ReadMessagesFromServer implements Runnable {
            DataInputStream in = null;
            Socket socket;

            ReadMessagesFromServer(Socket socket) {
                this.socket = socket;
            }

            public void run() {
                try {
                    in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    out = new DataOutputStream(socket.getOutputStream());

                    while (true) {
                        try {
                            String line = in.readUTF();
                            MESSAGES.put(line);
                        } catch (IOException | InterruptedException e) {
//							e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

    }


}
