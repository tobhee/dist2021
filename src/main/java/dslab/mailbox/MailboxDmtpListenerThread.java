package dslab.mailbox;

import dslab.protocols.Mail;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class MailboxDmtpListenerThread implements Runnable {
    private Socket socket;

    private MailStorage mailStorage;

    public MailboxDmtpListenerThread(Socket socket, MailStorage mailStorage) {
        this.socket = socket;
        this.mailStorage = mailStorage;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            String request;
            boolean editing = false;
            Mail mail = null;
            writer.println("ok DMTP");
            // read client requests
            while ((request = reader.readLine()) != null) {
                System.out.println("Client sent the following request: " + request);
                /*
                    * check if request has the correct format: !ping
                    * <client-name>
                    */
                String[] parts = request.split("\\s");
                //=========dialogue-starts==========
                String response = "S: error command not found";
                if(request.equals("begin")) {
                    if(!editing) {
                        mail = new Mail();
                        response = "ok";
                        editing = true;
                    } else response = "error already editing a message";
                }
                if(request.equals("quit")) {
                    writer.println("ok bye");
                    break;
                }
                String noCommandReq = parts.length > 1 ? request.split("\\s", 2)[1] : null;
                if(parts.length == 2 && parts[0].equals("to")) {
                    if(editing) {
                        if(request.contains("@")) {
                            String[] recipientAddresses = parts[1].split(",");
                            StringBuilder unknownUser = new StringBuilder("");
                            for(String recipientAddress : recipientAddresses) {
                                String user = recipientAddress.split("@")[0];
                                if(!mailStorage.knowsUser(user)) unknownUser.append(user).append(" ");
                            }
                            if(unknownUser.toString().equals("")) {
                                mail.setTo(noCommandReq);
                                response = "ok " + noCommandReq.split(",").length;
                            } else response = "error unknown user " + unknownUser;
                        } else response = "error recipient mail address is not valid";
                    } else response = "error currently not editing a message";
                }
                if(parts.length == 2 && parts[0].equals("from")) {
                    if(editing) {
                        if(request.matches("\\S*\\s\\S*@\\S*")) {
                            mail.setFrom(noCommandReq);
                            response = "ok";
                        } else response = "error sender mail address is not valid";
                    } else response = "error currently not editing a message";
                }
                if(parts[0].equals("subject")) {
                    if(editing) {
                        mail.setSubject(noCommandReq);
                        response = "ok";
                    } else response = "error currently not editing a message";
                }
                if(parts[0].equals("data")) {
                    if(editing) {
                        mail.setData(noCommandReq);
                        response = "ok";
                    } else response = "error currently not editing a message";
                }
                if(request.equals("send")) {
                    if(editing) {
                        if(mail.getTo()!= null) {
                            if(mail.getFrom() != null) {
                                if(mail.getSubject() != null) {
                                    if(mail.getData() != null) {
                                        String[] recipientAddresses = mail.getTo().split(",");
                                        for(String recipientAddress : recipientAddresses) {
                                            mailStorage.addMail(recipientAddress.split("@")[0], mail);
                                        }
                                        System.out.println(mail);
                                        editing = false;
                                        mail = null;
                                        response = "ok";
                                    } else response = "error missing message body";
                                } else response = "error missing subject";
                            } else response = "error missing recipient";
                        } else response = "error missing address";
                    } else response = "error currently not editing a message";
                }
                //=========dialogue-ends==========
                System.out.println(mail);
                // print request
                writer.println(response);
            }
        } catch (SocketException e) {
            // when the socket is closed, the I/O methods of the Socket will throw a SocketException
            // almost all SocketException cases indicate that the socket was closed
            System.out.println("SocketException while handling socket: " + e.getMessage());
        } catch (IOException e) {
            // you should properly handle all other exceptions
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    reader.close();
                    writer.close();
                    socket.close();
                } catch (IOException e) {
                    // Ignored because we cannot handle it
                }
            }
        }
    }
}
