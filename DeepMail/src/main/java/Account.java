import java.util.List;

public class Account {

    private String username;
    private byte[] hashedPassword;

    private List<Email> emailsList;

    public Account(String username, String hashedPassword) {
        this.username = username;
        //Tuleb asendada hashimiseks mõeldud meetodi kutsumisega
        this.hashedPassword = hashedPassword.getBytes();
    }

    public List<Email> getEmailsList() {
        return emailsList;
    }

    public void addEmail(String domain, String password) {
        this.emailsList.add(new Email(domain, password));
    }
}
