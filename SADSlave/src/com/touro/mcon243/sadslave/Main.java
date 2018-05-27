package com.touro.mcon243.sadslave;

/**
 * Created by amram on 5/27/2018.
 */
public class Main {
    public static void main(String[] args) {
        Thread slaveThread = new Thread(new SADSlave("slave1", 1000, new WorkGenerator()));
        slaveThread.start();

        try {
            slaveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
