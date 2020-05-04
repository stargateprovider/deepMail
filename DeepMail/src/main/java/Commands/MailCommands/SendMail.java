package Commands.MailCommands;

import Commands.DMExitCode;
import Utilities.PGPUtilities;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Käsk kirja saatmiseks.
 * Süntaks: reply <vastava kirja jrk.> -b <kirja sisu> -e <tee PGP public võtmeni (RSA)> -f <tee manuseni> / write <kirja saaja> -t <kirja pealkiri> -b -f
 * Kasutamisnäide: write user@example.com -t pealkiri -b sisu -f C:\\Users\\user\\Desktop\\file.txt
 */
@Command(name = "sendmail")
public class SendMail implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, arity = "0..*", description = "Path to the attachment(s).")
    List<String> file = new ArrayList<>();

    @Option(names = {"-b", "--body"}, description = "Body of the message.")
    private String body = "";

    @Option(names = {"-t", "--title"}, description = "Title of the message.")
    private String title = "";

    @Option(names = {"-e", "--encrypt"}, description = "Path to RSA public key.")
    private String encrypt = "";

    @Parameters(arity="1")
    String arg;

    FolderNavigation folderNav;

    public SendMail(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    public SendMail() {

    }

    public static void main(String[] args) {
        new CommandLine(new SendMail()).execute(args);
    }

    @Override
    public Integer call() throws MessagingException, NoSuchProviderException, IOException, PGPException {
        if (EmailLogin.credentials == null) {
            System.out.println("Use the \"login\" command first!");
            return DMExitCode.OK;
        }
        Address[] to;

        if (folderNav != null) {
            Message[] currentMessages = folderNav.getCurrentMessages();
            to = currentMessages[currentMessages.length - Integer.parseInt(arg)].getReplyTo();
            if (title.equals("")) {
                title = "RE: " + currentMessages[currentMessages.length - Integer.parseInt(arg)].getSubject();
            }
        } else {
            to = new Address[1];
            to[0] = new InternetAddress(arg);
        }
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", EmailLogin.credentials.getSmptServer());
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, null);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(EmailLogin.credentials.getUsername());
        msg.setRecipients(Message.RecipientType.TO, to);
        msg.setSubject(title);
        if (!encrypt.equals("")) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("body.input"));) { writer.write(body); }
            Security.addProvider(new BouncyCastleProvider());
            PGPUtilities.encryptFile("encrypted.asc", "body.input", encrypt, true, true);
            file.add("encrypted.asc");
        }
        if (file.isEmpty()) {
            msg.setText(body);
        } else {
            Multipart multipart = new MimeMultipart();
            BodyPart bodyPart = new MimeBodyPart();
            if (encrypt.equals("")) {
                bodyPart.setText(body);
                multipart.addBodyPart(bodyPart);
            }
            for (String i : file) {
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(new FileDataSource(i)));
                bodyPart.setFileName(Paths.get(i).getFileName().toString());
                multipart.addBodyPart(bodyPart);
            }
            msg.setContent(multipart);
        }
        Transport.send(msg,EmailLogin.credentials.getUsername(),String.valueOf(EmailLogin.credentials.getPassword()));
        file = new ArrayList<>();
        body = "";
        return DMExitCode.OK;
    }
}



