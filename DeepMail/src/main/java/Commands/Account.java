package Commands;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.w3c.dom.ls.LSOutput;
import picocli.CommandLine.Command;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "newaccount", description = "Create a new DeepMail account")
public class Account implements Callable<Integer>, Serializable {
    private String username;
    private byte[] hashedPassword;

    private List<Email> emailsList;
    private List<FilePermission> filePermissions;

    public static Account getAccount(String username, char[] password) {
        try (final Socket socket = new Socket("127.0.0.1", 1337);
             final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             final ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(1);
            out.writeUTF(username);
            out.writeInt(password.length);
            out.write(hashPassword(password));
            out.flush();

            return (Account) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer call() {
        return createAccount();
    }

    private int createAccount() {
        // TODO: Salvesta serveriaadress kuhugi
        try (final Socket socket = new Socket("127.0.0.1", 1337);
             final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             final ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(3);
            usernameValidation: do {
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
            filePermissions = new ArrayList<>();
            hashedPassword = hashPassword(password);
            out.writeObject(this);
            out.flush();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return DMExitCode.SOFTWARE;
        }
        return DMExitCode.OK;
    }

    void saveAccount() {
        try (Socket socket = new Socket("127.0.0.1", 1337);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(2);
            out.writeObject(this);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] hashPassword(char[] password) {
        ByteBuffer buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        byte[] hashed = new byte[buf.limit()];
        buf.get(hashed);
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
        this.emailsList.forEach(System.out::println);
    }
}
