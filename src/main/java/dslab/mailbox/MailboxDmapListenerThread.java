package dslab.mailbox;

import dslab.util.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class MailboxDmapListenerThread implements Runnable {

    private final Socket socket;
    private final Config userConfig;
    private final MailStorage mailStorage;

    public MailboxDmapListenerThread(Socket socket, Config config, MailStorage mailStorage) {
        this.socket = socket;
        this.userConfig = new Config(config.getString("users.config"));
        this.mailStorage = mailStorage;
    }



    @Override
    public void run() {
        int LOGGED_OUT = 0;
        int LOGGED_IN = 1;
        int state = LOGGED_OUT;
        String user = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            //System.out.println(reader.readLine());
            String request, response;
            writer.println("ok DMAP");
            while((request = reader.readLine()) != null) {
                System.out.println(request);

                // ------ dialogue start ---------
                response = "error unknown command";

                String[] words = request.split(" ");
                if(request.equals("quit")) {
                    writer.println("ok bye");
                    break;
                }

                if(state == LOGGED_OUT) {
                    if(words[0].equals("login") && words.length == 3) {
                        if(userConfig.containsKey(words[1])) {
                            if(userConfig.getString(words[1]).equals(words[2])) {
                                state = LOGGED_IN;
                                user = words[1];
                                response = "ok";
                            } else response = "error wrong password";
                        }  else response = "error unknown user";
                    }
                }

                if(request.equals("logout")) {
                    if(state == LOGGED_IN) {
                        state = LOGGED_OUT;
                        user = null;
                        response = "ok";
                    } else response = "error not logged in";
                }
                if(request.equals("login")) {
                    if(state == LOGGED_IN) {
                        response = "error already logged in";
                    }
                }

                if(words[0].equals("delete") && words.length == 2) {
                    if(state == LOGGED_IN) {
                        mailStorage.deleteMail(user, Integer.parseInt(words[1]));
                    } else response = "error not logged in";
                }

                if(words[0].equals("show") && words.length == 2 && words[1].matches("\\d+")) {
                    if(state == LOGGED_IN) {
                        if(mailStorage.showMail(user, Integer.parseInt(words[1])) != null) {
                            response = mailStorage.showMail(user, Integer.parseInt(words[1])).toString();
                        } else {
                            response = "mail with id " + words[1] + " does not exist";
                        }
                    } else response = "error not logged in";
                }

                if(request.equals("list")) {
                    response = mailStorage.listMailsOfUser(user);
                }
                // ------ dialogue end ---------

                writer.println(response);
            }
        } catch(SocketException e) {
            System.out.println("SocketException while handling socket: " + e.getMessage());
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    writer.close();
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    // Ignored because we cannot handle it
                }
            }
        }
    }
}
