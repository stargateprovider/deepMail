import Commands.CommandExecutor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "createaccount", mixinStandardHelpOptions = true)
public class Account implements Callable<Integer> {

    private String username;
    private byte[] hashedPassword;

    private List<Email> emailsList;

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

    public void addEmail(String domain, String password) {
        this.emailsList.add(new Email(domain, password));
    }

    @Override
    public Integer call(){
       return createAccount();
    }

    private int createAccount() {
        String username = CommandExecutor.quickInput("> Write your username: ");
        String password = CommandExecutor.quickInput("> Write your password: ");
        String passwordAgain = CommandExecutor.quickInput("> Write your password once again: ");

        if(password.equals(passwordAgain)){
            this.username = username;
            this.hashedPassword = password.getBytes();
            this.emailsList = new ArrayList<>();
            saveObject(this);
            return 0;
        }else{
            // TODO: 05.04.20 Teha tsüklisse korduvalt küsima
            System.out.println("Passwords didn't match!");
            return 1;
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
