package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final String name;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    private Thread listener;
    private Thread sender;

    public Client(Socket socket, String userName){
        this.socket = socket;
        name = userName;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Отправить сообщение
     */
    public void sendMessage(){
        sender = new Thread(new Runnable() {
            @Override
            public void run() {
                //!!! Для того чтобы можно было писать больше одного сообщения
                while (socket.isConnected()) {
                    try {
                        bufferedWriter.write(name);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();

                        Scanner readMessage = new Scanner(System.in);
                        String message = readMessage.nextLine();
                        bufferedWriter.write(name + ": " + message);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        sender.start();

    }

    /**
     * Слушатель для входящих сообщений
     */
    public void listenForMessage(){
        listener = new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()){
                    String message;
                    try {
                        message = bufferedReader.readLine();
                        System.out.println(message);
                    }
                    catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        });

        listener.start();

        if(!listener.isAlive()){
            System.out.println("Завершение работы клиента");
        }
    }

    /**
     * Завершение работы всех потоков, закрытие клиентского сокета
     * @param socket клиентский сокет
     * @param bufferedReader буфер для чтения данных
     * @param bufferedWriter буфер для отправки данных
     */
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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

    private String compareString(int phase, String[] decomposeString){
        String message = decomposeString[phase];
        for (int i = phase + 1; i < decomposeString.length; i++) {
            message += decomposeString[i] + " ";
        }

        return message;
    }
}
