package com.touro.mcon243.sadmaster.slave;

import com.touro.mcon243.sadmaster.Main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Created by amram on 6/12/2018.
 */
public class SlaveFrameOutputHandler {
    public static boolean dispatchMessageToIdleSlaveFrame(
            Stream<SlaveFrame> slaveFrameStream,
            String message,
            BiConsumer<BufferedWriter, String> messageDispatcher) {

        Optional<SlaveFrame> idleSlave = slaveFrameStream.filter(SlaveFrame::isIdle).findFirst();

        idleSlave.ifPresent(slave -> {
            synchronized (slave.status) {
                slave.setAsWorking();
            }
            messageDispatcher.accept(slave.writer, message);
        });

        return idleSlave.isPresent();
    }
}
