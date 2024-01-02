package ru.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.UnknownHostException;

public class Program {

    public static void main(String[] args) {
        try
        {
            ServerSocket serverSocket = new ServerSocket(1400);
            // Создание сервера
            Server server = new Server(serverSocket);
            // запуск сервера
            server.runServer();
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
