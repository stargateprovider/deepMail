package Commands.FolderCommands;

import picocli.CommandLine.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Kuvab emailid, mis vastavad regulaaravaldisele.
 */
@Command(name = "search", mixinStandardHelpOptions = true, description = "Search for matching messages")
public class SearchMsgs implements Callable<Integer> {
    @Parameters(description = "Regular expression")
    String regex;
    @Option(names = {"-c", "--content"}, description = "Search in message content", defaultValue = "false")
    boolean includeContent;

    FolderNavigation folderNav;

    public SearchMsgs(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() throws MessagingException, IOException {
        Pattern compiledRegex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Message[] messages = folderNav.getCurrentMessages();

        int numberOfResults = 0;
        for (int i = messages.length - 1; i > 0; i--) {
            Message msg = messages[i];

            String searchField = msg.getSubject() + Arrays.toString(msg.getFrom());
            if (includeContent) {
                searchField += ReadMsg.getText(msg);
            }
            Matcher matcher = compiledRegex.matcher(searchField);

            if (matcher.find()) {
                ScrollMessages.showMessageInfo(msg, messages.length - i);
                numberOfResults++;
            }
        }
        System.out.println("Found " + numberOfResults + " results.");
        return 0;
    }
}