package Commands.FolderCommands;

import Commands.CommandExecutor;
import Commands.Login;
import com.sun.mail.imap.IMAPFolder;
import picocli.CommandLine.*;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;

@Command(name = "selectFolder", aliases = {"tofolder"}, mixinStandardHelpOptions = true)
public class FolderNavigation implements Callable<Integer> {
    Login session;

    ArrayList<Folder> folders;
    ArrayList<String> folderNames;
    public IMAPFolder currentFolder;

    HashMap<String, Message[]> messages;
    int firstMessageIndex = 0;
    int lastMessageIndex = 0;

    public FolderNavigation(Login session) throws MessagingException {
        this.session = session;
        folders = new ArrayList<>();
        folderNames = new ArrayList<>();
        messages = new HashMap<>();

        for (Folder folder : session.store.getDefaultFolder().list("*")) {
            if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
                folders.add(folder);
                folderNames.add(folder.getFullName() + ": " + folder.getMessageCount());

                folder.open(Folder.READ_ONLY);
                messages.put(folder.getFullName(), folder.getMessages());
                folder.close();
            }
        }
    }

    @Override
    public Integer call() throws MessagingException {
        int folderIndex = CommandExecutor.quickChoice(folderNames, "\n");

        if (currentFolder != null && currentFolder.isOpen())
            currentFolder.close();
        currentFolder = (IMAPFolder) folders.get(folderIndex);
        if (!currentFolder.isOpen())
            currentFolder.open(Folder.READ_WRITE);

        return new NextMsgs(this).call();
    }

    // Äkki parameetritega firstIndex, lastIndex?
    public void showMessages(int count) throws MessagingException {
        int i, limit;
        Message[] currentMessages = messages.get(currentFolder.getFullName());

        if (count > 0) {
            i = currentMessages.length - lastMessageIndex;
            limit = Math.max(i - count, 0);
        } else {
            i = currentMessages.length - firstMessageIndex - count;
            if (i > currentMessages.length)
                i = currentMessages.length;
            limit = currentMessages.length - firstMessageIndex;

        }
        int start = i; //Kasutajale tagasiside andmiseks
        firstMessageIndex = currentMessages.length - i;

        while (i > limit) {
            Message msg = currentMessages[i - 1];

            String newPrefix = "";
            if (!msg.isSet(Flags.Flag.SEEN)) {
                newPrefix = "[NEW] ";
            }

            System.out.println(i + ". " + msg.getSubject()
                    + "\n\tFrom: " + Arrays.toString(msg.getFrom())
                    + "\n\t" + newPrefix + "Date: " + msg.getSentDate());
            i--;
        }
        lastMessageIndex = currentMessages.length - i;
        System.out.println(start - limit + " messages read out of " + currentMessages.length);
    }
}