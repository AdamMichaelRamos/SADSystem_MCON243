package com.touro.mcon243.sadmaster;

import com.touro.mcon243.sadmaster.slave.SlaveFrame;
import com.touro.mcon243.sadmaster.slave.SlaveFrameCreationHandler;
import com.touro.mcon243.sadmaster.slave.SlaveFrameInputHandler;
import com.touro.mcon243.sadmaster.slave.SlaveFrameOutputHandler;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by amram/nfried on 5/27/2018.
 */
public class Main {
    private static final String WORKING = "working", IDLE = "idle", DEAD = "dead";

    public static void main(String[] args) throws IOException {
        System.out.print("Master start...");

        SlaveFrameCreationHandler slaveConnections = SlaveFrameCreationHandler.getInstance();
        SlaveFrameInputHandler slaveInputHandler = SlaveFrameInputHandler.getInstance(slaveConnections.getSlaveFrameStream());

        Scanner scanner = new Scanner(System.in);

        System.out.print("\n> ");

        boolean foundIdleResource;
        String userInput;
        while (!(userInput = scanner.nextLine()).equalsIgnoreCase("exit")) {
            foundIdleResource = SlaveFrameOutputHandler.dispatchMessageToIdleSlaveFrame(
                    slaveConnections.getSlaveFrameStream(),
                    userInput,
                    (writer, message) -> {
                        try {
                            Main.sendCommandToSlave(writer, message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            if (!foundIdleResource)
                System.out.print(String.format("No resources available, dumping user input: %s\n> ", userInput));
        }

        slaveInputHandler.attemptToJoinAllThreads();
    }

    private static void sendCommandToSlave(BufferedWriter slaveWriter, String input) throws IOException {
        slaveWriter.write(input);
        slaveWriter.newLine();
        slaveWriter.flush();
    }
}
