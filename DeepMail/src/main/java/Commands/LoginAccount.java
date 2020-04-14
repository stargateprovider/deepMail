package Commands;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "loginaccount", mixinStandardHelpOptions = true)
public class LoginAccount implements Callable<Integer> {

    @CommandLine.Parameters(index = "0")
    private static String username;

    @CommandLine.Parameters(index = "1")
    private static String password;

    private static boolean LoggedIn = false;


    @Override
    public Integer call() {
        if(isLoggedIn()){
            System.out.println("You are already logged in. Please logout first!");
            return 2;
        }

        Account account = Account.getAccount(username, password);
        if(account == null){
            System.out.println("Credentials were wrong or account doensn't exist");
            return 2;
        }

        System.out.println("Logged in as " + account.getUsername());


        LoggedIn = true;
        return 1;

    }

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
