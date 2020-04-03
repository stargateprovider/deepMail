import Commands.CommandExecutor;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class GoogleAuth {

    public static void main(String[] args) throws Exception {
        authenticate();
    }

    public static String authenticate() throws IOException, InterruptedException, URISyntaxException {
        String auth_url = "https://accounts.google.com/o/oauth2/v2/auth";
        String requestBody = "?response_type=code&" +
                "client_id=768680996017-lvefegrclc82u278i4f3olafolaufdp8.apps.googleusercontent.com&" +
                "redirect_uri=http%3A//localhost%3A1337&" +
                "scope=email";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(auth_url + requestBody).toURI())
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1337"))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.uri() + "\n" + response.headers() + "\n" + response.body());
        return response.body();
    }

    public static void login() throws MessagingException {

        String oauth2_access_token = "4%2FyAE0LBAGiaYYN50iAANQFmucMlYJo2LqFT_6TyE3jAbz0qiXLiq-8fLupqupITud5R160t8OkF7_tsMNsl77nZ4";
        oauth2_access_token = "!ChRjR0ZKN3hQZHJwUnBGdEtzcW80bRIfYy0tVWE5S3UtRUVlOERFdWhZOThQY19WbFRISkVSYw%E2%88%99AF-3PDcAAAAAXn9q3puy0PaBFO-5si4tte4A_P6Jr5qF";
        //oauth2_access_token = "nqFwLtXS5rqPjuhkRrWwzCOzn7ZCOztkAY0q9WjU3yQ";

        Properties props = new Properties();
        props.put("mail.imap.ssl.enable", "true"); // required for Gmail
        props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        Session session = Session.getInstance(props);
        Store store = session.getStore("imap");
        store.connect("imap.gmail.com", CommandExecutor.quickInput("Enter username: "), oauth2_access_token);
        for (Folder folder : store.getDefaultFolder().list("*")) {
            if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
                System.out.println(folder.getFullName() + ": " + folder.getMessageCount());
            }
        }
    }
}