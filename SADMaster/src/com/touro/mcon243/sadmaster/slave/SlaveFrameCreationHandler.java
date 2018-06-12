package com.touro.mcon243.sadmaster.slave;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by amram on 6/12/2018.
 */
public class SlaveFrameCreationHandler {
    private static final int MAX_CONNECTIONS = 2;
    private static SlaveFrameCreationHandler instance = null;

    private List<SlaveFrame> slaveFrames;

    private SlaveFrameCreationHandler() {
        this.slaveFrames = IntStream.range(0, SlaveFrameCreationHandler.MAX_CONNECTIONS).collect(
                ArrayList::new,
                (newSlaves, i) -> {
                    try {
                        newSlaves.add(
                                SlaveFrameCreationHandler.createSlaveConnectionFrame("slave" + i, "localhost", 1000 + i));
                    }
                    catch (IOException e) { e.printStackTrace(); }
                },
                ArrayList::addAll);
    }

    public Stream<SlaveFrame> getSlaveFrameStream() {
        return this.slaveFrames.stream();
    }

    private static SlaveFrame createSlaveConnectionFrame(String slaveName, String host, int port) throws IOException {
        Socket slaveSocket = new Socket(host, port);
        BufferedWriter slaveWriter1 = new BufferedWriter(new OutputStreamWriter(slaveSocket.getOutputStream()));
        BufferedReader slaveReader1 = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));
        return new SlaveFrame(slaveName, slaveReader1, slaveWriter1);
    }

    public static SlaveFrameCreationHandler getInstance() {
        if (SlaveFrameCreationHandler.instance == null) SlaveFrameCreationHandler.instance = new SlaveFrameCreationHandler();
        return SlaveFrameCreationHandler.instance;
    }
}
