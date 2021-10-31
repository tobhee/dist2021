package dslab.mailbox;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import dslab.ComponentFactory;
import dslab.protocols.DmapProtocol;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {
    private String componentId;
    private Config config;
    private Socket socket;
    private InputStream in;
    private PrintStream out;
    private ConcurrentHashMap<Integer, String> messageMap;

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
        messageMap = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        try(
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String inputLine, outputLine;
            DmapProtocol dmap = new DmapProtocol();
            outputLine = dmap.processInput(null, this.config);
            out.println(outputLine);

            while((inputLine = in.readLine()) != null) {
                outputLine = dmap.processInput(inputLine, this.config);
                out.println(outputLine);
                if(outputLine.equals("ok bye")) break;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        // TODO
    }

    public static void main(String[] args) throws Exception {
        String mailboxDomain = args[0];
        int dmtpPort = Integer.parseInt(args[1]);
        int dmapPort = Integer.parseInt(args[2]);

        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        try(
                ServerSocket serverSocket = new ServerSocket(dmapPort);
        ) {
            while(true) {
                serverSocket.accept();
                (new Thread(server)).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + dmapPort);
            System.exit(-1);
        }
    }
}
