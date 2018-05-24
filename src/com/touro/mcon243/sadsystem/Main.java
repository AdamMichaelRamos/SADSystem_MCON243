package com.touro.mcon243.sadsystem;

public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(new SADServer(7777));
        Thread clientThread = new Thread(new SADClient("localhost", 7777));

        serverThread.start();
        clientThread.start();

        try {
            serverThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
