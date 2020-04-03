package Commands.FolderCommands;

import Commands.CommandExecutor;
import Commands.Login;
import com.sun.mail.imap.IMAPFolder;
import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

@Command(name = "selectFolder", aliases = {"tofolder"}, mixinStandardHelpOptions = true)
public class FolderNavigation implements Callable<Integer> {
    Login session;

    ArrayList<Folder> folders;
    ArrayList<String> folderNames;
    public IMAPFolder currentFolder;
    HashMap<String, Message[]> messages;

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

        ScrollMessages.firstMessageIndex = 1;
        ScrollMessages.lastMessageIndex = -1;
        return new NextMsgs(this).call();
    }

    public Message[] getCurrentMessages() {
        return messages.get(currentFolder.getFullName());
    }
}