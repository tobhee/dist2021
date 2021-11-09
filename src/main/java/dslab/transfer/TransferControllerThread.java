package dslab.transfer;

import dslab.util.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ThreadPoolExecutor;

public class TransferControllerThread implements Runnable{
    private ServerSocket serverSocket;
    private final Config config;
    private TransferListenerThread listener;
    private boolean shutdown;

    public TransferControllerThread(Config config) {
        this.config = config;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(config.getInt("tcp.port"));
            System.out.println("TransferServer up and running");
            Socket socket;
            while(true) {
                socket = serverSocket.accept();
                if(shutdown) break;
                listener = new TransferListenerThread(socket, config);
                new Thread(listener).start();
            }
        } catch (IOException e) {
            System.out.println("TransferServerSocket shut down");
        } finally {

        }
    }

    public void shutdown() {
        if(serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("controller serversocket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        shutdown = true;
    }
}
