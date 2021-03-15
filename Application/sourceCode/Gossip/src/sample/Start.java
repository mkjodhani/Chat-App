package sample;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Start
{
    public static String SERVER_ADDRESS;

    static {
        try {
            SERVER_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        Main.main(null);
    }
}
