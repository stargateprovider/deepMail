package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Kasutatav igas CommandExecutoris, et väljuda käsu seest eelmisesse käsku
 */
@Command(name = "back", aliases = {"close"}, description = "Exit current command menu")
public class Back implements Callable<Integer> {
    @Override
    public Integer call() {
        return DMExitCode.EXITMENU;
    }
}
