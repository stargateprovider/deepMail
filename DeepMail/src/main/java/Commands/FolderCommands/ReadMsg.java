package Commands.FolderCommands;

import picocli.CommandLine.*;

import javax.mail.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;

/**
 * Prindib valitud emaili sisu või avab vastava HTML faili brauseris
 */
@Command(name = "readmsg", mixinStandardHelpOptions = true)
public class ReadMsg implements Callable<Integer> {
    @Parameters(arity = "1")
    int msgNumber;

    FolderNavigation folderNav;

    public ReadMsg(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() throws IOException, MessagingException {
        Message[] currentMessages = folderNav.getCurrentMessages();
        String msgContent = getText(currentMessages[msgNumber - 1]);

        if (currentMessages[msgNumber - 1].isMimeType("multipart/*") && Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

            File tempFile = Files.createTempFile("msg", ".html").toFile();
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(msgContent);
            }
            Desktop.getDesktop().browse(tempFile.toURI());

        } else {
            System.out.println(msgContent);
        }
        return 0;
    }

    /**
     * https://gist.github.com/winterbe/5958387
     * Return the primary text content of the message.
     */
    private String getText(Part p) throws MessagingException, IOException {
        boolean textIsHtml = false;

        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);

                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();

            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return null;
    }
}