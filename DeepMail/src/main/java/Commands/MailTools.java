package Commands;

import java.util.ArrayList;
import java.util.HashMap;

public class MailTools {
    static final String[] outlookServers = new String[]{"imap-mail.outlook.com", "smtp-mail.outlook.com"};

    // Populaarsed meilipakkujate serverid kujul <meiliaadressi sufiks>: {IMAP aadress, SMTP aadress}
    public static final HashMap<String, String[]> mailServers = new HashMap<>() {{
        put("gmail",        new String[]{"imap.gmail.com", "smtp.gmail.com"});
        put("yahoo",        new String[]{"imap.mail.yahoo.com", "smtp.mail.yahoo.com"});
        put("hotmail",      outlookServers);
        put("live",         outlookServers);
        put("windowslive",  outlookServers);
        put("msn",          outlookServers);
        put("online",       new String[]{"mail.suhtlus.ee", "mail.hot.ee"});
        put("hot",          new String[]{"mail.suhtlus.ee", "mail.hot.ee"});
    }};

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

