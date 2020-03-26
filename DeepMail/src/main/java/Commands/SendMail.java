package Commands;

import picocli.CommandLine;
import picocli.CommandLine.*;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Käsk kirja saatmiseks.
 * Süntaks: sendmail <server> <saatja email> <saatja parool> <saaja> <pealkiri> <kirja sisu>
 * Kasutamisnäide: sendmail smtp.gmail.com asdf@gmail.com hunter2 qwer@gmail.com pealkiri "kirja sisu"
 */
@Command(name = "sendmail", mixinStandardHelpOptions = true)
public class SendMail implements Callable<Integer> {
    @Parameters(arity="6")
    String[] args;

    public static void main(String[] args) {
        new CommandLine(new SendMail()).execute(args);
    }

    @Override
    public Integer call() throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", args[0]);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, null);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(args[1]);
        msg.setRecipients(Message.RecipientType.TO, args[3]);
        msg.setSubject(args[4]);
        msg.setSentDate(new Date());
        msg.setText(args[5]);
        Transport.send(msg,args[1],args[2]);
        return 0;
    }
}



