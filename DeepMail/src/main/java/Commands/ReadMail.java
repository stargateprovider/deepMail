package Commands;

import com.sun.mail.imap.IMAPFolder;
import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.mail.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Prindib kõik meiliserveri foldername kaustas olevad emailid
 * Kasuta nii: ReadMail <meiliserver> <emaili aadress> -p [parool] [kirjade kaust]
 */
@Command(name = "readmail", mixinStandardHelpOptions = true)
public class ReadMail implements Callable<Integer> {
    @Parameters(arity="2")
    String[] args;
    @Option(names = {"-p", "--password"}, required = true, interactive = true)
    char[] password;
    @Option(names = {"-f", "--folder"}, defaultValue = "inbox")
    String folderName;

    Message[] messages;

    public static void main(String[] args) {
        new CommandLine(new ReadMail()).execute(args);
    }

    @Override
    public Integer call() throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);
        Store store = null;
        IMAPFolder folder = null;
        try {
            store = session.getStore("imaps");
            store.connect(args[0], args[1], String.valueOf(password));
            System.out.println("Logged in as " + args[1]);

            folder = (IMAPFolder) store.getFolder(folderName);
            if (!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            messages = folder.getMessages();

            for (int i = messages.length; i > 0; i--) {
                Message msg = messages[i - 1];
                System.out.println(i + ".\tFrom: " + Arrays.toString(msg.getFrom())
                        + "\n\tSubject: " + msg.getSubject()
                        + "\n\tDate: " + msg.getSentDate());
            }

            HashMap<String, Callable<Integer>> commands = new HashMap<>();
            commands.put("print", new ReadMsg());
            //commands.put("reply", new ReplyMsg());
            //commands.put("write", new WriteMsg());
            //commands.put("delete", new DeleteMsg());

            CommandExecutor cmdExecutor = new CommandExecutor(commands);
            cmdExecutor.run();
            System.out.println("Logged out");
        } finally {
            if (folder != null && folder.isOpen()) folder.close(true);
            if (store != null) store.close();
        }
        return 0;
    }


    /**
     * Prindib valitud emaili sisu või avab vastava HTML faili brauseris
     */
    @Command(name = "readmsg", mixinStandardHelpOptions = true)
    class ReadMsg implements Callable<Integer> {
        @Option(names = {"-i", "--number"}, required = true, arity = "1..*")
        int msgNumber;

        @Override
        public Integer call() throws IOException, MessagingException {
            String msgContent = getText(messages[msgNumber-1]);

            if (messages[msgNumber-1].isMimeType("multipart/*") && Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

                File file = new File("temp.html");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(msgContent);
                }
                Desktop.getDesktop().browse(file.toURI());

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
                String s = (String)p.getContent();
                textIsHtml = p.isMimeType("text/html");
                return s;
            }

            if (p.isMimeType("multipart/alternative")) {
                // prefer html text over plain text
                Multipart mp = (Multipart)p.getContent();
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
                Multipart mp = (Multipart)p.getContent();

                for (int i = 0; i < mp.getCount(); i++) {
                    String s = getText(mp.getBodyPart(i));
                    if (s != null)
                        return s;
                }
            }
            return null;
        }
    }
}



