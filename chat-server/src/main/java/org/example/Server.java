package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private String serverName;

    public Server(ServerSocket serverSocket, String serverName) {
        this.serverSocket = serverSocket;
        this.serverName = serverName;
    }

    public void runServer(){
        while (!serverSocket.isClosed()){
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Подключен новый клиент!");
                ClientManager clientManager = new ClientManager(socket);
                Thread thread = new Thread(clientManager);
                thread.start();
            }
            catch (IOException e){
                closeSocket();
            }
        }
    }

    /**
     * Закрытие объекта ServerSocket, в случае возникновения исключения
     */
    private void closeSocket()
    {
        try{
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
