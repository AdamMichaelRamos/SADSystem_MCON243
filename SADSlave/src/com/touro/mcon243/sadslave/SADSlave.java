package com.touro.mcon243.sadslave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by amram/nfried on 5/27/2018.
 */
public class SADSlave implements Runnable {
    private String name;
    private int port;
    private IWorkGenerator workGenerator;

    public SADSlave(String name, int port, IWorkGenerator workGenerator) {
        this.name = name;
        this.port = port;
        this.workGenerator = workGenerator;
    }

    @Override
    public void run() {
        try {
            ICommandReader commandReader = new CommandReader(SADSlave.acceptConnectionFromMaster(this.name, this.port));
            while(true) SADSlave.receiveCommandAndPerformJob(this.name, commandReader, this.workGenerator);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static BufferedReader acceptConnectionFromMaster(String slaveName, int slavePort) throws IOException {
        ServerSocket serverSocket = new ServerSocket(slavePort);
        System.out.println(String.format("%s| listening for connections on port: %s", slaveName, slavePort));

        Socket socket = serverSocket.accept();
        System.out.println(String.format("%s| Connection accepted", slaveName));

        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static void receiveCommandAndPerformJob(String slaveName, ICommandReader commandReader, IWorkGenerator workGenerator)
            throws IOException, InterruptedException {
        System.out.println(String.format("%s| Waiting for new command", slaveName));
        String commandFromMaster = commandReader.receiveCommand();

        System.out.println(String.format("%s| Job received from master: %s", slaveName, commandFromMaster));

        workGenerator.performWork();

        System.out.println(String.format("%s| Job 'complete': %s", slaveName, commandFromMaster));
    }
}
