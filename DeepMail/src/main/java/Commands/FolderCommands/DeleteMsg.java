package Commands.FolderCommands;

import picocli.CommandLine.*;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Callable;

/***
 * Kustuab valitud emaili, küsitakse ühe korra kasutajalt ka nõusolekut.
 * näidissüntaks: delete <number>
 */
@Command(name = "delete", mixinStandardHelpOptions = true)
public class DeleteMsg implements Callable<Integer> {

    @Parameters(arity = "1..*")
    int msgNumber;

    FolderNavigation folderNav;

    public DeleteMsg(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() {
        Message msg = folderNav.messages.get(folderNav.currentFolder.getFullName())[msgNumber - 1];

        try {
            System.out.println("Subject: " + msg.getSubject());
            System.out.println("From: " + Arrays.toString(msg.getFrom()));

            System.out.println("Are you sure you want to delete this email? (Y/N)");

            String result = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (result.toLowerCase().equals("y")) {

                msg.setFlag(Flags.Flag.DELETED, true);
                if (folderNav.currentFolder.isOpen()) {
                    folderNav.currentFolder.expunge();
                    System.out.println("Email is deleted!");
                } else {
                    System.out.println("Email deletion failed!");
                }
            }

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
}