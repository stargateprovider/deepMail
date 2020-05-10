package DeepmailServerHost;

import Commands.Account;
import Commands.DMExitCode;
import Utilities.SharedFile;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Selle klassi mõte on praegu imiteerida Andmete lugemist serveris,
 * et hiljem võimalusel realiseerida see päris võrgus.
 * Üldeesmärk on see, et sama kasutajat saaks kasutada mitmes arvutis
 * See tuleb käivitada koos deepmail rakendusega
 * Jookseb localhost:1337 peal
 * Esmane versioon on valmis
 * Handlib 1 requesti korraga
 * TODO: Handli mitu requesti
 * TODO: Anda kliendile tagasisidet
 */

public class Server {

    private static ConcurrentMap<String, Account> accounts;
    private static final File accountsFile = new File("DeepMail/Accounts.json");
    //private static final File accountsFile = new File("Accounts.json");
    private static final Path filesPath = Paths.get("files");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        accounts = new ConcurrentHashMap<>();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        if (!Files.exists(filesPath)) {
            Files.createDirectories(filesPath);
        }

        loadData();
        int portNumber = 1337;
        listen(portNumber);
        saveData();
    }

    private static void listen(int portNumber) {
        try (final ServerSocket ss = new ServerSocket(portNumber, 0, InetAddress.getByName("localhost"))) {
            System.out.println("Server started");

            while (true) {
                System.out.println("Waiting for connection...");
                try {
                    //new Thread(new ClientHandler(ss.accept())).start();
                    int result = listenSocket(ss.accept());
                    if (result == -1) {
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("HI");
                }
            }

        } catch (IOException i) {
            System.out.println("Something went wrong in a serversocket");
        }
    }

    private static int listenSocket(Socket socket) {

        try (socket; ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            // TODO: Kas siin võiks kasutada Enumi?
            switch (in.readInt()) {
                case 1: returnAccount(in, out); break;
                case 2: syncAccount(in, out); break;
                case 3: newAccount(socket, in, out); break;
                case 4: sendFiles(in, out); break;
                case 5: saveFiles(in); break;
                case 6: deleteFiles(in); break;
                case 7: shareFiles(in); break;
                case -1:
                    out.writeInt(DMExitCode.OK);
                    out.flush();
                    return DMExitCode.EXITMENU;
                default:
                    // Mitte-ettenähtud sisend, karista kohe häkkimise eest (rünnakuga?)
                    break;
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return DMExitCode.SOFTWARE;
        }

        System.out.println("Connection closed\n");
        return DMExitCode.OK;
    }


    private static void loadData(){
        if (!accountsFile.exists()) {
            return;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(accountsFile);
            if (rootNode.has("accounts")) {

                List<Account> accountsList = objectMapper.convertValue(
                        rootNode.get("accounts"), new TypeReference<List<Account>>(){});
                accounts = accountsList.stream()
                        .collect(Collectors.toConcurrentMap(Account::getUsername, a->a));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveData() {
        ArrayNode accountsNode = objectMapper.valueToTree(accounts.values());
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("accounts", accountsNode);
        try {

            JsonGenerator fileGenerator = objectMapper.getFactory()
                    .createGenerator(accountsFile, JsonEncoding.UTF8)
                    .useDefaultPrettyPrinter();
            objectMapper.writeTree(fileGenerator, rootNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean validateLogin(String username, byte[] password) {
        Account acc = accounts.getOrDefault(username, null);
        return acc != null && Arrays.equals(acc.getHashedPassword(), password);
    }

    private static boolean validateLogin(Account account) {
        return validateLogin(account.getUsername(), account.getHashedPassword());
    }


    private static void newAccount(Socket socket, ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        do {
            if (!accounts.containsKey(in.readUTF())) {
                break;
            }
            out.writeInt(DMExitCode.USAGE);
            out.flush();
        } while (!socket.isClosed());

        out.writeInt(DMExitCode.OK);
        out.flush();

        Account newAccount = (Account) in.readObject();
        accounts.putIfAbsent(newAccount.getUsername(), newAccount);
    }

    private static void syncAccount(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Account account = (Account) in.readObject();
        // TODO: Teha nii, et iga operatsioon synciks ise uued andmed

        if (validateLogin(account)) {
            Map<String, SharedFile> userFiles = account.getFiles();
            Map<String, SharedFile> serverFiles = accounts.get(account.getUsername()).getFiles();

            for (String key : serverFiles.keySet()) {
                if (!userFiles.containsKey(key)
                        || serverFiles.get(key).getModified().isAfter(userFiles.get(key).getModified())) {
                    account.addFile(serverFiles.get(key));
                }
            }
            accounts.put(account.getUsername(), account);
        }
        out.writeObject(account);
    }

    private static void returnAccount(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String username = in.readUTF();
        byte[] password = in.readNBytes(in.readInt());

        if (validateLogin(username, password)) {
            out.writeObject(accounts.get(username));
        } else {
            out.writeObject(null);
        }
        out.flush();
    }


    private static void sendFolder(ObjectOutputStream out, Path[] itemPaths, Path remotePath) throws IOException {
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
        }
    }

    private static void sendFiles(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        int amount = in.readInt();
        for (int i = 0; i < amount; i++) {

            String filename = in.readUTF();
            Path newPath = filesPath.resolve(filename);

            if (Files.isDirectory(newPath)) {
                out.writeBoolean(true);
                sendFolder(out, Files.list(newPath).toArray(Path[]::new), newPath);
            } else {
                out.writeBoolean(false);
                byte[] filebytes = Files.readAllBytes(newPath);
                out.writeInt(filebytes.length);
                out.write(filebytes);
            }
            out.flush();
        }
    }

    public static void populateFolder(ObjectInputStream in, Path dir) throws IOException {
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }

        int amount = in.readInt();
        for (int i = 0; i < amount; i++) {

            Path newPath = dir.resolve(in.readUTF());
            if (in.readBoolean()) {
                populateFolder(in, newPath);
            } else {
                Files.write(newPath, in.readNBytes(in.readInt()), StandardOpenOption.CREATE);
                System.out.println("Saved file " + newPath.toString());
            }
        }
    }

    private static void saveFiles(ObjectInputStream in) throws IOException {
        populateFolder(in, filesPath.resolve(Paths.get(in.readUTF())));
    }


    private static void deleteFiles(ObjectInputStream in) throws IOException {
        int amount = in.readInt();
        for (int i = 0; i < amount; i++) {
            String username = in.readUTF();
            String filepath = in.readUTF();
            Files.walk(filesPath.resolve(username).resolve(filepath))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            accounts.get(username).removeFile(filepath);
        }
    }

    private static void shareFiles(ObjectInputStream in) throws IOException {
        String fromuser = in.readUTF();
        String touser = in.readUTF();
        int amount = in.readInt();

        for (int i = 0; i < amount; i++) {
            String path = in.readUTF();
            boolean readonly = in.readBoolean();
            accounts.get(touser).addFile(new SharedFile(fromuser, path, readonly));
        }
    }
}
