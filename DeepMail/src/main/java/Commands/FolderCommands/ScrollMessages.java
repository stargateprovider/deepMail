package Commands.FolderCommands;

import picocli.CommandLine.*;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.concurrent.Callable;


public abstract class ScrollMessages implements Callable<Integer> {
    @Parameters(description = "Number of messages to show", defaultValue = "10")
    int msgsCount = 10;

    FolderNavigation folderNav;
    int numberOfMessages;
    static int firstMessageIndex;
    static int lastMessageIndex;

    public ScrollMessages(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() {
        try {
            numberOfMessages = folderNav.currentFolder.getMessageCount();
            scrollToRange();
        } catch (MessagingException e) {
            System.out.println("Failed to read " + msgsCount + " at once");
            return 1;
        }
        return 0;
    }

    abstract void scrollToRange() throws MessagingException;

    public void showMessages() throws MessagingException {
        Message[] currentMessages = folderNav.getCurrentMessages();

        for (int i = firstMessageIndex; i <= lastMessageIndex; i++) {
            Message msg = currentMessages[numberOfMessages - 1 - i];

            String newPrefix = "";
            if (!msg.isSet(Flags.Flag.SEEN)) {
                newPrefix = "[UNREAD] ";
                msg.setFlag(Flags.Flag.SEEN, true);
            }

            System.out.println(i+1 + ". " + msg.getSubject()
                    + "\n\tFrom: " + Arrays.toString(msg.getFrom())
                    + "\n\t" + newPrefix + "Date: " + msg.getSentDate());
        }
        System.out.printf("Showing messages %d-%d of %d.\n",
                firstMessageIndex+1, lastMessageIndex+1, currentMessages.length);
    }
}
