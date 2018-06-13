package com.touro.mcon243.sadmaster;

import com.touro.mcon243.sadmaster.slave.SlaveFrame;
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
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.print("Master start...");

        SlaveFrameCreationHandler slaveConnections = SlaveFrameCreationHandler.getInstance();
        SlaveFrameInputHandler slaveInputHandler = SlaveFrameInputHandler.getInstance(slaveConnections.getSlaveFrameStream());

        final HashMap<String, ClientFrame> clientFrameMap = new HashMap<>();
        Queue<ClientMessage> clientMessageQueue = new LinkedList<>();

        Thread slaveJobDispatcherThread = new Thread(() ->{
            boolean foundIdleResource;
            while (true) {
                ClientMessage clientMessage;
                synchronized (clientMessageQueue) {
                    clientMessage = clientMessageQueue.peek();
                }

                foundIdleResource = clientMessage != null && SlaveFrameOutputHandler.dispatchMessageToIdleSlaveFrame(
                        slaveConnections.getSlaveFrameStream(),
                        clientMessage,
                        (writer, message) -> {
                            try {
                                System.out.print(String.format("Sending message (%s) to idle slave\n> ", clientMessage.message));
                                Main.sendInputLineToWriter(writer, message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                if (foundIdleResource) {
                    synchronized (clientMessageQueue) {
                        clientMessageQueue.poll();
                    }
                }
            }
        });
        slaveJobDispatcherThread.start();

        Thread completedSlaveJobsThread = new Thread(() -> {
            while (true) {
                slaveConnections.getSlaveFrameStream()
                        .filter(slaveFrame -> {
                            boolean accept;
                            synchronized (slaveFrame.status) {
                                accept = slaveFrame.isDone();
                            }
                            return accept;
                        })
                        .forEach(doneSlave -> {
                            try {
                                synchronized (doneSlave.status) {
                                    ClientFrame client = clientFrameMap.get(doneSlave.assignedClientId);
                                    System.out.print(String.format("respond to client: %s with job completed: %s\n> ",
                                            client.clientId, doneSlave.slaveInput));
                                    Main.sendInputLineToWriter(client.writer, doneSlave.slaveInput);
                                    doneSlave.clearData();
                                    doneSlave.setAsIdle();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                }
            }
        );
        completedSlaveJobsThread.start();

        System.out.print("\n> ");
        final List<Thread> clientInputReaderThreads = new ArrayList<>();
        Thread clientConnectionAcceptorThread = new Thread(() -> {
            try {
                int counter = 0;
                ServerSocket serverSocket = new ServerSocket(7777);
                while (true) {
                    Socket userConnection = serverSocket.accept();
                    ClientFrame clientFrame = new ClientFrame("client_"+counter++, userConnection);

                    synchronized (clientFrameMap) {
                        clientFrameMap.put(clientFrame.clientId, clientFrame);
                    }

                    Thread clientFrameInputReaderThread = new Thread(() -> {
                        String userInput;
                        try {
                            while (!(userInput = clientFrame.reader.readLine()).equalsIgnoreCase("exit")) {
                                System.out.print(String.format("client: %s sent input: %s, submitting job to queue\n> ",
                                        clientFrame.clientId, userInput));
                                clientMessageQueue.add(new ClientMessage(clientFrame.clientId, userInput));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    clientFrameInputReaderThread.start();
                    clientInputReaderThreads.add(clientFrameInputReaderThread);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        clientConnectionAcceptorThread.start();

        slaveJobDispatcherThread.join();
        completedSlaveJobsThread.join();
        clientConnectionAcceptorThread.join();
        slaveInputHandler.attemptToJoinAllThreads();
        clientInputReaderThreads.forEach(t -> {
            try { t.join(); }
            catch (InterruptedException e) { e.printStackTrace(); }
        });
    }

    private static void sendInputLineToWriter(BufferedWriter slaveWriter, String input) throws IOException {
        slaveWriter.write(input);
        slaveWriter.newLine();
        slaveWriter.flush();
    }

    private static class ClientFrame {
        private final String clientId;
        private final Socket clientSocket;
        private final BufferedReader reader;
        private final BufferedWriter writer;

        private ClientFrame(String clientId, Socket clientSocket) throws IOException {
            this.clientId = clientId;
            this.clientSocket = clientSocket;
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }

        private void tryToClose() {
            try { this.clientSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static class ClientMessage {
        public final String clientId, message;
        private ClientMessage(String clientId, String message) {
            this.clientId = clientId;
            this.message = message;
        }
    }
}
