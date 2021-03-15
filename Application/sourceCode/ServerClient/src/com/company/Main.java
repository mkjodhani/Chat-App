package com.company;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main
{
    public static void main(String[] args)
    {
        try(ServerSocket serverSocket = new ServerSocket(8080))
        {
            while (true)
            {
                Socket socket = serverSocket.accept();
                Runnable runnable = new ThreadedClass(socket);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
