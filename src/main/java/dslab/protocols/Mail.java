package dslab.protocols;

public class Mail {

    private String to;
    private String from;
    private String subject;
    private String data;

    public Mail(String to, String from, String subject, String data) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.data = data;
    }

    public Mail(){}

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return
                "from " + from + '\n' +
                "to " + to + '\n' +
                "subject " + subject + '\n' +
                "data " + data;
    }
}
