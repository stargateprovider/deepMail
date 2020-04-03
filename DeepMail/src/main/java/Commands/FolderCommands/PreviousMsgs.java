package Commands.FolderCommands;

import picocli.CommandLine.*;
import javax.mail.MessagingException;

import static java.lang.Integer.max;

/**
 * Loeb argumendina antud arv eelmisi emaile, kui argumenti ei anta, siis default on 10 emaili.
 */
@Command(name = "previous", mixinStandardHelpOptions = true, description = "Show previous messages")
public class PreviousMsgs extends ScrollMessages {
    public PreviousMsgs(FolderNavigation folderNav) {
        super(folderNav);
    }

    @Override
    void scrollToRange() throws MessagingException {
        if (msgsCount > 0 && numberOfMessages > 0 && firstMessageIndex > 0){
            lastMessageIndex = firstMessageIndex;
            firstMessageIndex = max(firstMessageIndex - msgsCount, 0);
            showMessages();

        } else if (lastMessageIndex == numberOfMessages - 1) {
            System.out.println("Already at beginning of messages.");
        }
    }
}