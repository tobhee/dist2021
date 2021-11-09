package dslab.mailbox;

import dslab.util.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MailboxDmapControllerThread implements Runnable{

    private MailStorage mailStorage;
    private final Config config;
    private ServerSocket serverSocketDmap;
    private MailboxDmapListenerThread dmapListener;
    private boolean shutdown;

    public MailboxDmapControllerThread(MailStorage mailStorage, Config config) {
        this.mailStorage = mailStorage;
        this.config = config;
    }

    @Override
    public void run() {
        // setup serversockets and accept clients
        try {
            serverSocketDmap = new ServerSocket(config.getInt("dmap.tcp.port"));
            System.out.println("MailboxDmapServer up and running");
            Socket dmapSocket;

            while(true) {
                dmapSocket = serverSocketDmap.accept();
                if(shutdown) break;
                dmapListener = new MailboxDmapListenerThread(dmapSocket, config, mailStorage);
                new Thread(dmapListener).start();
            }
        } catch (IOException e) {
            System.out.println("MailboxServerSocketDmap shut down");
        }
    }

    public void shutdown() {
        try {
            if(serverSocketDmap != null && !serverSocketDmap.isClosed()) {
                serverSocketDmap.close();
            }
        } catch(IOException ignored) {}
        shutdown = true;
    }
}
