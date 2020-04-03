package Commands.FolderCommands;

import picocli.CommandLine.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Peamine eesmärk on liigutada kirju spam folderisse, aga võib ka mõnda teisse folderisse liigutada.
 */
@Command(name = "move", mixinStandardHelpOptions = true)
public class MoveMsg implements Callable<Integer> {

    @Parameters(arity = "1..*")
    int msgNumber;

    FolderNavigation folderNav;

    public MoveMsg(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() {
        Message msg = folderNav.messages.get(folderNav.currentFolder.getFullName())[msgNumber - 1];

        try {
            System.out.println("Subject: " + msg.getSubject());
            System.out.println("From: " + Arrays.toString(msg.getFrom()));

            System.out.println("Are you sure you want to move this email to another folder? (Y/N)");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String result = bufferedReader.readLine();
            if (result.toLowerCase().equals("y")) {
                System.out.println("Choose your folder (insert number)");
                for (int i = 0; i < folderNav.folders.size(); i++) {
                    System.out.println(i + 1 + ": " + folderNav.folders.get(i).getFullName());
                }
                int indeks = Integer.parseInt(bufferedReader.readLine()) - 1;

                if (folderNav.currentFolder.isOpen()) {
                    folderNav.currentFolder.moveMessages(new Message[]{msg}, folderNav.folders.get(indeks));
                    System.out.println("Email is now in " + folderNav.folders.get(indeks).getName() + " folder!");
                } else {
                    System.out.println("Email moving ended with a failure!");
                    System.out.println("Connection to your folder is lost.");
                    return 1;
                }
            }

        } catch (MessagingException | IOException e) {
            System.out.println("Check the folder you chose. Folders like drafts folder aren't accepted");
            return 1;
        }
        return 0;
    }
}