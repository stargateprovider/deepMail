package Commands.FileCommands;

import Commands.Account;
import Commands.CommandExecutor;
import Commands.ServerCommunicator;
import DeepmailServerHost.Server;
import Utilities.SharedFile;
import picocli.CommandLine.*;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


@Command(name = "download", description = "Download a file or directory from the cloud")
public class Pull extends ServerCommunicator {
    @Parameters(arity = "1..*", description = "Indexes of items you want to download")
    private int[] itemNumbers;

    @Option(names = {"-d", "--destination"}, description = "Destination folder for downloaded items", defaultValue = "")
    private String folderPath;

    @Option(names = {"-u", "--gui"}, description = "Use GUI to choose folder", defaultValue = "false")
    private boolean useGUI;

    private Account account;
    public Pull(Account account) {
        this.account = account;
    }

    @Override
    public Integer call() {
        // Asjade jaoks kausta valimine
        while (folderPath.isEmpty() || !Files.isDirectory(Paths.get(folderPath))) {
            if (useGUI) {
                JFileChooser fc = new JFileChooser(".");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                while (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) ;
                folderPath = fc.getSelectedFile().getAbsolutePath();

            } else {
                if (!folderPath.isEmpty()) {
                    System.out.println(folderPath + " is not a valid directory.");
                }
                folderPath = CommandExecutor.quickInput("Set destination directory for files: ");
            }
        }

        Path root = Paths.get(folderPath);
        SharedFile[] files = account.getFiles().values().toArray(SharedFile[]::new);

        // Serverist allalaadimine
        return accessServer((in, out) -> {

            out.writeInt(4);
            out.writeInt(itemNumbers.length);

            for (int nr : itemNumbers) {
                if (nr <= 0 || nr > files.length) {
                    System.out.println("Index " + nr + " is invalid.");
                    continue;
                }

                String remotePath = files[nr-1].getOwner() + File.separatorChar + files[nr-1].getPath();
                Path newPath = root.resolve(files[nr-1].getPath());
                out.writeUTF(remotePath);
                out.flush();

                if (in.readBoolean()) {
                    Server.populateFolder(in, newPath);
                } else {
                    Files.createDirectories(newPath);
                    Files.write(newPath, in.readNBytes(in.readInt()), StandardOpenOption.CREATE);
                    System.out.println("Saved file " + newPath);
                }
            }

        });
    }
}
