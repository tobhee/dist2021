package dslab.mailbox;

import dslab.protocols.Mail;
import dslab.util.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MailStorage {

    private int nextId;
    private HashMap<String, ConcurrentHashMap<Integer, Mail>> mails;

    public MailStorage(Config config) {
        this.mails = new HashMap<>();
        this.nextId = 0;
        Set<String> users = new Config((config.getString("users.config"))).listKeys();
        for(String user : users) {
            this.mails.put(user, new ConcurrentHashMap<>());
        }
    }

    public Mail showMail(String user, int id) {
        return mails.get(user).get(id);
    }

    // returns id of new mail
    public void addMail(String user, Mail mail) {
        System.out.println(user);
        System.out.println(mail);
        if(mails.containsKey(user)) {
            mails.get(user).put(nextId, mail);
            nextId++;
        }
    }

    public boolean knowsUser(String user) {
        return mails.containsKey(user);
    }

    public void deleteMail(String user, int id) {
        mails.get(user).remove(id);
    }

    public String listMailsOfUser(String user) {
        String str = "";
        // iterate through mailbox of user
        for(Map.Entry<Integer, Mail> mail : mails.get(user).entrySet()) {
            str = str.concat(mail.getKey().toString() + " " + mail.getValue().getFrom() + " " + mail.getValue().getSubject() + "\n\r");
        }
        str = str.equals("") ? "No mails in your inbox" : str;
        return str;
    }
}
