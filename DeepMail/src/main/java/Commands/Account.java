package Commands;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "createaccount", mixinStandardHelpOptions = true)
public class Account implements Callable<Integer> {

    private static List<Account> accounts;

    private String username;
    private byte[] hashedPassword;

    private List<Email> emailsList;

    public static Account getAccount(String username, String password) {

        if(accounts == null){
            accounts = loadData();
            if(accounts == null) return null;
        }

        for (Account account : accounts) {
            if(account.username.equals(username) && Arrays.equals(account.hashedPassword, password.getBytes())){
                return account;
            }
        }

        return null;
    }

    private static List<Account> loadData() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Account> accounts;

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(new File("Accounts.json"));
            if(jsonNode.has("account")){
                accounts = objectMapper.readValue(String.valueOf(jsonNode.get("account")), new TypeReference<List<Account>>(){});
                return accounts;
            }else{
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    private static void saveData() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ArrayNode accountsNode = objectMapper.valueToTree(accounts);
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.set("account", accountsNode);

            objectMapper.writeTree(new JsonFactory().createGenerator(new File("Accounts.json"), JsonEncoding.UTF8).setPrettyPrinter(new DefaultPrettyPrinter()), objectNode);

        } catch (IOException e) {
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
        ObjectMapper objectMapper = new ObjectMapper();
        List<Account> accounts;

        try {
            JsonNode jsonNode = objectMapper.readTree(new File("Accounts.json"));
            if(jsonNode.has("account")){
                accounts = objectMapper.readValue(String.valueOf(jsonNode.get("account")), new TypeReference<List<Account>>(){});
            }else{
                accounts = new ArrayList<>();
            }

            accounts.add(account);

            ArrayNode accountsNode = objectMapper.valueToTree(accounts);
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.set("account", accountsNode);

            objectMapper.writeTree(new JsonFactory().createGenerator(new File("Accounts.json"), JsonEncoding.UTF8).setPrettyPrinter(new DefaultPrettyPrinter()), objectNode);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
