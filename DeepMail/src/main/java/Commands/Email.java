package Commands;

import picocli.CommandLine.Command;

import java.io.Serializable;
import java.util.concurrent.Callable;

@Command(name = "addemail", description = "Add an email account to your DeepMail account")
public class Email implements Callable<Integer>, Serializable {

    private String emailDomain;
    private byte[] hashedPassword;


    public String getEmailDomain() {
        return emailDomain;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    @Override
    public Integer call() {

        if (!LoginAccount.isLoggedIn()) {
            System.out.println("You need to login first");
            return DMExitCode.USAGE;
        }

        emailDomain = CommandExecutor.quickInput("> Write your email domain: ");
        String password = CommandExecutor.quickInput("> Write your password: ");
        hashedPassword = password.getBytes();

        Account account = Account.getAccount(LoginAccount.getUsername(), LoginAccount.getPassword());
        if (account != null) account.addEmail(this);

        return DMExitCode.OK;

    }
}
