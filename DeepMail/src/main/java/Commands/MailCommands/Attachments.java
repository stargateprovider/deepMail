package Commands.MailCommands;

import Commands.CommandExecutor;
import Commands.DMExitCode;
import picocli.CommandLine.*;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Prindib valitud emaili sisu või avab vastava HTML faili brauseris
 */
@Command(name = "attachments", description = {"Open or save the chosen email's attachments"})
public class Attachments implements Callable<Integer> {
    @Parameters(arity = "1")
    int msgNumber;

    FolderNavigation folderNav;
    ArrayList<MimeBodyPart> attachments;

    public Attachments(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() throws IOException, MessagingException {
        Message[] messages = folderNav.getCurrentMessages();
        attachments = new ArrayList<>();
        getAttachments(messages[messages.length - msgNumber]);

        if (attachments.isEmpty()) {
            System.out.println("No attachments.");
        } else {
            ArrayList<String> names = new ArrayList<>();
            for (MimeBodyPart att : attachments) {
                names.add(att.getFileName());
            }
            System.out.println("Attachments: " + String.join(", ", names));

            List<String> options = Arrays.asList("open", "save", "nothing");
            int choice = CommandExecutor.quickChoice(options, " ");
            switch (choice) {
                case 0: open();break;
                case 1: save();
            }
        }
        return DMExitCode.OK;
    }

    public void getAttachments(Part p) throws MessagingException, IOException {
        if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
            attachments.add((MimeBodyPart) p);

        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                getAttachments(mp.getBodyPart(i));
            }
        }
    }

    public void open() throws IOException, MessagingException {
        if (attachments.isEmpty()) {
            System.out.println("No attachments.");
        } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

            for (MimeBodyPart att : attachments) {
                String name = att.getFileName();
                int dotIndex = name.lastIndexOf(".");

                File tempFile = Files.createTempFile(name.substring(0, dotIndex), name.substring(dotIndex)).toFile();
                att.saveFile(tempFile);
                Desktop.getDesktop().browse(tempFile.toURI());
            }
        } else {
            System.out.println("Command Promptis ei tööta");
        }
    }

    public void save() throws IOException, MessagingException {
        if (attachments.isEmpty()) {
            System.out.println("No attachments.");
        } else {
            Path dirPath = Paths.get("attachments");
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            for (MimeBodyPart att : attachments) {
                att.saveFile(dirPath + File.pathSeparator + att.getFileName());
            }
            System.out.println("Saved files to disk");
        }
    }
}