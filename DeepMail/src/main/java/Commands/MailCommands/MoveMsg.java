package Commands.MailCommands;

import Commands.CommandExecutor;
import Commands.DMExitCode;
import java.util.Arrays;
import java.util.concurrent.Callable;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "move", description = {"Move messages to a different folder"})
public class MoveMsg implements Callable<Integer> {
    @Parameters(arity = "1..*")
    int[] msgNumbers;
    FolderNavigation folderNav;

    public MoveMsg(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    public Integer call() {
        Message[] messages = folderNav.getCurrentMessages();
        Message[] messagesToMove = new Message[msgNumbers.length];

        for(int i = 0; i < msgNumbers.length; ++i) {
            Message msg = messages[messages.length - msgNumbers[i]];
            messagesToMove[i] = msg;

            try {
                System.out.println("Subject: " + msg.getSubject() + "\nFrom: " + Arrays.toString(msg.getFrom()));
            } catch (MessagingException e) {
                System.out.println("Could not read message. Is the connection lost?");
                return DMExitCode.SOFTWARE;
            }
        }

        String input = CommandExecutor.quickInput(
                "\nAre you sure you want to move these emails to another folder? (Y/N): ");
        if (input.equalsIgnoreCase("y")) {
            while(true) {

                System.out.println("Choose your folder:");
                int indeks = CommandExecutor.quickChoice(folderNav.folderNames, "\n");
                if (!folderNav.ensureOpenFolder()) {
                    System.out.println("Could not open folder. Email moving ended with a failure!");
                    break;
                }

                Folder destFolder = folderNav.folders.get(indeks);

                try {
                    folderNav.currentFolder.moveMessages(messagesToMove, destFolder);
                    System.out.println("Email(s) moved to " + destFolder.getName() + " folder!");
                    folderNav.refreshCurrent();
                    return DMExitCode.OK;

                } catch (MessagingException e) {
                    System.out.println("Check the folder you chose. Folders like drafts folder aren't accepted.");
                }
            }
        }
        return DMExitCode.SOFTWARE;
    }
}
