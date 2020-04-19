package DeepmailServerHost;

import Commands.Account;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class server {

    private static List<Account> accounts;

    public static void main(String[] args){
        int portNumber = 1337;

        listen(portNumber);
    }

    private static void listen(int portNumber){
        try(ServerSocket ss = new ServerSocket(portNumber, 0, InetAddress.getByName("localhost"))){
            System.out.println("Server started");

            while(true) {
                System.out.println("Waiting for connection...");
                try{
                    //new Thread(new ClientHandler(ss.accept())).start();
                    listenSocket(ss.accept());
                } catch (IOException e) {
                    System.out.println("HI");
                }
            }

        }catch (IOException i){
            System.out.println("Something went wrong in a serversocket");
        }
    }

    private static void listenSocket(Socket socket){



        try(socket; ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ){
            int i = in.readInt();

            if(i == 1){
                String username = in.readUTF();
                String password = in.readUTF();

                if(accounts == null){
                    accounts = loadData();
                    if(accounts == null) out.writeObject(null); out.flush();
                }

                for (Account account : accounts) {
                    if(account.getUsername().equals(username) && Arrays.equals(account.getHashedPassword(), password.getBytes())){
                        out.writeObject(account);
                        out.flush();
                    }
                }
                //getAccount
            }else if(i == 2){
                Account account = (Account) in.readObject();

                for (int j = 0; j < accounts.size(); j++) {
                    if(accounts.get(j).getUsername().equals(account.getUsername()) &&
                            Arrays.equals(accounts.get(j).getHashedPassword(), account.getHashedPassword())){
                        accounts.set(j, account);
                        saveData();
                        break;
                    }
                }
                //Save account data
            }else if(i == 3){
                Account account = (Account) in.readObject();
                saveObject(account);
                //Save new account
            }

        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("Something went wrong in a socket");
        }

        System.out.println("Conneciton closed\n");
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

    private static void saveObject(Account account) {
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
