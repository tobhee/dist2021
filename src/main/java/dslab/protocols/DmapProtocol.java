package dslab.protocols;

import dslab.util.Config;

public class DmapProtocol {

    private final int LOGGED_OUT = 0;

    private int state = LOGGED_OUT;

    public String processInput(String request, Config config) {
        if(request == null)
            return "ok DMAP";
        String[] words = request.split(" ");
        if(request.equals("quit"))
            return "ok bye";
        if(state == LOGGED_OUT)
        if(words[0].equals("login") && words.length == 3) {
            if(config.containsKey(words[1])) {
                if(config.getString(words[1]).equals(words[2])) {
                    return "ok";
                } else return "error wrong password";
            }  else return "error unknown user";
        } else return "error already logged in";



        return "error unknown command";
    }
}
