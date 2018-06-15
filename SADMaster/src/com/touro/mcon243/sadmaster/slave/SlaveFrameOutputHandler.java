package com.touro.mcon243.sadmaster.slave;

import com.touro.mcon243.sadmaster.Main;

import java.io.BufferedWriter;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by amram on 6/12/2018.
 *
 */
public class SlaveFrameOutputHandler {
    private static SlaveFrameOutputHandler instance;
    private Thread slaveJobDispatcherThread;

    private SlaveFrameOutputHandler(
            Supplier<Main.ClientMessage> messageSupplier,
            Supplier<Stream<SlaveFrame>> slaveStreamSupplier,
            BiConsumer<BufferedWriter, String> masterMessageDispatcher,
            Consumer<Boolean> resultOfDispatch) {

        this.slaveJobDispatcherThread = new Thread(() ->{
            boolean foundIdleResource;
            while (true) {
                foundIdleResource = SlaveFrameOutputHandler.dispatchMessageToIdleSlaveFrame(
                        slaveStreamSupplier.get(),
                        messageSupplier.get(),
                        masterMessageDispatcher);

                resultOfDispatch.accept(foundIdleResource);
            }
        });
    }

    public static SlaveFrameOutputHandler getInstance(
            Supplier<Main.ClientMessage> messageSupplier,
            Supplier<Stream<SlaveFrame>> slaveStreamSupplier,
            BiConsumer<BufferedWriter, String> masterMessageDispatcher,
            Consumer<Boolean> resultOfDispatch) {

        if (SlaveFrameOutputHandler.instance == null)
            SlaveFrameOutputHandler.instance = new SlaveFrameOutputHandler(
                    messageSupplier,
                    slaveStreamSupplier,
                    masterMessageDispatcher,
                    resultOfDispatch);
        return SlaveFrameOutputHandler.instance;
    }

    public void start() {
        this.slaveJobDispatcherThread.start();
    }

    public void tryToJoin() {
        try {
            this.slaveJobDispatcherThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean dispatchMessageToIdleSlaveFrame(
            Stream<SlaveFrame> slaveFrameStream,
            Main.ClientMessage clientMessage,
            BiConsumer<BufferedWriter, String> messageDispatcher) {

        if (clientMessage == null) return false;

        Optional<SlaveFrame> idleSlave = slaveFrameStream.filter(SlaveFrame::isIdle).findFirst();

        idleSlave.ifPresent(slave -> {
            synchronized (slave.status) {
                slave.setAsWorking();
                slave.assignedClientId = clientMessage.clientId;
            }
            messageDispatcher.accept(slave.writer, clientMessage.message);
        });
        return idleSlave.isPresent();
    }
}
