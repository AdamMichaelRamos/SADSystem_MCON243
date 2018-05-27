package com.touro.mcon243.sadsystem;

import com.touro.mcon243.sadsystem.slave.SADSlave;
import com.touro.mcon243.sadsystem.slave.WorkGenerator;

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
