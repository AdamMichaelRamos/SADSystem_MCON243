package com.touro.mcon243.sadsystem.slave;

import com.touro.mcon243.sadsystem.slave.IWorkGenerator;

import java.util.Random;

/**
 * Created by amram on 5/27/2018.
 */
public class WorkGenerator implements IWorkGenerator {
    private Random random = new Random();

    @Override
    public void performWork() throws InterruptedException {
        int randomWorkTime = this.random.nextInt(10000)+1000;

        System.out.println(String.format("work for %s's", randomWorkTime));
        Thread.sleep(randomWorkTime);
    }
}
