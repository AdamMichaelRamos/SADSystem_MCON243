package com.touro.mcon243.sadmaster.client;

import com.touro.mcon243.sadmaster.Main;
import com.touro.mcon243.sadmaster.slave.SlaveFrame;

import java.io.BufferedWriter;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by amram on 6/14/2018.
 */
public class ClientFrameOutputHandler {
    private static ClientFrameOutputHandler instance;

    private Thread completedSlaveJobsThread;

    private ClientFrameOutputHandler(
            Supplier<Stream<SlaveFrame>> slaveStreamSupplier,
            Function<String, Main.ClientFrame> clientFrameFunction,
            BiFunction<BufferedWriter, String, Boolean> sendFinishedJobToClient) {

        this.completedSlaveJobsThread = new Thread(() -> {
            while (true) slaveStreamSupplier.get()
                    .filter(ClientFrameOutputHandler::slaveIsDone)
                    .forEach(ClientFrameOutputHandler.getDoneSlaveHandler(
                            clientFrameFunction,
                            sendFinishedJobToClient));

        });
    }

    public static ClientFrameOutputHandler getInstance(
            Supplier<Stream<SlaveFrame>> slaveStreamSupplier,
            Function<String, Main.ClientFrame> clientFrameFunction,
            BiFunction<BufferedWriter, String, Boolean> sendFinishedJobToClient) {

        if (ClientFrameOutputHandler.instance == null)
            ClientFrameOutputHandler.instance = new ClientFrameOutputHandler(
                    slaveStreamSupplier,
                    clientFrameFunction,
                    sendFinishedJobToClient);

        return ClientFrameOutputHandler.instance;
    }

    private static boolean slaveIsDone(SlaveFrame slaveFrame) {
        boolean accept;
        synchronized (slaveFrame.status) {
            accept = slaveFrame.isDone();
        }
        return accept;
    }

    private static Consumer<SlaveFrame> getDoneSlaveHandler(
            Function<String, Main.ClientFrame> clientFrameFunction,
            BiFunction<BufferedWriter, String, Boolean> sendFinishedJobToClient) {

        return doneSlave -> {
            synchronized (doneSlave.status) {
                Main.ClientFrame client = clientFrameFunction.apply(doneSlave.assignedClientId);
                if (sendFinishedJobToClient.apply(client.writer, doneSlave.slaveInput)) {
                    doneSlave.clearData();
                    doneSlave.setAsIdle();
                    System.out.print(String.format("respond to client: %s with job completed: %s\n> ",
                            client.clientId, doneSlave.slaveInput));
                }
            }
        };
    }

    public void start() {
        this.completedSlaveJobsThread.start();
    }

    public void tryToJoin() {
        try {
            this.completedSlaveJobsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
