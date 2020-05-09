package Commands;

import picocli.CommandLine.Command;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "addemail", description = "Add an email account to your DeepMail account")
public class AddEmail implements Callable<Integer> {

    private LoginAccount currentLogin;

    public AddEmail(LoginAccount currentLogin) {
        this.currentLogin = currentLogin;
    }
    public AddEmail() {}

    @Override
    public Integer call() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        if (!currentLogin.isLoggedIn()) {
            System.out.println("Login to your DeepMail account to add email accounts");
            return DMExitCode.USAGE;
        }



        SecretKeySpec keySpec = new SecretKeySpec(currentLogin.getPw(), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        String address = CommandExecutor.quickInput("> Write your email domain: ");
        // TODO: passwordi küsimine võiks käia ilma Stringita
        String password = CommandExecutor.quickInput("> Write your password: ");
        byte[] encryptedPassword = cipher.doFinal(password.getBytes());

        currentLogin.getAccount().addEmail(new Email(address, encryptedPassword));
        return DMExitCode.OK;
    }
}
