package Commands.FolderCommands;

import picocli.CommandLine.*;

import javax.mail.MessagingException;
import java.util.concurrent.Callable;

/**
 * Loeb argumendina antud arv järgmisi emaile, kui argumenti ei anta, siis default on 10 emaili.
 */
@Command(name = "next", mixinStandardHelpOptions = true, description = "Show next messages")
public class NextMsgs implements Callable<Integer> {

    @Parameters(description = "Number of messages to show", defaultValue = "10")
    int msgsCount = 10;

    FolderNavigation folderNav;

    public NextMsgs(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() {
        try {
            folderNav.showMessages(msgsCount);
        } catch (MessagingException e) {
            System.out.println("Failed to read " + msgsCount + " at once");
            return 1;
        }
        return 0;
    }
}