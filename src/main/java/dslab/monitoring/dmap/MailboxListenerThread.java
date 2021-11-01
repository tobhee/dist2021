package dslab.monitoring.dmap;

import dslab.util.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

public class MailboxListenerThread extends Thread {

    private final ServerSocket serverSocket;
    private Config config;
    private Config userConfig;
    private ConcurrentHashMap<Integer, String> messageMap;

    final int LOGGED_OUT = 0;
    final int LOGGED_IN = 1;
    int state = LOGGED_OUT;

    public MailboxListenerThread(ServerSocket serverSocket, Config config) {
        this.serverSocket = serverSocket;
        this.config = config;
        this.userConfig = new Config(config.getString("users.config"));
        this.messageMap = new ConcurrentHashMap<>();
    }

    public void run() {

        while(true) {
            Socket socket = null;
            try {
                //wait for a client to start a dialogue
                socket = serverSocket.accept();
                //start new thread to wait for a client to start a dialogue
                new MailboxListenerThread(serverSocket, config).start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                String inputLine, outputLine;
                writer.println("ok DMAP");
                while((inputLine = reader.readLine()) != null) {
                    outputLine = processInput(inputLine);
                    writer.println(outputLine);
                    if(outputLine.equals("ok bye")) break;
                }
            } catch(SocketException e) {
                System.out.println("SocketException while handling socket: " + e.getMessage());
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Ignored because we cannot handle it
                    }
                }
            }
        }
    }

    public String processInput(String request) {

        if(request == null) {
            return "ok DMAP";
        }
        String[] words = request.split(" ");
        if(request.equals("quit")) {
            return "ok bye";
        }

        if(state == LOGGED_OUT) {
            if(words[0].equals("login") && words.length == 3) {
                if(userConfig.containsKey(words[1])) {
                    if(userConfig.getString(words[1]).equals(words[2])) {
                        state = LOGGED_IN;
                        return "S: ok";
                    } else return "S: error wrong password";
                }  else return "S: error unknown user";
            }
        }

        if(request.equals("logout")) {
            if(state == LOGGED_IN) {
                state = LOGGED_OUT;
                return "S: ok";
            } else return "S: error not logged in";
        }

        return "S: error unknown command";
    }
}
