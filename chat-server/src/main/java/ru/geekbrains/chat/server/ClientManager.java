package ru.geekbrains.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

// Имплементируем интерфейс для запуска в режиме многопоточности
public class ClientManager implements Runnable {

    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Первое сообщение, которые мы получаем от клиента - его имя
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage(name + " подключился к чату.", "Server");
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String massageFromClient;

        while (socket.isConnected()) {
            try {
                // метод readLine будет в режиме ожидания до тех пор пока от клиента не придет сообщение
                massageFromClient = bufferedReader.readLine();
                /*if (massageFromClient == null){
                    // для  macOS
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }*/
                sendMessage(massageFromClient);
            }
            catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void sendMessage(String massageFromClient) {
        String[] messageArray = massageFromClient.split(" ");
        if (messageArray[0].startsWith("@")) {
            String nameAddress = messageArray[0].substring(1);
            toClientMessage(massageFromClient, nameAddress);
        }
        else {
            broadcastMessage(massageFromClient, name);
        }
    }

    private void toClientMessage(String massageFromClient, String nameAddress) {
        for (ClientManager client: clients) {
            if (client.getName().equals(nameAddress)) {
                try {
                    // if (!client.name.equals(name)) {
                    client.bufferedWriter.write(name + ": " + massageFromClient);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                    //}
                }
                catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }
    }

    private void broadcastMessage(String message, String nameCast){
        for (ClientManager client: clients) {
            try {
                // if (!client.name.equals(name)) {
                if (message.length() > 0) {
                    client.bufferedWriter.write(nameCast + ": " + message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
                //}
            }
            catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }


    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient(){
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage(name + " покинул чат.", "Server");
    }

    public String getName() {
        return name;
    }
}
