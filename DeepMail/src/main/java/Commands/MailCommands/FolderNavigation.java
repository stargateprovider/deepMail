package Commands.MailCommands;

import Commands.CommandExecutor;
import com.sun.mail.imap.IMAPFolder;
import picocli.CommandLine.*;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

@Command(name = "selectFolder", aliases = {"tofolder"}, description = {"Navigate to another folder"})
public class FolderNavigation implements Callable<Integer> {
    EmailLogin session;

    ArrayList<Folder> folders;
    ArrayList<String> folderNames;
    public IMAPFolder currentFolder;
    HashMap<String, Message[]> messages;

    public FolderNavigation(EmailLogin session) throws MessagingException {
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

        if (currentFolder != null && currentFolder.isOpen()) {
            currentFolder.close();
        }
        currentFolder = (IMAPFolder) folders.get(folderIndex);
        refreshCurrent();

        ScrollMessages.firstMessageIndex = 1;
        ScrollMessages.lastMessageIndex = -1;
        return (new NextMsgs(this)).call();
    }

    public Message[] getCurrentMessages() {
        return messages.get(currentFolder.getFullName());
    }

    public boolean ensureOpenFolder() {
        if (!currentFolder.isOpen()) {
            try {
                currentFolder.open(Folder.READ_WRITE);
            } catch (MessagingException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        return true;
    }

    public void refreshCurrent() throws MessagingException {
        if (!currentFolder.isOpen()) {
            currentFolder.open(Folder.READ_WRITE);
        }
        currentFolder.expunge();
        messages.put(currentFolder.getFullName(), currentFolder.getMessages());
    }
}