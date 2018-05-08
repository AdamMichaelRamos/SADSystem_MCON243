package com.touro.mcon243.sadsystem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(7777);
                Socket client = serverSocket.accept();
                while (true) System.out.println(client.getInputStream().read());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                Socket client = new Socket("localhost", 7777);
                while (true) client.getOutputStream().write(10);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
