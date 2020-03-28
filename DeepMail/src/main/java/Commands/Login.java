package Commands;

import picocli.CommandLine.*;

import java.util.concurrent.Callable;

/**
 * Käsk kasutaja admete salvestamiseks kirjade lugemise/saatmise hõlbustamiseks.
 * Näide: login asdf@gmail.com
 */
@Command(name = "login", mixinStandardHelpOptions = true)
public class Login implements Callable<Integer> {
    @Parameters(arity="1")
    String username;

    @Override
    public Integer call() {
        if (CommandExecutor.credentials == null) {
            String imapServer = MailTools.identifyMailServer(username, true);
            String smptServer = MailTools.identifyMailServer(username, false);
            CommandExecutor.credentials = new Credentials(username, imapServer, smptServer, CommandExecutor.readPassword());
        } else {
            System.out.println("You have already saved your credentials (use 'logout' first to login as another user)!");
        }
        return 0;
    }
}
