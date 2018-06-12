package com.touro.mcon243.sadclient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by amram on 6/12/2018.
 */
public class Main {
    public static void main(String[] ags) throws IOException {
        System.out.println("Client start...");

        int port = 7777;
        Socket connectionToMaster = new Socket("localhost", port);
        BufferedWriter outputToMaster = new BufferedWriter(new OutputStreamWriter(connectionToMaster.getOutputStream()));
        System.out.println(String.format("Successfully connected to master at port: %d", port));

        Scanner scanner = new Scanner(System.in);

        System.out.print("\n> ");

        String userInput;
        while (!(userInput = scanner.nextLine()).trim().equalsIgnoreCase("exit")) {
            System.out.print(String.format("Sending command: %s to master \n> ", userInput));
            outputToMaster.write(userInput);
            outputToMaster.newLine();
            outputToMaster.flush();
        }
    }
}
