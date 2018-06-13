package com.touro.mcon243.sadslave;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by amram on 5/27/2018.
 *
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String name = args[0];
        int port = Integer.parseInt(args[1]);
        IWorkGenerator workGenerator = new WorkGenerator();

        ServerSocket server = new ServerSocket(port);
        System.out.println(String.format("%s| listening for connections on port: %s", name, port));

        Socket socket = server.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println(String.format("%s| Connection accepted", name));

        while (true) {
            System.out.println(String.format("%s| Waiting for new command", name));
            String commandFromMaster = reader.readLine();

            System.out.println(String.format("%s| Job received from master: %s", name, commandFromMaster));

            workGenerator.performWork();

            System.out.println(String.format("%s| Job 'complete': %s", name, commandFromMaster));

            writer.write(new StringBuilder(commandFromMaster).reverse().toString());
            writer.newLine();
            writer.flush();
        }
    }
}
