package Commands.MailCommands;

import Commands.CommandExecutor;
import Commands.DMExitCode;
import picocli.CommandLine.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;


/**
 * Käsk, millega saab saata kirja, määrates saatjaks suvalise aadressi.
 * süntaks: sendfake <saatja> <saajad...> [-t=<teema>] [-b=<sisu>] [-f=<manused...>] [-r=<pärissaatja>] [-s]
 */

@Command(name = "sendfake", description = "Send an email from a spoofed email address")
public class SendFake implements Callable<Integer> {
    @Parameters(index = "0", description = "Sender's email address")
    private String from;

    @Parameters(index = "1", description = "Recipients", arity = "1..*")
    private String[] to;

    @Option(names = {"-t", "--title"}, defaultValue = "", description = "Title of the message.")
    private String title;

    @Option(names = {"-b", "--body"}, defaultValue = "", description = "Body of the message.")
    private String body;

    @Option(names = {"-f", "--file"}, arity = "0..*", description = "Path to the attachment(s).")
    private List<String> file = new ArrayList<>();

    @Option(names = {"-r", "--realsender"}, defaultValue = "",
            description = "Enter a read address to use for authentication")
    private String realfrom;

    @Option(names = {"-s", "--spf"}, defaultValue = "false",
            description = "Try to bypass SPF check by trimming the email address")
    boolean bypass;

    @Override
    public Integer call() throws MessagingException {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", "587");
        Session session;

        if (realfrom.isEmpty()) {
            properties.setProperty("mail.smtp.host", EmailLogin.mailServers.get("hot")[1]);
            session = Session.getDefaultInstance(properties);

        } else {
            properties.put("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.host", EmailLogin.identifyMailServer(realfrom, false));
            session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(realfrom, CommandExecutor.quickInput(
                            "Enter the password of " + realfrom + " for authentication: "));
                }
            });
        }

        // E-maili loomine
        InternetAddress[] toAddresses = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            toAddresses[i] = new InternetAddress(to[i]);
        }
        MimeMessage message = new MimeMessage(session);
        if (bypass) {
            from = from.substring(0, from.lastIndexOf('.'));
        }
        message.setFrom(new InternetAddress(from));
        message.addRecipients(Message.RecipientType.TO, toAddresses);
        message.setSubject(title);

        // Manuste lisamine
        if (file.isEmpty()) {
            // MIME type of "text/plain"
            message.setText(body);
        } else {
            Multipart multipart = new MimeMultipart();
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(body);
            multipart.addBodyPart(bodyPart);

            for (String i : file) {
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(new FileDataSource(i)));
                bodyPart.setFileName(Paths.get(i).getFileName().toString());
                multipart.addBodyPart(bodyPart);
            }
            message.setContent(multipart);
        }

        // Saatmine
        Transport.send(message);

        file = new ArrayList<>();
        return DMExitCode.OK;
    }
}