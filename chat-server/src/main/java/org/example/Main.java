package org.example;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(900);
            Server server = new Server(serverSocket, "serverFirst");
            server.runServer();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}