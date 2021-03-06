package Commands;

import picocli.CommandLine.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "loginaccount", description = "Login to your DeepMail account")
public class LoginAccount implements Callable<Integer> {

    @Parameters(description = "DeepMail username")
    private String username;

    private boolean loggedIn;
    private Account account;
    private char[] pw;

    public byte[] getPw() {
        return new String(pw).getBytes(StandardCharsets.UTF_8);
    }

    public LoginAccount() {
        loggedIn = false;
    }

    @Override
    public Integer call() {
        if(isLoggedIn()){
            // TODO: Logouti kohe siit?
            System.out.println("You are already logged in. Please logout first!");
            return DMExitCode.USAGE;
        }

        pw = CommandExecutor.readPassword();
        Account accountLogin = Account.getAccount(username, pw);
        if(accountLogin == null){
            System.out.println("Credentials were wrong or account doesn't exist");
            return DMExitCode.USAGE;
        }

        System.out.println("Logged in as " + accountLogin.getUsername());
        account = accountLogin;
        loggedIn = true;
        return DMExitCode.OK;
    }

    public Account getAccount(){
        return account;
    }

    public List<Email> getEmailsList() {
        return account.getEmailsList();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void logout() {
        account.sync();
        loggedIn = false;
        account = null;

    }
}
