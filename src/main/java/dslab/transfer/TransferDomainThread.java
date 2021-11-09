package dslab.transfer;

import dslab.protocols.Mail;
import dslab.util.Config;

import java.io.*;
import java.net.*;
import java.util.Arrays;

//client
public class TransferDomainThread implements Runnable {

    private final Mail mail;
    private final Config domainsConfig = new Config("domains");
    private Socket socket;
    private final Config config;

    public TransferDomainThread(Mail mail, Config config) {
        this.mail = mail;
        this.config = config;
    }

    @Override
    public void run() {
        try {
            String[] recipients = mail.getTo().split(",");
            // new array that beholds all unique mailserver addresses that the email shall be sent to
            String[] recipientServers = new String[recipients.length];
            for(int i=0; i< recipients.length; i++) {
                recipientServers[i] = recipients[i].split("@")[1];
            }
            String[] uniqueRecipientServerDomains = Arrays.stream(recipientServers).distinct().toArray(String[]::new);

            for(String recipientServerDomain : uniqueRecipientServerDomains) {
                //domain name lookup
                System.out.println("recipient servername: " + recipientServerDomain);
                if(domainsConfig.containsKey(recipientServerDomain)) {
                    System.out.println("trying to send error mail to sender");
                    lookupDomain(recipientServerDomain);
                } else {
                    // if domain lookup failed, notify sender via mail
                    sendErrorMail("mailserver " + recipientServerDomain + " not found");
                }

                if(socket != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                    notifyMonitoringServer();

                    writer.println("begin");
                    String response = reader.readLine();
                    writer.println("from " + mail.getFrom());
                    response += reader.readLine();
                    writer.println("to " + mail.getTo());
                    response += reader.readLine();
                    writer.println("subject " + mail.getSubject());
                    response += reader.readLine();
                    writer.println("data " + mail.getData());
                    response += reader.readLine();
                    writer.println("send");
                    response += reader.readLine();
                    System.out.println("mailbox server response: " + response);
                    // react to server response message
                    if(response.contains("error")) {
                        System.out.println("error mailserver did not accept mail; use protocol dmtp");
                        sendErrorMail("Mailboxserver " + recipientServerDomain + " did not accept your mail");
                    }
                    reader.close();
                    writer.close();
                } else sendErrorMail("Mailboxserver " + recipientServerDomain + " not available");
            }



        } catch (UnknownHostException e) {
            System.out.println("Cannot connect to host: " + e.getMessage());
        } catch (SocketException e) {
            System.out.println("SocketException while handling socket: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void lookupDomain(String domain) throws IOException {
        String[] ipAndPort = domainsConfig.getString(domain).split(":");
        socket = new Socket(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
    }

    private void sendErrorMail(String errorMessage) throws IOException {
        String senderServerDomain = mail.getFrom().split("@")[1];
        if(domainsConfig.containsKey(senderServerDomain)) {
            Mail errorMail = new Mail();
            errorMail.setFrom("mailer" + config.getString("monitoring.host"));
            errorMail.setTo(mail.getFrom());
            errorMail.setSubject("error");
            errorMail.setData(errorMessage);
            System.out.println(errorMail);
            lookupDomain(errorMail.getTo().split("@")[1]);
        } else {
            System.out.println("mail address of sender does not exist");
        }
    }

    void notifyMonitoringServer() {
        DatagramSocket socket;
        byte[] buffer;
        DatagramPacket packet;
        String message = "127.0.0.1:" + config.getString("tcp.port") + " " + mail.getFrom();

        try {
            socket = new DatagramSocket();
            buffer = message.getBytes();
            packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName(config.getString("monitoring.host")),
                    config.getInt("monitoring.port"));

            socket.send(packet);

        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
