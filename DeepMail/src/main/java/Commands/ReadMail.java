package Commands;

import com.sun.mail.imap.IMAPFolder;
import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.mail.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Prindib kõik meiliserveri foldername kaustas olevad emailid
 * Kasuta nii: ReadMail <emaili aadress> [-s <meiliserver>]
 */
@Command(name = "readmail", mixinStandardHelpOptions = true)
public class ReadMail implements Callable<Integer> {
    @Parameters(arity="1")
    String email;
    @Option(names = {"-s", "--server"}, description = "Incoming mail server", defaultValue = "")
    String server;

    Message[] messages;
    Store store;
    IMAPFolder currentFolder;
    ArrayList<Folder> folders;
    ArrayList<String> folderNames;

    public static void main(String[] args) {
        new CommandLine(new ReadMail()).execute(args);
    }

    @Override
    public Integer call() throws MessagingException {
        if (server.isEmpty())
            server = MailTools.identifyMailServer(email, true);

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.starttls.enable", "true");
        //props.setProperty("mail.imap.auth.xoauth2.disable", "false");
        props.setProperty("mail.imap.sasl.enable", "true");

        Session session = Session.getInstance(props, null);
        try {
            store = session.getStore("imaps");
            store.connect(server, email, String.valueOf(CommandExecutor.readPassword()));
            System.out.println("Logged in as " + email);

            // Kausta valimine
            folders = new ArrayList<>();
            folderNames = new ArrayList<>();

            for (Folder folder : store.getDefaultFolder().list("*")) {
                if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
                    folders.add(folder);
                    folderNames.add(folder.getFullName() + ": " + folder.getMessageCount());
                }
            }
            new SelectFolder().call();

            // Meilidevaade
            HashMap<String, Callable<Integer>> commands = new HashMap<>();
            commands.put("print", new ReadMsg());
            //commands.put("reply", new ReplyMsg());
            //commands.put("write", new WriteMsg());
            commands.put("delete", new DeleteMsg());
            commands.put("folder", new SelectFolder());

            CommandExecutor cmdExecutor = new CommandExecutor(commands);
            cmdExecutor.run();
            System.out.println("Logged out");
        } /* LISA TINGIMUS: KUI ON UUS GMAIL KASUTAJA>> catch (AuthenticationFailedException e){
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Desktop.getDesktop().browse(URI.create("https://myaccount.google.com/lesssecureapps"));
        }*/ finally {
            //close();
        }
        return 0;
    }

    public void getMessages() throws MessagingException {
        messages = currentFolder.getMessages();

        for (int i = messages.length; i > 0; i--) {
            Message msg = messages[i - 1];

            String newPrefix = "";
            if (!msg.isSet(Flags.Flag.SEEN)) {
                newPrefix = "(NEW) ";
            }

            System.out.println(i + ".\tFrom: " + Arrays.toString(msg.getFrom())
                    + "\n\tSubject: " + msg.getSubject()
                    + "\n\t" + newPrefix + "Date: " + msg.getSentDate());
        }
    }

    public void close() throws MessagingException {
        if (currentFolder != null && currentFolder.isOpen())
            currentFolder.close(true);
        if (store != null)
            store.close();
    }


    /**
     * Prindib valitud emaili sisu või avab vastava HTML faili brauseris
     */
    @Command(name = "readmsg", mixinStandardHelpOptions = true)
    class ReadMsg implements Callable<Integer> {
        @Parameters(arity = "1")
        int msgNumber;

        @Override
        public Integer call() throws IOException, MessagingException {
            String msgContent = getText(messages[msgNumber-1]);

            if (messages[msgNumber-1].isMimeType("multipart/*") && Desktop.isDesktopSupported()
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

    /***
     * Kustuab valitud emaili, küsitakse ühe korra kasutajalt ka nõusolekut.
     * näidissüntaks: delete -i <number>
     */

    @Command(name = "delete", mixinStandardHelpOptions = true)
    class DeleteMsg implements Callable<Integer>{

        @Option(names = {"-i", "--number"}, required = true, arity = "1..*")
        int msgNumber;

        @Override
        public Integer call() {
            Message msg = messages[msgNumber-1];

            try {
                System.out.println("Subject: " + msg.getSubject());
                System.out.println("From: " + Arrays.toString(msg.getFrom()));

                System.out.println("Are you sure you want to delete this email? (Y/N)");

                String result = new BufferedReader(new InputStreamReader(System.in)).readLine();
                if(result.toLowerCase().equals("y")) {

                    msg.setFlag(Flags.Flag.DELETED, true);
                    if(currentFolder.isOpen()){
                        currentFolder.expunge();
                        System.out.println("Email is deleted!");
                    }
                    else{
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

    /**
     * Valib meilide kausta
     * Süntaks: folder <number>
     */
    @Command(name = "folder", mixinStandardHelpOptions = true)
    class SelectFolder implements Callable<Integer> {

        @Override
        public Integer call() throws MessagingException {

            int folderIndex = CommandExecutor.quickChoice(folderNames, "\n");
            String folderName = folders.get(folderIndex).getFullName();

            if (currentFolder != null && currentFolder.isOpen())
                currentFolder.close();
            currentFolder = (IMAPFolder) store.getFolder(folderName);
            if (!currentFolder.isOpen())
                currentFolder.open(Folder.READ_WRITE);

            getMessages();
            return 0;
        }
    }
}
