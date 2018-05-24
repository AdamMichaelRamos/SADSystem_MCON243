package com.touro.mcon243.sadsystem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by amram/nfried on 5/24/2018.
 */
public class SADServer implements Runnable {
    private int port;

    public SADServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);
            Socket client = serverSocket.accept();
            while (true) System.out.println(client.getInputStream().read());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
