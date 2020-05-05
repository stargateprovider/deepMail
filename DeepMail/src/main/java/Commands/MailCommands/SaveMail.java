package Commands.MailCommands;


import Commands.DMExitCode;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import picocli.CommandLine;

import javax.mail.Message;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * In this class we have all the methods needed to save mails in different file formats like html, pdf, docx and so on
 */

@CommandLine.Command(name = "savemail", mixinStandardHelpOptions = true)
public class SaveMail implements Callable<Integer> {

    @CommandLine.Parameters(arity = "1")
    int msgNumber;


    FolderNavigation folderNav;


    public SaveMail(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() throws Exception {
        mailToPdf();
        return DMExitCode.OK;
    }



    public CompletableFuture<File> mailToPdf() throws Exception{
        CompletableFuture<File> pdf = new CompletableFuture<>();
        new Thread(() -> {
            try {
                System.out.println("File saving has begun!");
                pdf.complete(htmlToPdf());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return pdf;
    }

    private File htmlToPdf() throws Exception {
        Message[] currentMsgs = folderNav.getCurrentMessages();

        Message message = currentMsgs[currentMsgs.length-msgNumber];

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();

        File pdfFile = Files.createTempFile("test", ".pdf").toFile();

        Document document = Jsoup.parse(Objects.requireNonNull(ReadMsg.getText(message)));
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        builder.withHtmlContent(document.html(), pdfFile.toURI().toURL().toString());

        builder.toStream(new FileOutputStream(pdfFile));
        builder.run();

        Desktop.getDesktop().browse(pdfFile.toURI());
        System.out.println("File saving has ended");
        return pdfFile;
    }

}
