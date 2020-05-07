package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Käsk kasutaja "väljalogimiseks".
 * Näide: logout
 * TODO: Hetkel salvestatakse selle käsuga ka konto muudatused.
 */
@Command(name = "logout", description = "Logout of DeepMail account")
public class Logout implements Callable<Integer> {

    LoginAccount currentLogin;

    public Logout(LoginAccount currentLogin) {
        this.currentLogin = currentLogin;
    }

    @Override
    public Integer call() {
        if (currentLogin.isLoggedIn()) {
            currentLogin.logout();
        }
        return DMExitCode.OK;
    }
}
