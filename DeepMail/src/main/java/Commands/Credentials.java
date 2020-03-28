package Commands;

public class Credentials {
    public String getUsername() {
        return username;
    }

    public String getImapServer() {
        return imapServer;
    }

    public String getSmptServer() {
        return smptServer;
    }

    public char[] getPassword() {
        return password;
    }

    private String username;
    private String imapServer;
    private String smptServer;
    private char[] password;

    public Credentials(String username, String imapServer, String smptServer, char[] password) {
        this.username = username;
        this.imapServer = imapServer;
        this.smptServer = smptServer;
        this.password = password;
    }
}
