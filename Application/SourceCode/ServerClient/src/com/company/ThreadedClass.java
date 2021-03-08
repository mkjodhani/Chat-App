package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreadedClass implements Runnable
{
    private static Database database = new Database();
    private static final Pattern pattern = Pattern.compile("^(--)(\\S+)(--)(\\S+)(--)");
    private static final Pattern  errorPattern = Pattern.compile("^!!--!!");
    private static final Pattern servicePattern = Pattern.compile("^#-\\*-#--(\\S+)--(\\S+)--(\\S*)");
    private  String hostname;
    private static String clientsList = "";
    private static final HashMap<String,Socket> hashMap = new HashMap<>();
    Socket incomingSocket;
    Scanner scanner = new Scanner(System.in);
    InputStream inputStream = null;
    OutputStream outputStream = null;
    Scanner input = null;
    public static String generateClientList()
    {
        String list = "";
        for(String host : hashMap.keySet())
        {
            list += host + " ";
        }
        clientsList = list;
        return list;
    }
    public static void updateClients(String type,String username)
    {
        PrintWriter Writer = null;
        try {
            for(String user : hashMap.keySet())
            {
                if (user.equals(username))
                    continue;
                else
                {
                    System.out.println(makeServiceResponce(type, username));
                    Writer = new PrintWriter(new OutputStreamWriter(hashMap.get(user).getOutputStream(), StandardCharsets.UTF_8), true);
                    Writer.println(makeServiceResponce(type, username));
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public ThreadedClass(Socket incomingSocket)
    {

        this.incomingSocket = incomingSocket;
        input = null;
        PrintWriter printWriter =null;
        try
        {
            inputStream = incomingSocket.getInputStream();
            outputStream = incomingSocket.getOutputStream();
            input = new Scanner(inputStream,StandardCharsets.UTF_8);
            printWriter = new PrintWriter(new OutputStreamWriter(outputStream,StandardCharsets.UTF_8),true);
            printWriter.println(makeServiceResponce("Echo",""));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    while (incomingSocket.isConnected())
                    {
                        String line = input.nextLine(),list = null;
                        System.out.println(line);
                        switch (getTypeMessgae(line))
                        {
                            case "Service":
                                switch (getServiceType(line))
                                {
                                    case "Close":
                                        hashMap.remove(getServiceClient(line));
                                        updateClients("removeMember",hostname);
                                        System.out.println("Remove Member!");
                                        generateClientList();
                                        break;
                                    case "clientList":
                                        list = generateClientList();
                                        Socket t = hashMap.get(getServiceClient(line));
                                        PrintWriter Other = new PrintWriter(new OutputStreamWriter(t.getOutputStream(), StandardCharsets.UTF_8), true);
                                        Other.println(makeServiceResponce("clientList",list));
                                        System.out.println("List:"+ThreadedClass.clientsList);
                                        break;
                                    case "Echo":
                                        list = generateClientList();
                                        hostname = getHostName(line);
                                        updateClients("addMember",hostname);
                                        hashMap.put(hostname,incomingSocket);
                                        System.out.println("Echo : #" + hostname);
                                        ThreadedClass.clientsList += hostname + " ";
                                        System.out.println(ThreadedClass.clientsList);
                                        PrintWriter other = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                                        other.println(makeServiceResponce("clientList", list));
                                        break;
                                    case "signUp":
                                        String username = getHostName(line);
                                        String password = getHostPassword(line);
                                        System.out.println("signUp : #" + username);
                                        System.out.println("signUp : #" + password);
                                        database.addRecord(username,password);
                                        PrintWriter newWriter = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                                        newWriter.println("done");
                                        break;
                                    case "signIn":
                                        String user = getHostName(line);
                                        String pass = getHostPassword(line);
                                        System.out.println("signIn : #" + user);
                                        System.out.println("signIn : #" + pass);
                                        PrintWriter oldWriter = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                                        if(database.logIn(user,pass))
                                            oldWriter.println("done");
                                        else
                                            oldWriter.println("error");
                                        break;
                                    default:
                                        System.out.println("New Service Appear..!");
                                }
                                break;
                            case "Message":
                                System.out.println(hashMap.get(getToIndex(line)));
                                PrintWriter printWriter = null;
                                try
                                {
                                    Socket s = hashMap.get(getToIndex(line));
                                    if (s == null)
                                    {
                                        s = hashMap.get(getFromIndex(line));
                                        System.out.println("USER-ERROR/"+getToIndex(line));
                                        printWriter = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                                        printWriter.println(getError(getToIndex(line)+" is not logged in at this moment!"));
                                    }
                                    else
                                    {
                                        printWriter = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                                        printWriter.println(line);
                                    }
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                System.out.println("Something different happened this time...!::::"+line);
                                break;
                        }
                    }
                }
                catch (NoSuchElementException e)
                {
                    System.out.println("Logged Out:"+hostname);
                    hashMap.remove(hostname);
                    updateClients("removeMember",hostname);
                    System.out.println("Remove Member!");
                    generateClientList();
                }
                catch (IOException ioException)
                {
                    try {
                        incomingSocket.close();
                    } catch (IOException e)
                    {
                    }
                }
            }
        }).start();
    }
    private static String getToIndex(String string)
    {
        String name = null;
        Matcher matcher = pattern.matcher(string);
        if(matcher.find())
        {
            name =  matcher.group(4);
        }
        return  name;
    }
    private static String getFromIndex(String string)
    {
        String name = null;
        Matcher matcher = pattern.matcher(string);
        if(matcher.find())
        {
            name =  matcher.group(2);
        }
        return  name;
    }
    private static String getError(String string)
    {
        return "!!--!!" + string;
    }
    private static String getTypeMessgae(String string)
    {
        String name = "Other";
        Matcher matcher = pattern.matcher(string);
        if(matcher.find())
            name = "Message";
        else if(errorPattern.matcher(string).find())
            name = "Error";
        else if(servicePattern.matcher(string).find())
            name = "Service";
        return name;
    }
    private static String getServiceMessgae(String string)
    {
        Matcher matcher = servicePattern.matcher(string);
        matcher.find();
        return string.replace(matcher.group(0),"");
    }
    private static String getServiceClient(String string)
    {
        Matcher matcher = servicePattern.matcher(string);
        matcher.find();
        return matcher.group(1);
    }
    private static String getServiceType(String string)
    {
        Matcher matcher = servicePattern.matcher(string);
        matcher.find();
        return matcher.group(2);
    }
    private static String makeServiceResponce(String name, String responce)
    {
        return "#-*-#--"+"Server"+"--"+name+"--"+responce;
    }
    private static String getHostName(String line)
    {
        Matcher matcher = servicePattern.matcher(line);
        matcher.find();
        return matcher.group(1);
    }
    private static String getHostPassword(String line)
    {
        Matcher matcher = servicePattern.matcher(line);
        matcher.find();
        return matcher.group(3);
    }
}
