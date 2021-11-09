package dslab.mailbox;

public class StatefulResponse{
    private String reponse;
    private int state;
    private String user;

    public StatefulResponse(String reponse, int state, String user) {
        this.state = state;
        this.reponse = reponse;
        this.user = user;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
