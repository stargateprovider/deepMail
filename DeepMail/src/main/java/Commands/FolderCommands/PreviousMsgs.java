package Commands.FolderCommands;

import picocli.CommandLine.*;

import javax.mail.MessagingException;
import java.util.concurrent.Callable;

/**
 * Loeb argumendina antud arv eelmisi emaile, kui argumenti ei anta, siis default on 10 emaili.
 */
@Command(name = "previous", mixinStandardHelpOptions = true, description = "Show previous messages")
public class PreviousMsgs implements Callable<Integer> {

    @Parameters(description = "Number of messages to show", defaultValue = "10")
    int msgsCount;

    FolderNavigation folderNav;

    public PreviousMsgs(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() {
        try {
            folderNav.showMessages(-1 * msgsCount);
        } catch (MessagingException e) {
            System.out.println("Failed to read " + msgsCount + " at once");
            return 1;
        }
        return 0;
    }
}