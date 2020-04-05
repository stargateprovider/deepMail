import Commands.CommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Account implements Callable<Integer> {

    private String username;
    private byte[] hashedPassword;

    private List<Email> emailsList;

    public List<Email> getEmailsList() {
        return emailsList;
    }

    public void addEmail(String domain, String password) {
        this.emailsList.add(new Email(domain, password));
    }

    @Override
    public Integer call(){
       return createAccount();
    }

    private int createAccount() {
        String username = CommandExecutor.quickInput("Write your username");
        String password = CommandExecutor.quickInput("Write your password");
        String passwordAgain = CommandExecutor.quickInput("Write your password once again");

        if(password.equals(passwordAgain)){
            this.username = username;
            this.hashedPassword = password.getBytes();
            this.emailsList = new ArrayList<>();
            return 0;
        }else{
            // TODO: 05.04.20 Teha tsüklisse korduvalt küsima
            System.out.println("Passwords didn't match!");
            return 1;
        }
    }
}
