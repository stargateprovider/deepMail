public class Email {

    private String emailDomain;
    private byte[] hashedPassword;


    public Email(String emailDomain, String hashedPassword) {
        this.emailDomain = emailDomain;
        //Tuleb asendada hashimiseks mõeldud meetodi kutsumisega
        this.hashedPassword = hashedPassword.getBytes();
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

}
