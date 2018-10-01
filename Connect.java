package com.chatapplication;
import java.net.*;
public class Connect extends Thread {
    public String ipString;
    public InetAddress addr;
    public void connect(Socket socket){
        System.out.println("Connecting to" + socket);

    }
    Connect(String ipString) throws SocketException{
        this.ipString = ipString;
        try {
            this.addr = InetAddress.getByName(ipString);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}