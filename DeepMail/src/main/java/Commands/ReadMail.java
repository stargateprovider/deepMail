package Commands;

import com.sun.mail.imap.IMAPFolder;
import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.mail.*;
import java.awt.*;
import java.io.*;
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

    IMAPFolder folder;
    Store store;

    public static void main(String[] args) {
        new CommandLine(new ReadMail()).execute(args);
    }

    @Override
    public Integer call() throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);
        store = null;
        folder = null;
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
            commands.put("delete", new DeleteMsg());
            commands.put("spam", new SpamMsg());

            CommandExecutor cmdExecutor = new CommandExecutor(commands);
            cmdExecutor.run();
            System.out.println("Logged out");
        } finally {
            //if (folder != null && folder.isOpen()) folder.close(true);
            //if (store != null) store.close();
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
                    if(folder.isOpen()){
                        folder.expunge();
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

    @Command(name = "spam", mixinStandardHelpOptions = true)
    class SpamMsg implements Callable<Integer>{

        @Option(names = {"-i", "--number"}, required = true, arity = "1..*")
        int msgNumber;


        @Override
        public Integer call(){
            Message msg = messages[msgNumber-1];

            try{
                System.out.println("Subject: " + msg.getSubject());
                System.out.println("From: " + Arrays.toString(msg.getFrom()));

                System.out.println("Are you sure you want to mark this email as a spam? (Y/N)");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                String result = bufferedReader.readLine();
                if(result.toLowerCase().equals("y")) {
                    Folder[] folders = store.getDefaultFolder().list("*");
                    System.out.println("Valige enda spam folder (number sisestage)");
                    for (int i = 0; i<folders.length; i++) {
                        if(i != 1) //Sellel indeksil on emaili serveri enda emakausta nimi nagu näiteks [Gmail], seega seda ei ole vaja valida
                            System.out.println(i+1 + ": " + folders[i].getName());
                    }
                    int indeks = Integer.parseInt(bufferedReader.readLine())-1;


                    if(folder.isOpen()){
                        folder.moveMessages(new Message[]{msg}, store.getFolder(folders[1].getName()+"/"+folders[indeks].getName()));
                        System.out.println("Email is now in spam folder!");
                    }
                    else{
                        System.out.println("Email marking as spam ended with a failure!");
                    }
                }

            }catch (MessagingException | IOException e){
                System.out.println("SOmething went wrong with spam emails");
                return 1;
            }
            return 0;
        }
    }
}



