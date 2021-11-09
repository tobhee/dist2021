package dslab.monitoring;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.transfer.TransferControllerThread;
import dslab.util.Config;

public class MonitoringServer implements IMonitoringServer {

    private final Config config;
    private MonitoringListenerThread listener;
    private final Shell shell;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MonitoringServer(String componentId, Config config, InputStream in, PrintStream out) {
        this.config = config;
        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "-shell>");
    }

    @Override
    @Command
    public void run() {
        listener = new MonitoringListenerThread(config);
        new Thread(listener).start();
        // blocking main thread for shell
        shell.run();
    }

    @Override
    @Command
    public void addresses() {
        HashMap<String, Integer> addresses = listener.getAddresses();
        if (!addresses.isEmpty()) {
            for (Map.Entry<String, Integer> entry : addresses.entrySet()) {
                shell.out().println(entry.getKey() + " " + entry.getValue().toString());
/*                System.out.println(entry.getKey() + " " + entry.getValue().toString());*/
            }
        } else shell.out().println("no messages sent yet");

    }

    @Override
    @Command
    public void servers() {
        HashMap<String, Integer> servers = listener.getServers();
        for (Map.Entry<String, Integer> entry : servers.entrySet()) {
            shell.out().println(entry.getKey() + " " + entry.getValue());
        }
    }

    @Override
    @Command
    public void shutdown() {
        listener.shutdown();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMonitoringServer server = ComponentFactory.createMonitoringServer(args[0], System.in, System.out);
        server.run();
    }

}
