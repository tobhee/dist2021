package dslab.monitoring;

import dslab.util.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;


public class MonitoringListenerThread implements Runnable {

    private boolean shutdown;
    private DatagramSocket datagramSocket;
    private final Config config;

    private HashMap<String, Integer> addresses;
    private HashMap<String, Integer> servers;

    public MonitoringListenerThread(Config config) {
        this.config = config;
        addresses = new HashMap<>();
        servers = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket(config.getInt("udp.port"));
            byte[] buffer;
            DatagramPacket packet;
            while(!shutdown) {
                buffer = new byte[1024];
                packet = new DatagramPacket(buffer, buffer.length);

                datagramSocket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength());

                System.out.println("Received request-packet from client: " + request);
                String[] parts = request.split("\\s");
                if(request.matches("\\S*:\\d* \\S*")) {
                    String transferServerIpPort = request.split(" ")[0];
                    String emailAddress = request.split(" ")[1];
                    if(addresses.containsKey(emailAddress)) {
                        addresses.put(emailAddress, addresses.get(emailAddress) + 1);
                    } else {
                        addresses.put(emailAddress, 1);
                    }
                    if(servers.containsKey(transferServerIpPort)) {
                        servers.put(transferServerIpPort, servers.get(transferServerIpPort) + 1);
                    } else {
                        servers.put(transferServerIpPort, 1);
                    }
                } else {
                    System.out.println("UDP packet has wrong format");
                }

            }
        } catch (IOException e) {
            System.out.println("DatagramSocket shut down");
        } finally {
            if(datagramSocket != null) datagramSocket.close();
        }
    }

    public void shutdown() {
        shutdown = true;
        if(datagramSocket != null) {
            datagramSocket.close();
        }
    }

    public HashMap<String, Integer> getAddresses() {
        return addresses;
    }

    public HashMap<String, Integer> getServers() {
        return servers;
    }
}
