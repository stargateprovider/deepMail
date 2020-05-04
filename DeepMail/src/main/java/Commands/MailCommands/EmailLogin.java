package Commands.MailCommands;

import Commands.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Käsk kasutaja admete salvestamiseks kirjade lugemise/saatmise hõlbustamiseks.
 * Näide: login asdf@gmail.com
 */
@Command(name = "login", description = {"Login to a mail account"})
public class EmailLogin implements Callable<Integer> {
    @Option(names = {"-u", "--user"}, arity = "1", defaultValue = "", description = {"Email address"})
    String username;

    static Credentials credentials = null;
    public Store store;
    public FolderNavigation folderNav;
    static final String[] outlookServers = new String[]{"imap-mail.outlook.com", "smtp-mail.outlook.com"};

    // Populaarsed meilipakkujate serverid kujul <meiliaadressi sufiks>: {IMAP aadress, SMTP aadress}
    public static final HashMap<String, String[]> mailServers = new HashMap<>();
    static {
        mailServers.put("gmail", new String[]{"imap.gmail.com", "smtp.gmail.com"});
        mailServers.put("yahoo", new String[]{"imap.mail.yahoo.com", "smtp.mail.yahoo.com"});
        mailServers.put("hotmail", outlookServers);
        mailServers.put("live", outlookServers);
        mailServers.put("windowslive", outlookServers);
        mailServers.put("msn", outlookServers);
        mailServers.put("online", new String[]{"mail.suhtlus.ee", "mail.hot.ee"});
        mailServers.put("hot", new String[]{"mail.suhtlus.ee", "mail.hot.ee"});
    }

    public static void main(String[] args) {
        (new CommandLine(new EmailLogin())).execute(args);
    }

    public Integer call() throws MessagingException {
        char[] password = new char[0];

        if (LoginAccount.isLoggedIn()) {
            Account account = LoginAccount.getAccount();
            List<String> emailAddresses = account.getEmailsList().stream()
                    .map(Email::getEmailDomain)
                    .collect(Collectors.toList());

            if (!emailAddresses.isEmpty()) {
                System.out.println("Choose your email:");
                int index = CommandExecutor.quickChoice(emailAddresses, "\n");

                username = emailAddresses.get(index);
                password = (new String((account.getEmailsList().get(index)).getHashedPassword())).toCharArray();
            }
        }

        if (username.isEmpty()) {
            username = CommandExecutor.quickInput("Enter email address: ");
        }
        if (password.length == 0) {
            password = CommandExecutor.readPassword();
        }

        String imapServer = identifyMailServer(username, true);
        String smtpServer = identifyMailServer(username, false);
        credentials = new Credentials(username, imapServer, smtpServer, password);

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.starttls.enable", "true");
        props.setProperty("mail.imap.sasl.enable", "true");
        Session session = Session.getInstance(props, null);

        try {
            store = session.getStore("imaps");
            store.connect(imapServer, username, String.valueOf(credentials.getPassword()));
            System.out.println("Logged in as " + username);

            folderNav = new FolderNavigation(this);
            folderNav.call();

            HashMap<String, Callable<Integer>> commands = new HashMap();
            commands.put("selectfolder", folderNav);
            commands.put("folder", new GetCurrentFolder(folderNav));
            commands.put("read", new ReadMsg(folderNav));
            commands.put("attachments", new Attachments(folderNav));
            commands.put("next", new NextMsgs(folderNav));
            commands.put("previous", new PreviousMsgs(folderNav));
            commands.put("search", new SearchMsgs(folderNav));
            commands.put("delete", new DeleteMsg(folderNav));
            commands.put("move", new MoveMsg(folderNav));
            commands.put("reply", new SendMail(folderNav));
            commands.put("write", new SendMail());
            commands.put("logout", new Back());

            CommandExecutor cmdExecutor = new CommandExecutor(commands);
            cmdExecutor.run();
            System.out.println("Logged out");

        } catch (AuthenticationFailedException e) {
            System.out.println("Authentication failed.");
            if (imapServer.equals(mailServers.get("gmail")[0])) {
                System.out.println("If you're logging in with this address for the first time," +
                        "you need to enable access at https://myaccount.google.com/lesssecureapps.");
            }
        } finally {
            close();
        }
        return DMExitCode.OK;
    }

    public void close() throws MessagingException {
        if (folderNav != null && folderNav.currentFolder != null && folderNav.currentFolder.isOpen()) {
            folderNav.currentFolder.close(true);
        }
        if (store != null) {
            store.close();
        }
        credentials = null;
    }

    public static String identifyMailServer(String email, boolean incoming) {
        int inOrOut = incoming ? 0 : 1;
        String suffix = email.substring(email.indexOf('@') + 1, email.lastIndexOf('.'));

        if (mailServers.containsKey(suffix)) {
            return mailServers.get(suffix)[inOrOut];
        } else {
            var options = new ArrayList<>(mailServers.keySet());
            options.add("Other server");

            int index = CommandExecutor.quickChoice(options, "\n");
            if (index == options.size() - 1) {
                return CommandExecutor.quickInput("Enter custom server address: ");
            }
            return mailServers.get(options.get(index))[inOrOut];
        }
    }
}


class Credentials {
    private String username;
    private String imapServer;
    private String smptServer;
    private char[] password;

    public String getUsername() {
        return username;
    }

    public String getImapServer() {
        return imapServer;
    }

    public String getSmptServer() {
        return smptServer;
    }

    public char[] getPassword() {
        return password;
    }

    public Credentials(String username, String imapServer, String smptServer, char[] password) {
        this.username = username;
        this.imapServer = imapServer;
        this.smptServer = smptServer;
        this.password = password;
    }
}
