package Commands.FolderCommands;

import picocli.CommandLine.*;
import javax.mail.MessagingException;

import static java.lang.Integer.min;

/**
 * Loeb argumendina antud arv järgmisi emaile, kui argumenti ei anta, siis default on 10 emaili.
 */
@Command(name = "next", mixinStandardHelpOptions = true, description = "Show next messages")
public class NextMsgs extends ScrollMessages {
    public NextMsgs(FolderNavigation folderNav) {
        super(folderNav);
    }

    @Override
    void scrollToRange() throws MessagingException {
        if (msgsCount > 0 && numberOfMessages > 0 && lastMessageIndex < numberOfMessages - 1){
            firstMessageIndex = lastMessageIndex;
            lastMessageIndex = min(lastMessageIndex + msgsCount, numberOfMessages - 1);
            showMessages();

        } else if (lastMessageIndex == numberOfMessages - 1) {
            System.out.println("Already at end of messages.");
        }
    }

}