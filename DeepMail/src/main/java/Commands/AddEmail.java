package Commands;

import picocli.CommandLine.Command;

import java.io.Serializable;
import java.util.concurrent.Callable;

@Command(name = "addemail", description = "Add an email account to your DeepMail account")
public class AddEmail implements Callable<Integer> {

    private LoginAccount currentLogin;

    public AddEmail(LoginAccount currentLogin) {
        this.currentLogin = currentLogin;
    }
    public AddEmail() {}

    @Override
    public Integer call() {

        if (!currentLogin.isLoggedIn()) {
            System.out.println("Login to your DeepMail account to add email accounts");
            return DMExitCode.USAGE;
        }

        String address = CommandExecutor.quickInput("> Write your email domain: ");
        // TODO: passwordi küsimine võiks käia ilma Stringita
        String password = CommandExecutor.quickInput("> Write your password: ");
        byte[] encryptedPassword = password.getBytes();

        currentLogin.getAccount().addEmail(new Email(address, encryptedPassword));
        return DMExitCode.OK;
    }
}
