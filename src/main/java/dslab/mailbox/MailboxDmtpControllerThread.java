package dslab.mailbox;

import dslab.util.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MailboxDmtpControllerThread implements Runnable{

    private MailStorage mailStorage;
    private final Config config;
    private ServerSocket serverSocketDmtp;
    private MailboxDmtpListenerThread dmtpListener;
    private boolean shutdown;

    public MailboxDmtpControllerThread(MailStorage mailStorage, Config config) {
        this.mailStorage = mailStorage;
        this.config = config;
    }

    @Override
    public void run() {
        // setup serversockets and accept clients
        try {
            serverSocketDmtp = new ServerSocket(config.getInt("dmtp.tcp.port"));
            System.out.println("MailboxDmtpServer up and running");
            Socket dmtpSocket;
            while(true) {
                dmtpSocket = serverSocketDmtp.accept();
                if(shutdown) break;
                dmtpListener = new MailboxDmtpListenerThread(dmtpSocket, mailStorage, config);
                new Thread(dmtpListener).start();
            }
        } catch (IOException e) {
            System.out.println("MailboxServerSocketDmtp shut down");
        }
    }

    public void shutdown() {
        try {
            if(serverSocketDmtp != null && !serverSocketDmtp.isClosed()) {
                serverSocketDmtp.close();
            }
        } catch(IOException ignored) {}
        shutdown = true;
    }
}
