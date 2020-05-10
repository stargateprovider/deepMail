package Commands.FileCommands;

import Commands.CommandExecutor;
import Commands.DMExitCode;
import Commands.LoginAccount;
import picocli.CommandLine.Command;

import java.util.*;
import java.util.concurrent.Callable;

@Command(name = "files", description = "Access your files in DeepMail filecloud")
public class FileClient implements Callable<Integer> {
    LoginAccount currentLogin;

    public FileClient(LoginAccount currentLogin) {
        this.currentLogin = currentLogin;
    }

    @Override
    public Integer call() {
        if (!currentLogin.isLoggedIn()) {
            System.out.println("Login to your DeepMail account to access cloud");
            return DMExitCode.USAGE;
        }

        // TODO: Failide muutmisel/kustutamisel õiguste kontroll

        HashMap<String, Callable<Integer>> commands = new HashMap<>();
        commands.put("show", new ShowFiles(currentLogin.getAccount()));
        //commands.put("search", new Search());
        commands.put("download", new Pull(currentLogin.getAccount()));
        commands.put("upload", new Push(currentLogin.getAccount()));
        commands.put("delete", new Delete(currentLogin.getAccount()));
        //commands.put("move", new Move());
        commands.put("share", new Share(currentLogin.getAccount()));

        CommandExecutor cmdExecutor = new CommandExecutor(commands);
        cmdExecutor.run();
        return DMExitCode.OK;
    }
}