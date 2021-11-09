package dslab.transfer;

import dslab.protocols.Mail;
import dslab.util.Config;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TransferListenerThread implements Runnable {

    private final Socket socket;
    private final Config config;
    private ThreadPoolExecutor workerThreads;

    public TransferListenerThread(Socket socket, Config config) {
        this.socket = socket;
        this.config = config;
        this.workerThreads = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

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
                    socket.close();
                }
                String noCommandReq = parts.length > 1 ? request.split("\\s", 2)[1] : null;
                if(parts.length > 1 && parts[0].equals("to")) {
                    if(editing) {
                        if(request.contains("@")) {
                            mail.setTo(noCommandReq);
                            response = "ok " + noCommandReq.split(",").length;
                        } else response = "error recipient mail address is not valid";
                    } else response = "error currently not editing a message";
                }
                if(parts.length == 2 && parts[0].equals("from")) {
                    if(editing) {
                        if(request.contains("@")) {
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
                                        TransferDomainThread send = new TransferDomainThread(mail, config);
                                        workerThreads.execute(send);
                                        System.out.println(mail);
                                        editing = false;
                                        mail = null;
                                        response = "ok";
                                    } else response = "error missing message body";
                                } else response = "error missing subject";
                            } else response = "error missing recipient";
                        } else response = "error missing sender";
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
                    if(workerThreads != null) workerThreads.shutdownNow();
                    System.out.println("listener reader, writer, socket, threadpool closed");
                } catch (IOException e) {
                    // Ignored because we cannot handle it
                }
            }
        }
    }
}
