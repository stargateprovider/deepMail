package Commands.MailCommands;

import Commands.DMExitCode;
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
            return DMExitCode.SOFTWARE;
        }
        return DMExitCode.OK;
    }

    abstract void scrollToRange() throws MessagingException;

    public void showMessages() throws MessagingException {
        Message[] currentMessages = folderNav.getCurrentMessages();

        for (int i = firstMessageIndex; i <= lastMessageIndex; i++) {
            Message msg = currentMessages[numberOfMessages - 1 - i];
            showMessageInfo(msg, i+1);
        }
        System.out.printf("Showing messages %d-%d of %d.\n",
                firstMessageIndex+1, lastMessageIndex+1, currentMessages.length);
    }

    public static void showMessageInfo(Message msg, int nr) throws MessagingException {
        String newPrefix = "";
        if (!msg.isSet(Flags.Flag.SEEN)) {
            newPrefix = "[UNREAD] ";
        }

        System.out.println(nr + ". " + msg.getSubject()
                + "\n\tFrom: " + Arrays.toString(msg.getFrom())
                + "\n\t" + newPrefix + "Date: " + msg.getSentDate());
    }
}
