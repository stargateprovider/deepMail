package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Käsk kasutaja "väljalogimiseks".
 * Näide: logout
 */
@Command(name = "logout", description = {"Logout of DeepMail account"})
public class Logout implements Callable<Integer> {

    @Override
    public Integer call() {
        // TODO
        return DMExitCode.OK;
    }
}
