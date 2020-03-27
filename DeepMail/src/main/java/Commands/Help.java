package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Kasutatav igas CommandExecutoris, et kuvada võimalikud commandid
 */
@Command(name = "help", aliases = {"?"}, mixinStandardHelpOptions = true)
public class Help implements Callable<Integer> {
    String helpMsg;

    public Help(String helpMsg) {
        this.helpMsg = helpMsg;
    }

    @Override
    public Integer call() {
        System.out.println(helpMsg);
        return 0;
    }
}
