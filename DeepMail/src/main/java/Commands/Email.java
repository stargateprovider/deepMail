package Commands;

import picocli.CommandLine.Command;

import java.io.Serializable;
import java.util.concurrent.Callable;

@Command(name = "addemail", description = "Add an email account to your DeepMail account")
public class Email implements Callable<Integer>, Serializable {

    private String address;
    private byte[] encryptedPassword;
    private LoginAccount currentLogin;

    public String getAddress() {
        return address;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public Email(LoginAccount currentLogin) {
        this.currentLogin = currentLogin;
    }
    public Email() {}

    @Override
    public Integer call() {

        if (!currentLogin.isLoggedIn()) {
            System.out.println("Login to your DeepMail account to add email accounts");
            return DMExitCode.USAGE;
        }

        address = CommandExecutor.quickInput("> Write your email domain: ");
        // TODO: passwordi küsimine võiks käia ilma Stringita
        String password = CommandExecutor.quickInput("> Write your password: ");
        encryptedPassword = password.getBytes();

        currentLogin.getAccount().addEmail(this);
        return DMExitCode.OK;
    }
}
