package com.touro.mcon243.sadslave;

/**
 * Created by amram on 5/27/2018.
 */
public class Main {
    public static void main(String[] args) {
        SADSlave slave = new SADSlave(args[0], Integer.parseInt(args[1]), new WorkGenerator());
        slave.run();
    }
}
