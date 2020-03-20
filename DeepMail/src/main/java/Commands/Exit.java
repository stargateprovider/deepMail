package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "exit", mixinStandardHelpOptions = true)
public class Exit implements Callable<Integer> {
    @Override
    public Integer call() {
        System.exit(0);
        return 0;
    }
}
