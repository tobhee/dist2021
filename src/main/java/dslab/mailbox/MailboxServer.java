package dslab.mailbox;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;

import dslab.ComponentFactory;
import dslab.monitoring.dmap.MailboxListenerThread;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {
    private String componentId;
    private Config config;
    private InputStream in;
    private PrintStream out;
    private ServerSocket serverSocket;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {
        // TODO
        this.componentId = componentId;
        this.config = config;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(config.getInt("dmap.tcp.port"));
            new MailboxListenerThread(serverSocket, config).start();
        } catch (IOException e) {
            throw new UncheckedIOException("Error while creating server socket", e);
        }


        try {
            Thread.sleep(5000000);
        } catch (InterruptedException e) {
            shutdown();
        }
        //continue only if listener thread closes (request="quit"); maybe works with interrupt???
        shutdown();
    }

    @Override
    public void shutdown() {
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch(IOException e) {
                System.err.println("Error while closing server socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
