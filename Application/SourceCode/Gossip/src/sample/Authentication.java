package sample;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Authentication
{
    private Socket socket = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Scanner input;
    private PrintWriter printWriter;
    private int Valid;
    public Authentication(String username,String password) throws IOException {
        String message = makeServiceSignIn(username, passwordHash(password));
        socket = new Socket(InetAddress.getLocalHost(), 8080);
        if (socket.isConnected()) {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            input = new Scanner(inputStream, StandardCharsets.UTF_8);
            printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
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
    public  int returnValid()
    {
        return  Valid;
    }
    private String makeServiceSignIn(String username,String password)
    {
        return "#-*-#--"+username+"--signIn--"+password;
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
}
