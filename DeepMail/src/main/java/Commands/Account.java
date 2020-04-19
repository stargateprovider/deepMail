package Commands;

import picocli.CommandLine;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "createaccount", mixinStandardHelpOptions = true)
public class Account implements Callable<Integer>, Serializable {

    private static List<Account> accounts;

    private String username;
    private byte[] hashedPassword;

    private List<Email> emailsList;

    public static Account getAccount(String username, String password) {

        try(Socket socket = new Socket("127.0.0.1", 1337);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            out.writeInt(1);
            out.writeUTF(username);
            out.writeUTF(password);
            out.flush();

            return (Account) in.readObject();

        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }

    }

    private void saveData() {

        try(Socket socket = new Socket("127.0.0.1", 1337);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            out.writeInt(2);
            out.writeObject(this);
            out.flush();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public Integer call(){
       return createAccount();
    }

    private int createAccount() {
        String username = CommandExecutor.quickInput("> Write your username: ");
        while(true) {
            String password = CommandExecutor.quickInput("> Write your password: ");
            String passwordAgain = CommandExecutor.quickInput("> Write your password once again: ");
            if (password.equals(passwordAgain)) {
                this.username = username;
                this.hashedPassword = password.getBytes();
                this.emailsList = new ArrayList<>();
                saveObject(this);
                return 0;
            } else {
                System.out.println("Passwords didn't match!");
            }
        }
    }

    private void saveObject(Account account) {

        try(Socket socket = new Socket("127.0.0.1", 1337);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            out.writeInt(3);
            out.writeObject(account);
            out.flush();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public List<Email> getEmailsList() {
        return emailsList;
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

    public void addEmail(Email email) {
        this.emailsList.add(email);
        saveData();
    }
}
