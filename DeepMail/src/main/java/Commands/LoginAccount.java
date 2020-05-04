package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "loginaccount", description = {"Login to your DeepMail account"})
public class LoginAccount implements Callable<Integer> {

    @Parameters(index = "0")
    private static String username;

    @Parameters(index = "1")
    private static String password;

    private static boolean LoggedIn = false;
    private static Account account;


    @Override
    public Integer call() {
        if(isLoggedIn()){
            System.out.println("You are already logged in. Please logout first!");
            return DMExitCode.USAGE;
        }

        Account accountLogin = Account.getAccount(username, password);
        if(accountLogin == null){
            System.out.println("Credentials were wrong or account doensn't exist");
            return DMExitCode.USAGE;
        }

        System.out.println("Logged in as " + accountLogin.getUsername());

        account = accountLogin;

        LoggedIn = true;
        return DMExitCode.SOFTWARE;

    }

    public static Account getAccount(){ return account;}

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static boolean isLoggedIn() {
        return LoggedIn;
    }
}
