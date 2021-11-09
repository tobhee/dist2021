package dslab.mailbox;

import java.io.*;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {
    private Config config;
    private InputStream in;
    private MailStorage mailStorage;
    private MailboxDmtpControllerThread controllerDmtp;
    private MailboxDmapControllerThread controllerDmap;
    private final Shell shell;

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
        this.config = config;
        this.in = in;
        this.mailStorage = new MailStorage(config);
        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "-shell> ");
    }

    @Override
    public void run() {
        controllerDmtp = new MailboxDmtpControllerThread(mailStorage, config);
        new Thread(controllerDmtp).start();
        controllerDmap = new MailboxDmapControllerThread(mailStorage, config);
        new Thread(controllerDmap).start();

        // blocking main thread for shell
        shell.run();
    }

    @Command
    @Override
    public void shutdown() {
        controllerDmtp.shutdown();
        controllerDmap.shutdown();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
