package Commands.MailCommands;

import Commands.CommandExecutor;
import Commands.DMExitCode;
import java.util.Arrays;
import java.util.concurrent.Callable;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "delete", description = {"Delete emails"})
public class DeleteMsg implements Callable<Integer> {
    @Parameters(arity = "1..*", description = {"Indexes of emails to delete"})
    int[] msgNumbers;

    FolderNavigation folderNav;

    public DeleteMsg(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    public Integer call() {
        Message[] messages = folderNav.messages.get(folderNav.currentFolder.getFullName());

        try {
            for(int i = 0; i < msgNumbers.length; ++i) {
                Message msg = messages[messages.length - msgNumbers[i]];

                System.out.println(i + ". Subject: " + msg.getSubject());
                System.out.println("From: " + Arrays.toString(msg.getFrom()));

                String input = CommandExecutor.quickInput("Are you sure you want to delete this email? (Y/N): ");
                if (input.equalsIgnoreCase("y")) {
                    msg.setFlag(Flag.DELETED, true);
                }
            }

            if (!folderNav.ensureOpenFolder()) {
                throw new MessagingException("Could not open folder. Email Deletion failed!");
            }

            folderNav.currentFolder.expunge();
            System.out.println("Email(s) deleted!");
            folderNav.refreshCurrent();

        } catch (MessagingException e) {
            e.printStackTrace();
            return DMExitCode.SOFTWARE;
        }

        return DMExitCode.OK;
    }
}
