package com.company;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class Database
{
    private Connection  connection;
    public Database()
    {
        try
        {
            connection = null;
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS USERS(USERNAME TEXT,PASS TEXT);");
            //statement.executeUpdate("INSERT INTO USERS VALUES(1,\"mkjodhani\",\"mk\");");
            System.out.println("Opened database successfully");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public boolean logIn(String username,String password)
    {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet =  statement.executeQuery("SELECT PASS FROM USERS WHERE USERNAME = \""+username+"\";");
            if (resultSet.isClosed())
                return false;
            else if (resultSet.getString(1).trim().equals(passwordHash(password)))
                return  true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return  false;
        }
        return  false;
    }
    public ArrayList<String> getUsers()
    {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet =  statement.executeQuery("SELECT USERNAME FROM USERS;");
            while (resultSet.next())
            {
                arrayList.add(resultSet.getString(1));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return  arrayList;
    }
    public boolean addRecord(String username,String password)
    {
        try {
            Statement statement = connection.createStatement();
            if(!getUsers().contains(username))
            {
                statement.executeUpdate("INSERT INTO USERS VALUES(\""+username+"\",\""+passwordHash(password)+"\");");
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
    public  void printDatabase()
    {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet rs=statement.executeQuery("select * from USERS");
            while(rs.next())
            {
                System.out.println(rs.getString(1)+" "+rs.getString(2));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
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
