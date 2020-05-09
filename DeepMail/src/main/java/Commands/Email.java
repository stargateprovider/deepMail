package Commands;

import picocli.CommandLine.Command;

import java.io.Serializable;
import java.util.concurrent.Callable;

@Command(name = "addemail", description = "Add an email account to your DeepMail account")
public class Email implements Serializable {

    private byte[] encryptedPassword;

    public Email() {
    }

    private String address;

    public Email(String address, byte[] encryptedPassword) {
        this.address = address;
        this.encryptedPassword = encryptedPassword;
    }

    public String getAddress() {
        return address;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

}
