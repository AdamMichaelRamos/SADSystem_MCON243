package com.touro.mcon243.sadsystem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
            ArrayList<Socket> sadClients = new ArrayList<>();

            Thread clientAcceptorThread = new Thread(() -> {
                while (true)
                    try {
                        sadClients.add(serverSocket.accept());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });
            clientAcceptorThread.start();

            while (true) sadClients.forEach(client -> {
                try {
                    System.out.println(client.getInputStream().read());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
