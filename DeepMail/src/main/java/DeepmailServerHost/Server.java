package DeepmailServerHost;

import Commands.Account;
import Commands.DMExitCode;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
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
    private static final File accountsFile = new File("Accounts.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        accounts = new ConcurrentHashMap<>();

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
                case 2: saveAccount(in, out); break;
                case 3: newAccount(socket, in, out); break;
                case 4:
                    // Tahab faili (pull)
                    break;
                case 5:
                    // Laeb üles faili (push, teavitab, kui merge conflict)
                    break;
                case 6:
                    // Tahab kustutada faili
                    break;
                case 7:
                    // Tahab jagada faili, failiõiguste faili muudetakse
                    break;
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
            System.out.println("Something went wrong in a socket");
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
        if (acc == null) {
            return false;
        }
        return Arrays.equals(acc.getHashedPassword(), password);
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

    private static void saveAccount(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Account account = (Account) in.readObject();
        int outCode = DMExitCode.USAGE;
        if (validateLogin(account)) {
            accounts.put(account.getUsername(), account);
            outCode = DMExitCode.OK;
        }
        out.writeInt(outCode);
        out.flush();
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
}
