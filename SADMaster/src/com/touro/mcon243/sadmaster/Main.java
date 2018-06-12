package com.touro.mcon243.sadmaster;

import com.touro.mcon243.sadmaster.slave.SlaveFrameCreationHandler;
import com.touro.mcon243.sadmaster.slave.SlaveFrameInputHandler;
import com.touro.mcon243.sadmaster.slave.SlaveFrameOutputHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by amram/nfried on 5/27/2018.
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.print("Master start...");

        SlaveFrameCreationHandler slaveConnections = SlaveFrameCreationHandler.getInstance();
        SlaveFrameInputHandler slaveInputHandler = SlaveFrameInputHandler.getInstance(slaveConnections.getSlaveFrameStream());

        ServerSocket serverSocket = new ServerSocket(7777);
        Socket userConnection = serverSocket.accept();
        BufferedReader userInputReader = new BufferedReader(new InputStreamReader(userConnection.getInputStream()));

        System.out.print("\n> ");

        boolean foundIdleResource;
        String userInput;
        while (!(userInput = userInputReader.readLine()).equalsIgnoreCase("exit")) {
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
