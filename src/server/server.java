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

package server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

public class server extends Application{

    Stage window;
    int port;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            port = Integer.parseInt(super.getParameters().getRaw().get(0));

            // Window
            this.window = primaryStage;
            this.window.setTitle("Server");

            // Grid
            GridPane grid = new GridPane();
            grid.setVgap(8);
            grid.setHgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));

            // Scene
            Scene scene = new Scene(grid, 500, 400);

            // Creata a new server with port number as 3000
            new Server(port, grid);

            // Finish Window
            this.window.setScene(scene);
            this.window.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Server {

        // Socket
        private Socket socket = null;
        private ServerSocket server = null;

        // Socket Network Communication
        private final ArrayList<HandleClient> CLIENTS = new ArrayList<>();
        private final LinkedBlockingQueue<String> messages;

        // GUI
        private final ListView<String> CLIENTS_LIST_VIEW = new ListView<>();
        private final TextArea CHAT = new TextArea();
        ArrayList<String> clientColors = new ArrayList<String>();
        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

        public Server(int port, GridPane grid) {
            this.messages = new LinkedBlockingQueue<>();

            // Customize CLIENTS_LIST_VIEW
            changeClientsListView();
            clientColors.add("#FCBA03");
            clientColors.add("#03FCBA");
            clientColors.add("#BA03FC");
            clientColors.add("#FC03C1");
            clientColors.add("#03FC3D");

//			Thread 1 to accept new users joining the server (Chatroom)
            Thread acceptClients = new Thread() {

                public void run() {
                    try {
                        server = new ServerSocket(port);
                        CHAT.setEditable(false);
                        CHAT.appendText("Server Started\n");
                        CHAT.appendText("Waiting Client...");
                        GridPane.setConstraints(CHAT, 0, 0);
                        CHAT.getStyleClass().add("server-CHAT");

                        GridPane.setConstraints(CLIENTS_LIST_VIEW, 1, 0);
                        CLIENTS_LIST_VIEW.getStyleClass().add("clients-list");

                        grid.getChildren().addAll(CHAT, CLIENTS_LIST_VIEW);

                        while (true) {

                            socket = server.accept();

                            // handle multithreading for clients
//							Create a new thread for every client
                            HandleClient client = new HandleClient(socket.getRemoteSocketAddress().toString(),
                                    socket.getPort(), socket);
                            CLIENTS.add(client);
                            new Thread(client).start();
                        }

                    } catch (Exception e) {
                        System.out.println("Error here " + e.getMessage());
                    }
                }
            };

            acceptClients.start();

//			Thread 2 - to pass the messages accross all the clients
            Thread writeMessages = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            String message = messages.take();

                            for (HandleClient client : CLIENTS) {
                                client.write("> " + message);
                            }

                        } catch (Exception e) {
                            System.out.println("Error is it here " + e.getMessage());
                        }
                    }
                }
            };

            writeMessages.start();

        }

        //		table view to hold the list of users
//		create random color for each user
        public void changeClientsListView() {
            CLIENTS_LIST_VIEW.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param) {
                    return new ListCell<String>() {

                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item == null || empty) {
                                setText(null);
                                setStyle("-fx-background-color: white");
                            } else {
                                setText(item);
                                String color = getRandomColor();
                                setStyle("-fx-background-color: " + color);
                            }
                        }

                    };
                }
            });
        }

        //		get a random color to update in the view
        public String getRandomColor() {
            Random rand = new Random();
            return clientColors.get(rand.nextInt(clientColors.size()));
        }

        //		Thread class for each client (User) joining the chatroom
//		Each user is handled in a different thread to maintain concurrency.
        private class HandleClient implements Runnable {

            private DataInputStream in = null;
            private DataOutputStream out = null;
            private String address;
            private int port;
            public Socket socket;
            private String username;

            public HandleClient(String address, int port, Socket socket) {

                this.setAddress(address);
                this.setPort(port);
                this.socket = socket;

                try {
                    this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    this.out = new DataOutputStream(socket.getOutputStream());
                } catch (Exception e) {
                    System.out.println("Error here " + e.getMessage());
                }
            }

            public void run() {
                try {

                    // Get entered username from user
                    String username = null;
                    out.writeUTF("Enter username: ");

                    while (username == null) {
                        username = in.readUTF();
                        setUsername(username);
                        addClientToUI(username);
                    }

                    if (this.username.isBlank()) {
                        out.writeUTF("Connection Accepted" + "\nWelcome " + username
                                + "\nNOTE: SINCE YOU HAVEN'T PROVIDED AN USERNAME, YOU'RE IN MODE READ ONLY.");
                    } else {
                        out.writeUTF("Connection Accepted" + "\nWelcome " + username);

                        // welcome message to server
                        String message = "Welcome " + this.username;
                        try {
                            messages.put(message);
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                        addMessageToChat(message);
                    }

                    String line = "";
                    while (!line.equals("exit")) {

                        try {
                            line = this.in.readUTF();
                            String line_formatted = this.username + ": " + line;

                            if (line.equals("allUsers")) {
                                write(CLIENTS.toString());
                                addMessageToChat(line_formatted);
                                continue;
                            }
//								If the username is blank then the user can only view the chats
                            else if (!this.username.isBlank()) {
                                messages.put(line_formatted);
                                // print to server
                                addMessageToChat(line_formatted);
                            }

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            System.out.println(this.username);
                        }
                    }

                    try {
                        String goodbyeMessage = "Server: Goodbye " + this.username + ", user is removed from the queue";
                        messages.put(goodbyeMessage);
                        addMessageToChat(goodbyeMessage);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    removeClientFromUI(this.getUsername());
                    CLIENTS.remove(this);
                    this.in.close();
                    this.out.close();
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error :" + e.getMessage());
                }

            }

            public void write(String message) {
                try {
                    this.out.writeUTF(message);
                } catch (IOException e) {
                    System.out.println("Error here " + e.getMessage());
                }
            }

            public void addMessageToChat(String message) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        Date date = new Date(System.currentTimeMillis());
                        String dateFormatted = formatter.format(date);
                        CHAT.appendText("\n" + dateFormatted + " " + message);
                    }
                });
            }

            public void addClientToUI(String username) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        CLIENTS_LIST_VIEW.getItems().add(username);
                    }
                });
            }

            public void removeClientFromUI(String username) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        CLIENTS_LIST_VIEW.getItems().remove(username);
                    }
                });
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getUsername() {
                return this.username;
            }

            @Override
            public String toString() {

                return getUsername();
            }

            @SuppressWarnings("unused")
            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            @SuppressWarnings("unused")
            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

        }

    }
}
