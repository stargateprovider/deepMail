package Commands.MailCommands;


import Commands.DMExitCode;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import picocli.CommandLine;

import javax.mail.Message;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
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


        Document document = new Document();
        File pdfFile = Files.createTempFile("test", ".pdf").toFile();

        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

        document.open();

        Font font = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);

        Paragraph chunk = new Paragraph(ReadMsg.getText(message), font);

        document.add(chunk);
        document.close();

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
