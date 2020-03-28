package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Käsk kasutaja "väljalogimiseks".
 * Näide: logout
 */
@Command(name = "logout", mixinStandardHelpOptions = true)
public class Logout implements Callable<Integer> {

    @Override
    public Integer call() {
        CommandExecutor.credentials = null;
        return 0;
    }
}
