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
 * Käsk kirja päisest SPF, DKIM ja/või DMARC kirjete otsimiseks kirja päritolu kinnitamiseks.
 * Süntaks: checkauth <kirja number>
 * Kasutamisnäide: checkauth 42
 */
@Command(name = "checkauth")
public class EmailHeaders implements Callable<Integer> {

    @Parameters(arity="1")
    int emailNo;

    FolderNavigation folderNav;

    public EmailHeaders(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    public EmailHeaders() {

    }

    public static void main(String[] args) {
        new CommandLine(new SendMail()).execute(args);
    }

    @Override
    public Integer call() throws MessagingException {
        if (EmailLogin.credentials == null) {
            System.out.println("Use the \"login\" command first!");
            return DMExitCode.OK;
        }

        if (folderNav != null) {
            Message[] currentMessages = folderNav.getCurrentMessages();
            Enumeration<Header> headers = currentMessages[currentMessages.length - emailNo].getMatchingHeaders(new String[]{"Authentication-Results"});
            String[] results = new String[0];
            while (headers.hasMoreElements()) {
                results = headers.nextElement().getValue().split(";\\s*");
                for (String i : results) {
                    if(i.startsWith("dkim="))
                        System.out.println("DKIM result: " + i);
                    if(i.startsWith("spf="))
                        System.out.println("SPF result: " + i);
                    if(i.startsWith("dmarc="))
                        System.out.println("DMARC result: " + i);
                }
                break;
//                System.out.println("<"+header.getName() + "> : <" + header.getValue()+">");
            }
            if (results.length == 0) {
                System.out.println("No Message Authentication Status headers found.");
            }

        } else {
            System.out.println("No folder found, user \"readmail\" first.");
        }
        return DMExitCode.OK;
    }
}



