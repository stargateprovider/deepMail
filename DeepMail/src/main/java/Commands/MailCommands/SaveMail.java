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

        Message[] currentMsgs = folderNav.getCurrentMessages();

        Message message = currentMsgs[currentMsgs.length-msgNumber];


        /*Document document = new Document();

        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

        document.open();

        Font font = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);

        Paragraph chunk = new Paragraph(ReadMsg.getText(message), font);

        document.add(chunk);
        document.close();*/

        PdfRendererBuilder builder = new PdfRendererBuilder();

        File pdfFile = Files.createTempFile("test", ".pdf").toFile();

        Document document = Jsoup.parse(Objects.requireNonNull(ReadMsg.getText(message)));
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        builder.withHtmlContent(document.html(), pdfFile.toURI().toURL().toString());

        builder.toStream(new FileOutputStream(pdfFile));
        builder.run();

       /* File tempFile = Files.createTempFile("msg", ".xhtml").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Hello world");
        }*/



        Desktop.getDesktop().browse(pdfFile.toURI());

        return DMExitCode.OK;
    }

    public void htmlToPdf(){
    }

}
