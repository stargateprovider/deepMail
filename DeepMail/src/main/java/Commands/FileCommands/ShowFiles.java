package Commands.FileCommands;

import Commands.Account;
import Commands.DMExitCode;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(name = "show", description = "Show all files in your DeepMail cloud")
public class ShowFiles implements Callable<Integer> {
    Account account;

    public ShowFiles(Account account) {
        this.account = account;
    }

    @Override
    public Integer call() {
        account.sync();

        System.out.println("Files you have access to:");
        final String[] files = account.getFiles().keySet().toArray(String[]::new);
        for (int i = 0; i < files.length; i++) {
            System.out.println(i+1 + ". " + files[i]);
        }

        return DMExitCode.OK;
    }
}