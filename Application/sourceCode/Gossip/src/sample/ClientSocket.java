package sample;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientSocket
{
    private static final Pattern pattern = Pattern.compile("^(--)(\\S+)(--)(\\S+)--");
    private static final Pattern  errorPattern = Pattern.compile("^(!!--!!)");
    private static final Pattern servicePattern = Pattern.compile("^#-\\*-#--(\\S+)--(\\S+)--");
    private static final Pattern filePattern = Pattern.compile("^\\*-\\*-\\*--(\\S+)--(\\S+)--");

    private static String SERVER_ADDR = Start.SERVER_ADDRESS;
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
            socket = new Socket(SERVER_ADDR,8080);
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
    public static String getServiceMessage(String string)
    {
        Matcher matcher = servicePattern.matcher(string);
        matcher.find();
        return string.replace(matcher.group(0),"");
    }
    public static String encoder(String from,String to,String str)
    {
        return "--"+from+"--"+to+"--"+str;
    }
    public static String decoder(String str)
    {
        String ans = str;
        Matcher matcher = pattern.matcher(str);
        if(matcher.find())
        {
            ans =  str.replaceFirst(matcher.group(0),"");
        }
        return ans;
    }
    public static String getTypeServiceResponce(String string)
    {
        Matcher matcher = servicePattern.matcher(string);
        matcher.find();
        return matcher.group(2);
    }
    public static String makeService(String string,String hostname)
    {
        return "#-*-#--"+hostname+"--"+string+"--";
    }
    public static String getErrorMessgae(String string)
    {
        Matcher matcher = errorPattern.matcher(string);
        matcher.find();
        return string.replace(matcher.group(1),"");
    }
    public static String getTypeMessgae(String string)
    {
        String name = "Echo";
        Matcher matcher = pattern.matcher(string);
        if(matcher.find())
            name = "Message";
        else if(errorPattern.matcher(string).find())
            name = "Error";
        else if(servicePattern.matcher(string).find())
            name = "Service";
        return name;
    }
    public static String getFromIndex(String string)
    {
        String name = null;
        Matcher matcher = pattern.matcher(string);
        if(matcher.find())
            name =  matcher.group(2);
        return  name;
    }
}
