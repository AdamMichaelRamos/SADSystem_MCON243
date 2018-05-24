package com.touro.mcon243.sadsystem;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by amram on 5/24/2018.
 */
public class SADClient implements Runnable {
    private String host;
    private int port;

    public SADClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket client = new Socket(this.host, this.port);
            while (true) client.getOutputStream().write(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
