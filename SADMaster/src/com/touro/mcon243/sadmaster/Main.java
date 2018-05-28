package com.touro.mcon243.sadmaster;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Created by amram on 5/27/2018.
 */
public class Main {
    private static final String
            UPDATE = "update",
            OUTPUT = "output",
            WORKING = "working",
            IDLE = "idle",
            DEAD = "dead";
    private static final List<String> VALID_STATUSES = Arrays.asList(Main.WORKING, Main.IDLE);

    public static void main(String[] args) throws IOException {
        Socket slaveSocket1 = new Socket("localhost", 1000);
        BufferedWriter slaveWriter1 = new BufferedWriter(new OutputStreamWriter(slaveSocket1.getOutputStream()));
        BufferedReader slaveReader1 = new BufferedReader(new InputStreamReader(slaveSocket1.getInputStream()));

        Socket slaveSocket2 = new Socket("localhost", 1001);
        BufferedWriter slaveWriter2 = new BufferedWriter(new OutputStreamWriter(slaveSocket2.getOutputStream()));
        BufferedReader slaveReader2 = new BufferedReader(new InputStreamReader(slaveSocket2.getInputStream()));

        System.out.print("Master start...");

        SlaveFrame slave1Frame = new SlaveFrame() {{ this.status = Main.IDLE; this.reader = slaveReader1; }};
        Thread slave1StatusThread = new Thread(Main.createSlaveFrameHandler(slave1Frame));

        SlaveFrame slave2Frame = new SlaveFrame() {{ this.status = Main.IDLE; this.reader = slaveReader2; }};
        Thread slave2StatusThread = new Thread(Main.createSlaveFrameHandler(slave2Frame));

        slave1StatusThread.start();
        slave2StatusThread.start();

        String input;
        Scanner scanner = new Scanner(System.in);

        System.out.print("\n> ");
        while (!(input = scanner.nextLine()).equalsIgnoreCase("exit")) {
            System.out.print(String.format("User typed: %s\n> ", input));

            if (slave1Frame.status.equalsIgnoreCase(Main.IDLE))
                Main.sendCommandToSlave(slaveWriter1, input);
            else if (slave2Frame.status.equalsIgnoreCase(Main.IDLE))
                Main.sendCommandToSlave(slaveWriter2, input);
            else System.out.print("No resources available, dumping input\n> ");
        }
    }

    private static void sendCommandToSlave(BufferedWriter slaveWriter, String input) throws IOException {
        slaveWriter.write(input);
        slaveWriter.newLine();
        slaveWriter.flush();
    }

    private static Runnable createSlaveFrameHandler(SlaveFrame slaveFrame) {
        return () -> {
            try {
                while (true) {
                    String action = slaveFrame.reader.readLine(), output = slaveFrame.reader.readLine();

                    if (action.equalsIgnoreCase(Main.UPDATE))
                        slaveFrame.status = output;
                    else if (action.equalsIgnoreCase(Main.OUTPUT))
                        slaveFrame.outputQueue.add(output);

                    if (!Main.VALID_STATUSES.contains(slaveFrame.status)) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            slaveFrame.status = Main.DEAD;
        };
    }

    private static class SlaveFrame {
        String status;
        BufferedReader reader;
        Queue<String> outputQueue = new LinkedList<>();
    }
}
