package Commands.FileCommands;

import Commands.Account;
import Commands.DMExitCode;
import Commands.ServerCommunicator;
import picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.Arrays;


@Command(name = "upload", description = "Upload a file or directory", usageHelpAutoWidth = true)
public class Push extends ServerCommunicator {
    @Parameters(arity = "1..*", paramLabel = "paths", description = "Paths of items to upload")
    private String[] itemnames;

    @Option(arity = "1", names = {"-d", "--destination"}, description = "Destination folder in server",
            defaultValue = "", showDefaultValue = Help.Visibility.ALWAYS)
    private String remotePathStr;

    private Path remotePath;
    private Account account;

    public Push(Account account) {
        this.account = account;
    }

    @Override
    public Integer call() {
        remotePath = Paths.get(remotePathStr).normalize();
        if (itemnames.length == 0) {
            return DMExitCode.USAGE;
        }

        accessServer((in, out) -> {
            out.writeInt(5);
            out.writeUTF(account.getUsername() + File.separatorChar + remotePath.toString());
            if (!remotePathStr.isEmpty()) {
                account.addFile(remotePath);
            }
            sendFolder(out, Arrays.stream(itemnames)
                    .filter(filename -> {
                        Path path = Paths.get(filename);
                        if (Files.isDirectory(path) || Files.isRegularFile(path)) {
                            return true;
                        }
                        System.out.println(filename + " is not a valid file or directory, discarding...");
                        return false;
                    })
                    .map(Paths::get)
                    .toArray(Path[]::new), remotePath);
            out.flush();
        });

        return DMExitCode.OK;
    }

    private void sendFolder(ObjectOutputStream out, Path[] itemPaths, Path remotePath) throws IOException {
        out.writeInt(itemPaths.length);
        for (Path path : itemPaths) {

            // Saadetakse järgmise kausta/faili nimi
            out.writeUTF(path.getFileName().toString());
            // Salvestatakse järgmise kausta/faili tee serveris
            Path newRemotePath = remotePath.resolve(path.getFileName());

            if (Files.isDirectory(path)) {
                out.writeBoolean(true);
                sendFolder(out, Files.list(path).toArray(Path[]::new), newRemotePath);

            } else if (Files.isRegularFile(path)) {
                out.writeBoolean(false);
                byte[] fileBytes = Files.readAllBytes(path);
                out.writeInt(fileBytes.length);
                out.write(fileBytes);
            }

            System.out.println("Uploaded " + path);
            account.addFile(newRemotePath);
        }
    }
}
