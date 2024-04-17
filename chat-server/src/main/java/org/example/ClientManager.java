package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable{
    private final Socket socket;
    private String name;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void removeClient(){
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClient();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Отправка сообщения всем слушателям и конкретному клиенту через комманду: ->имя_пользователя сообщение
     * @param message сообщение
     */
    private void broadcastMessage(String message){
        for(ClientManager client : clients){
            try {
                if (!client.name.equals(name) && message != null) {
                    //Для того, чтобы не печатать сообщения в которых мы объявляем только имя клиента
                    if(message.contains(":")){
                        //Для отправки всем
                        if(!message.contains("->")){
                            client.bufferedWriter.write(message);
                            client.bufferedWriter.newLine();
                            client.bufferedWriter.flush();
                        }
                        //Для отправки только конкретному пользователю
                        else {
                            String[] decomposeMessage = message.split(" ");

                            String recipient = decomposeMessage[1];
                            //Что бы не отправить самому себе
                            if(!recipient.contains(name)){
                                ClientManager temp = clients.stream().
                                        //Для опредления жесткого синтаксиса, где: "->" стоит перед именем получателя
                                                filter(c -> recipient.indexOf(c.name) > recipient.indexOf("->")).
                                        findFirst().
                                        get();

                                //Убираем комманду
                                message = message.replaceAll("->" + client.name, "");
                                temp.bufferedWriter.write(message);
                                temp.bufferedWriter.newLine();
                                temp.bufferedWriter.flush();
                            }

                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    /**
     * Чтение сообщений ОТ клиента
     */
    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                /*
                if (messageFromClient == null) {
                    // для  macOS
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
                 */
                broadcastMessage(messageFromClient);
            }
            catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }


}
