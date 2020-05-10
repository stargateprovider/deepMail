package Commands;

import Utilities.SharedFile;
import picocli.CommandLine.Command;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Path;
import java.util.*;

@Command(name = "newaccount", description = "Create a new DeepMail account")
public class Account extends ServerCommunicator implements Serializable {
    private String username;
    private byte[] hashedPassword;

    private List<Email> emailsList;
    private Map<String, SharedFile> files;

    public static Account getAccount(String username, char[] password) {
        accessServer((in, out) -> {

            out.writeInt(1);
            out.writeUTF(username);
            byte[] hpw = hashPassword(password);
            out.writeInt(hpw.length);
            out.write(hpw);
            out.flush();

            returnObject = in.readObject();
        });
        return (Account) returnObject;
    }

    @Override
    public Integer call() {
        return accessServer((in, out) -> {

            out.writeInt(3);
            usernameValidation:
            do {
                username = CommandExecutor.quickInput("> Write your username: ");
                out.writeUTF(username);
                out.flush();

                switch (in.readInt()) {
                    case DMExitCode.OK:
                        break usernameValidation;
                    case DMExitCode.USAGE:
                        System.out.println("Username already taken. Try another one.");
                        break;
                    default:
                        throw new IOException("Server is having problems, try again later.");
                }
            } while (true);

            char[] password;
            do {
                password = CommandExecutor.quickInput("> Write your password: ").toCharArray();
                char[] passwordAgain = CommandExecutor.quickInput("> Write your password once again: ").toCharArray();
                if (Arrays.equals(password, passwordAgain)) {
                    break;
                }
                System.out.println("Passwords didn't match!");
            } while (true);

            emailsList = new ArrayList<>();
            files = new LinkedHashMap<>();
            hashedPassword = hashPassword(password);
            out.writeObject(this);
            out.flush();

        });
    }

    public void sync() {
        accessServer((in, out) -> {
            out.writeInt(2);
            out.writeObject(this);
            out.flush();
            returnObject = in.readObject();
        });
        Account synced = (Account) returnObject;

        // Uuendatakse vajalikud väljad:
        this.files = synced.files;
    }

    public static byte[] hashPassword(char[] password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        ByteBuffer buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        byte[] hashed = new byte[buf.limit()];
        buf.get(hashed);
        hashed = digest.digest(hashed);
        return hashed;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setEmailsList(List<Email> emailsList) {
        this.emailsList = emailsList;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public List<Email> getEmailsList() {
        return new ArrayList<>(emailsList);
    }

    public void addEmail(Email email) {
        this.emailsList.add(email);
        this.sync();
    }

    public Map<String, SharedFile> getFiles() {
        return Map.copyOf(files);
    }
    public void setFiles(Map<String, SharedFile> files) {
        this.files = files;
    }

    public void addFile(SharedFile file) {
        files.put(file.toString(), file);
    }
    public void addFile(String filename, boolean readonly) {
        addFile(new SharedFile(this.username, filename, readonly));
    }
    public void addFile(Path path) {
        addFile(new SharedFile(username, path));
    }

    public void removeFile(String filename) {
        files.remove(filename);
        Set<String> fileKeys = Set.copyOf(files.keySet());
        for (String key : fileKeys) {
            if (key.contains(filename)) {
                files.remove(key);
            }
        }
    }
}
