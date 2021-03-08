package sample;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientSocket
{
    private ArrayList<String> peerClients;
    private final String hostname;
    private Socket socket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    PrintWriter printWriter =null;
    Scanner input = null;
    private Boolean serverUp ;

    public ArrayList<String> getPeerClients() {
        return peerClients;
    }

    public String getHostname() {
        return hostname;
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public ClientSocket(String user)
    {
        this.peerClients = new ArrayList<>();
        this.hostname = user;
        try
        {
            socket = new Socket(InetAddress.getLocalHost(),8080);
            if (socket.isConnected())
            {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                input = new Scanner(inputStream, StandardCharsets.UTF_8);
                printWriter = new PrintWriter(new OutputStreamWriter(outputStream,StandardCharsets.UTF_8),true);
                printWriter.println(makeService("echo"));
                serverUp = true;
            }
        }
        catch (IOException e)
        {
            serverUp = false;
        }
    }

    public Boolean getServerUp() {
        return serverUp;
    }

    private String makeService(String string)
    {
        return "#-*-#--"+hostname+"--"+string+"--";
    }

    public Scanner getInput() {
        return input;
    }

    public void sendData(String data)
    {
        printWriter.println(data);
    }
}
