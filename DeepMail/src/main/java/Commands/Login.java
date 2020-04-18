package Commands;

import Commands.FolderCommands.*;
import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * Käsk kasutaja admete salvestamiseks kirjade lugemise/saatmise hõlbustamiseks.
 * Näide: login asdf@gmail.com
 */
@Command(name = "login", mixinStandardHelpOptions = true)
public class Login implements Callable<Integer> {
    @Parameters(arity = "1")
    String username;

    public Store store;
    public FolderNavigation folderNav;

    static final String[] outlookServers = new String[]{"imap-mail.outlook.com", "smtp-mail.outlook.com"};

    // Populaarsed meilipakkujate serverid kujul <meiliaadressi sufiks>: {IMAP aadress, SMTP aadress}
    public static final HashMap<String, String[]> mailServers = new HashMap<>() {{
        put("gmail", new String[]{"imap.gmail.com", "smtp.gmail.com"});
        put("yahoo", new String[]{"imap.mail.yahoo.com", "smtp.mail.yahoo.com"});
        put("hotmail", outlookServers);
        put("live", outlookServers);
        put("windowslive", outlookServers);
        put("msn", outlookServers);
        put("online", new String[]{"mail.suhtlus.ee", "mail.hot.ee"});
        put("hot", new String[]{"mail.suhtlus.ee", "mail.hot.ee"});
    }};

    public static void main(String[] args) {
        new CommandLine(new Login()).execute(args);
    }

    @Override
    public Integer call() throws MessagingException {
        String imapServer, smtpServer;
        char[] password = new char[0];

        if(LoginAccount.isLoggedIn()){
            Account account = LoginAccount.getAccount();
            System.out.println("Choose your email");

            int emailIndex = 1;
            for (Email email : account.getEmailsList()) {
                System.out.println(emailIndex + ". " + email.getEmailDomain());
            }
            int index = Integer.parseInt(CommandExecutor.quickInput("Insert the number: "));
            username = account.getEmailsList().get(index-1).getEmailDomain();
            password = new String(account.getEmailsList().get(index-1).getHashedPassword()).toCharArray();

        }

        if (CommandExecutor.credentials == null) {
            imapServer = identifyMailServer(username, true);
            smtpServer = identifyMailServer(username, false);

            if(LoginAccount.isLoggedIn()) CommandExecutor.credentials = new Credentials(username, imapServer, smtpServer, password);
            else CommandExecutor.credentials = new Credentials(username, imapServer, smtpServer, CommandExecutor.readPassword());

        } else {
            System.out.println("You have already saved your credentials (use 'logout' first to login as another user)!");
            return 0;
        }

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.starttls.enable", "true");
        //props.setProperty("mail.imap.auth.xoauth2.disable", "false");
        props.setProperty("mail.imap.sasl.enable", "true");

        Session session = Session.getInstance(props, null);
        try {
            store = session.getStore("imaps");
            store.connect(imapServer, username, String.valueOf(CommandExecutor.credentials.getPassword()));
            System.out.println("Logged in as " + username);

            folderNav = new FolderNavigation(this);
            folderNav.call();

            // Meilidevaade
            HashMap<String, Callable<Integer>> commands = new HashMap<>() {{
                put("selectfolder", folderNav);
                put("folder", new GetCurrentFolder(folderNav));
                put("read", new ReadMsg(folderNav));
                put("next", new NextMsgs(folderNav));
                put("previous", new PreviousMsgs(folderNav));
                put("search", new SearchMsgs(folderNav));
                put("delete", new DeleteMsg(folderNav));
                put("move", new MoveMsg(folderNav));
                put("reply", new SendMail(folderNav));
                put("write", new SendMail());
                put("logout", new Logout());
            }};

            CommandExecutor cmdExecutor = new CommandExecutor(commands);
            cmdExecutor.run();
        } /* LISA TINGIMUS: KUI ON UUS GMAIL KASUTAJA>> catch (AuthenticationFailedException e){
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Desktop.getDesktop().browse(URI.create("https://myaccount.google.com/lesssecureapps"));
        }*/ finally {
            close();
            System.out.println("Logged out");
        }
        return 0;
    }

    public void close() throws MessagingException {
        if (folderNav != null && folderNav.currentFolder != null && folderNav.currentFolder.isOpen())
            folderNav.currentFolder.close(true);
        if (store != null)
            store.close();
        CommandExecutor.credentials = null;
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

// Tõstsin siia ideega, et Commands kaustas ainult commandid
class Credentials {
    public String getUsername() { return username; }
    public String getImapServer() { return imapServer; }
    public String getSmptServer() { return smptServer; }
    public char[] getPassword() { return password; }

    private String username;
    private String imapServer;
    private String smptServer;
    private char[] password;

    public Credentials(String username, String imapServer, String smptServer, char[] password) {
        this.username = username;
        this.imapServer = imapServer;
        this.smptServer = smptServer;
        this.password = password;
    }
}
