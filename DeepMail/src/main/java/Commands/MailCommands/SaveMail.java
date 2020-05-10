package Commands.MailCommands;


import Commands.DMExitCode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.w3c.tidy.Tidy;
import picocli.CommandLine;

import javax.mail.Message;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.awt.print.PrinterJob;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @CommandLine.Option(names = {"-f", "--file"}, description = "file name where to save mail", defaultValue = "test.txt")
    String filename;


    FolderNavigation folderNav;


    public SaveMail(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() throws Exception {

        CompletableFuture<File> pdfFile = mailToPdf();
        String basedir = System.getProperty("user.home");
        File newFile = new File(basedir + "/" + filename);


        if(filename.endsWith("docx")){
            //create docx
            XWPFDocument doc = new XWPFDocument();

            File temp = new File("temporal.pdf");

            FileChannel src = new FileInputStream(pdfFile.get()).getChannel();
            FileChannel dest = new FileOutputStream(temp).getChannel();
            dest.transferFrom(src, 0, src.size());

            PdfReader reader = new PdfReader("temporal.pdf");
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);

            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                TextExtractionStrategy strategy =
                        parser.processContent(i, new SimpleTextExtractionStrategy());
                String text = strategy.getResultantText();
                XWPFParagraph p = doc.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(text);
                run.addBreak(BreakType.PAGE);
            }
            FileOutputStream out = new FileOutputStream(newFile.getAbsolutePath());
            doc.write(out);
            temp.delete();


        }else if(filename.endsWith(".txt")){
            //create txt
            File pdf = pdfFile.get();
            String parsedText;
            PDFParser parser = new PDFParser(new RandomAccessFile(pdf, "r"));
            parser.parse();
            parsedText = new PDFTextStripper().getText(new PDDocument(parser.getDocument()));

            PrintWriter pw = new PrintWriter(newFile);
            pw.print(parsedText);
            pw.close();

        }else if(filename.equals("print")){
            newFile.delete();
            PDDocument document = PDDocument.load(pdfFile.get());
            PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
            if(printService != null){
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPageable(new PDFPageable(document));
                job.setPrintService(printService);
                job.print();
            }
        } else{
            //create pdf
            FileChannel src = new FileInputStream(pdfFile.get()).getChannel();
            FileChannel dest = new FileOutputStream(newFile).getChannel();
            dest.transferFrom(src, 0, src.size());

        }

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
        File htmlfile = Files.createTempFile("test", ".html").toFile();
        File xhtmlfile = Files.createTempFile("xtest", ".xhtml").toFile();

        FileWriter f = new FileWriter(htmlfile);
        f.write(ReadMsg.getText(message));
        f.flush();


       // Document document = Jsoup.parse(Objects.requireNonNull(ReadMsg.getText(message)));
       // document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
       // document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.parse(new FileInputStream(htmlfile), new FileOutputStream(xhtmlfile));

        builder.withHtmlContent(Files.readString(Path.of(xhtmlfile.toURI())), pdfFile.toURI().toURL().toString());

        builder.toStream(new FileOutputStream(pdfFile));
        builder.run();

        //Desktop.getDesktop().browse(pdfFile.toURI());
        System.out.println("File saving has ended");
        return pdfFile;
    }

}
