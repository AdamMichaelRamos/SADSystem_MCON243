package com.touro.mcon243.sadmaster;

import com.touro.mcon243.sadmaster.client.ClientFrameOutputHandler;
import com.touro.mcon243.sadmaster.slave.SlaveFrameCreationHandler;
import com.touro.mcon243.sadmaster.slave.SlaveFrameInputHandler;
import com.touro.mcon243.sadmaster.slave.SlaveFrameOutputHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by amram/nfried on 5/27/2018.
 *
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.print("Master start...");

        final HashMap<String, ClientFrame> clientFrameMap = new HashMap<>();
        final Queue<ClientMessage> clientMessageQueue = new LinkedList<>();
        final SlaveFrameCreationHandler slaveConnections = SlaveFrameCreationHandler.getInstance();

        SlaveFrameInputHandler slaveInputHandler = SlaveFrameInputHandler.getInstance(slaveConnections.getSlaveFrameStream());
        SlaveFrameOutputHandler slaveFrameOutputHandler = Main.buildSlaveOutputHandler(slaveConnections, clientMessageQueue);
        ClientFrameOutputHandler clientFrameOutputHandler = ClientFrameOutputHandler.getInstance(
                slaveConnections::getSlaveFrameStream,
                clientFrameMap::get,
                Main::writeLineToWriterAndReturnSuccess);

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

        clientConnectionAcceptorThread.join();
        clientInputReaderThreads.forEach(t -> {
            try { t.join(); }
            catch (InterruptedException e) { e.printStackTrace(); }
        });
    }

    private static void sendInputLineToWriter(BufferedWriter writer, String input) throws IOException {
        writer.write(input);
        writer.newLine();
        writer.flush();
    }

    private static boolean writeLineToWriterAndReturnSuccess(BufferedWriter writer, String input) {
        try {
            Main.sendInputLineToWriter(writer, input);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static SlaveFrameOutputHandler buildSlaveOutputHandler(
            SlaveFrameCreationHandler slaveConnections,
            Queue<ClientMessage> clientMessageQueue) {

        Supplier<ClientMessage> messageSupplier = () -> {
            ClientMessage message;
            synchronized (clientMessageQueue) {
                message = clientMessageQueue.peek();
            }
            return message;
        };
        BiConsumer<BufferedWriter, String> masterMessageDispatcher = (writer, message) ->  {
            try {
                System.out.print(String.format("Sending message (%s) to idle slave\n> ", message));
                Main.sendInputLineToWriter(writer, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Consumer<Boolean> handleResultOfDispatch = success -> {
            if (success) {
                synchronized (clientMessageQueue) {
                    clientMessageQueue.poll();
                }
            }
        };
        return SlaveFrameOutputHandler.getInstance(
                messageSupplier,
                slaveConnections::getSlaveFrameStream,
                masterMessageDispatcher,
                handleResultOfDispatch);
    }

    public static class ClientFrame {
        public final String clientId;
        private final Socket clientSocket;
        private final BufferedReader reader;
        public final BufferedWriter writer;

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
