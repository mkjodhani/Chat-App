package sample;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EncoderDecoder
{
    private static final Pattern pattern = Pattern.compile("^(--)(\\S+)(--)(\\S+)--");
    private static final Pattern  errorPattern = Pattern.compile("^(!!--!!)");
    private static final Pattern servicePattern = Pattern.compile("^#-\\*-#--(\\S+)--(\\S+)--");
    private static final Pattern filePattern = Pattern.compile("^\\*-\\*-\\*--(\\S+)--(\\S+)--");

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
