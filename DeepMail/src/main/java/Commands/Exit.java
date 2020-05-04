package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Programmist koheselt väljumiseks (enamasti halb mõte?)
 */
@Command(name = "exit", aliases = {"quit"}, description = {"Exit application"})
public class Exit implements Callable<Integer> {
    @Override
    public Integer call() {
        System.exit(0);
        return DMExitCode.OK;
    }
}
