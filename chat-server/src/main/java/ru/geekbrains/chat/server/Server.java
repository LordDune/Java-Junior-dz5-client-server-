package ru.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void runServer(){
        try {
            while (!serverSocket.isClosed()) {
                // метод accept переводит наш поток в режим ожидания (ожидания подключения нового сокета)
                Socket socket = serverSocket.accept(); // ?? посмотреть что за метод
                // Как только подключился новый клиент, создаем обертку в виде менеджера
                ClientManager clientManager = new ClientManager(socket);
                //System.out.println("Подключен новый клиент!");
                // Чтобы основной поток не был заблокирован, передаем менеджер (который имплементирует Runnable) в отдельный поток
                Thread thread = new Thread(clientManager);
                thread.start();
            }
        }
        catch (IOException e){
            // Если что то пошло не так, возникла ошибка, дальнешая работа с нашим сокетом невозможна, закрываем его
            closeSocket();
        }
    }

    private void closeSocket(){
        try{
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
