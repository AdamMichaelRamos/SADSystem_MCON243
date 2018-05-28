package com.touro.mcon243.sadmaster;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Created by amram/nfried on 5/27/2018.
 */
public class Main {
    private static final String WORKING = "working", IDLE = "idle", DEAD = "dead";

    public static void main(String[] args) throws IOException {
        Socket slaveSocket1 = new Socket("localhost", 1000);
        BufferedWriter slaveWriter1 = new BufferedWriter(new OutputStreamWriter(slaveSocket1.getOutputStream()));
        BufferedReader slaveReader1 = new BufferedReader(new InputStreamReader(slaveSocket1.getInputStream()));

        Socket slaveSocket2 = new Socket("localhost", 1001);
        BufferedWriter slaveWriter2 = new BufferedWriter(new OutputStreamWriter(slaveSocket2.getOutputStream()));
        BufferedReader slaveReader2 = new BufferedReader(new InputStreamReader(slaveSocket2.getInputStream()));

        System.out.print("Master start...");

        SlaveFrame slave1Frame = new SlaveFrame("slave1", slaveReader1, slaveWriter1);
        Thread slave1StatusThread = new Thread(Main.createSlaveOutputHandler(slave1Frame));

        SlaveFrame slave2Frame = new SlaveFrame("slave2", slaveReader2, slaveWriter2);
        Thread slave2StatusThread = new Thread(Main.createSlaveOutputHandler(slave2Frame));

        slave1StatusThread.start();
        slave2StatusThread.start();

        Scanner scanner = new Scanner(System.in);

        System.out.print("\n> ");

        List<SlaveFrame> slaveFrames = Arrays.asList(slave1Frame, slave2Frame);
        final boolean[] foundIdleResource = {false};
        String[] userInput = new String[1];
        while (!(userInput[0] = scanner.nextLine()).equalsIgnoreCase("exit")) {
            slaveFrames.stream()
                    .filter(SlaveFrame::isIdle)
                    .findFirst()
                    .ifPresent(idleSlave -> {
                        foundIdleResource[0] = true;

                        synchronized (idleSlave.status) {
                            idleSlave.setAsWorking();
                        }

                        try {
                            Main.sendCommandToSlave(idleSlave.writer, userInput[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            if (!foundIdleResource[0])
                System.out.print(String.format("No resources available, dumping user input: %s\n> ", userInput[0]));
            foundIdleResource[0] = false;
        }
    }

    private static void sendCommandToSlave(BufferedWriter slaveWriter, String input) throws IOException {
        slaveWriter.write(input);
        slaveWriter.newLine();
        slaveWriter.flush();
    }

    private static Runnable createSlaveOutputHandler(SlaveFrame slaveFrame) {
        return () -> {
            try {
                while (slaveFrame.isAlive()) {
                    String slaveOutput = slaveFrame.reader.readLine();
                    synchronized (slaveFrame.status) {
                        slaveFrame.setAsIdle();
                    }
                    System.out.println(String.format("%s completed job: %s", slaveFrame.name, slaveOutput));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            slaveFrame.setDead();
            System.out.println(String.format("%s shutting down...", slaveFrame.name));
        };
    }

    private static class SlaveFrame {
        private final String[] status;
        private final String name;
        private final BufferedReader reader;
        private final BufferedWriter writer;

        private SlaveFrame(String name, BufferedReader reader, BufferedWriter writer) {
            this.status = new String[]{ Main.IDLE };
            this.name = name;
            this.reader = reader;
            this.writer = writer;
        }

        private boolean isIdle() {
            return this.status[0].equalsIgnoreCase(Main.IDLE);
        }

        private boolean isAlive() {
            return !this.status[0].equalsIgnoreCase(Main.DEAD);
        }

        private void setAsIdle() {
            this.status[0] = Main.IDLE;
        }

        private void setAsWorking() {
            this.status[0] = Main.WORKING;
        }

        private void setDead() {
            this.status[0] = Main.DEAD;
        }
    }
}
