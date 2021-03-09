package sample;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Start
{
    public static String SERVER_ADDRESS;

    static {
        try {
            SERVER_ADDRESS = InetAddress.getLocalHost().getHostAddress();
            SERVER_ADDRESS = "18.222.214.51";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        Main.main(null);
    }
}
