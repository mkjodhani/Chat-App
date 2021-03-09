package sample;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Authentication
{
    private static String SERVER_ADDR = Start.SERVER_ADDRESS;
    private  String user,password;
    private Socket socket = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Scanner input;
    private PrintWriter printWriter;
    private int Valid;
    private int Removed;
    public Authentication(String username,String password) throws IOException
    {
        this.Valid = -1;
        this.Removed = -1;
        this.user = username;
        this.password = password;
        socket = new Socket(SERVER_ADDR, 8080);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        input = new Scanner(inputStream, StandardCharsets.UTF_8);
        printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

    }
    public  int returnValid()
    {
        return  Valid;
    }
    private String makeServiceSignIn(String username,String password)
    {
        return "#-*-#--"+username+"--signIn--"+password;
    }
    private String makeServiceRemove(String username,String password)
    {
        return "#-*-#--"+username+"--delete--"+password;
    }
    public String passwordHash(String str)
    {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance("SHA-512");
            md5.update(str.getBytes());
            byte[] digestBytes = md5.digest();
            for (byte b:digestBytes)
            {
                stringBuilder.append(String.format("%02x",b));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }
        return  stringBuilder.toString();
    }

    public int getRemoved()
    {
        return Removed;
    }

    public void removeMember()
    {
        String message = makeServiceRemove(user, passwordHash(password));
        if (socket.isConnected())
        {
            printWriter.println(message);
            input.nextLine().trim();
            String incoming = input.nextLine().trim();
            if (incoming.equals("done"))
                Removed = 1;
            else if (incoming.equals("notexist"))
                Removed = 0;
            else
                Removed = -1;
        }
    }
    public void authenticate()
    {
        String message = makeServiceSignIn(user, passwordHash(password));
        if (socket.isConnected()) {
            printWriter.println(message);
            input.nextLine().trim();
            String incoming = input.nextLine().trim();
            if (incoming.equals("done"))
            {
                Valid = 1;
            }
            else
            {
                Valid = 0;
            }
        }
        else
        {
            Valid = -1;
        }
    }
}
