package com.touro.mcon243.sadmaster.slave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by amram on 6/12/2018.
 *
 */
public class SlaveFrameInputHandler {
    private static SlaveFrameInputHandler instance = null;

    private List<Thread> slaveFrameInputThreads;

    private SlaveFrameInputHandler(Stream<SlaveFrame> slaveFrameStream) {
        this.slaveFrameInputThreads = slaveFrameStream.collect(
                ArrayList::new,
                (newThreads, slaveFrame) -> newThreads.add(
                        new Thread(SlaveFrameInputHandler.createSlaveInputHandler(slaveFrame))),
                ArrayList::addAll);

        this.slaveFrameInputThreads.forEach(Thread::start);
    }

    public void attemptToJoinAllThreads() {
        this.slaveFrameInputThreads.forEach(t -> {
            try { t.join(); }
            catch (InterruptedException e) { e.printStackTrace(); }
        });
    }

    private static Runnable createSlaveInputHandler(SlaveFrame slaveFrame) {
        return () -> {
            try {
                while (slaveFrame.isAlive()) {
                    String slaveInput = slaveFrame.reader.readLine();
                    synchronized (slaveFrame.status) {
                        slaveFrame.setAsDone();
                        slaveFrame.slaveInput = slaveInput;
                    }
                    System.out.println(String.format("%s completed job: %s", slaveFrame.name, slaveInput));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            slaveFrame.setDead();
            System.out.println(String.format("%s shutting down...", slaveFrame.name));

            slaveFrame.tryToClose();
        };
    }

    public static SlaveFrameInputHandler getInstance(Stream<SlaveFrame> slaveFrameStream) {
        if (SlaveFrameInputHandler.instance == null)
            SlaveFrameInputHandler.instance = new SlaveFrameInputHandler(slaveFrameStream);

        return SlaveFrameInputHandler.instance;
    }
}
