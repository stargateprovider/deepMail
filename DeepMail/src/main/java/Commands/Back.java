package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Kasutatav igas CommandExecutoris, et väljuda käsu seest eelmisesse käsku
 */
@Command(name = "back", aliases = {"close"}, mixinStandardHelpOptions = true)
public class Back implements Callable<Integer> {
    @Override
    public Integer call() {
        return -1;
    }
}
