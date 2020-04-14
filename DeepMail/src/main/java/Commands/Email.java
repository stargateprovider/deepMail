package Commands;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "addemail", mixinStandardHelpOptions = true)
public class Email implements Callable<Integer> {

        private String emailDomain;
        private byte[] hashedPassword;



        public String getEmailDomain() {
            return emailDomain;
        }

        public byte[] getHashedPassword() {
            return hashedPassword;
        }

        @Override
        public Integer call(){

            if(!LoginAccount.isLoggedIn()){
                System.out.println("You need to login first");
                return 2;
            }

            emailDomain = CommandExecutor.quickInput("> Write your email domain: ");
            String password = CommandExecutor.quickInput("> Write your password: ");
            hashedPassword = password.getBytes();

            Account account = Account.getAccount(LoginAccount.getUsername(), LoginAccount.getPassword());
            if(account != null) account.addEmail(this);

            return 0;

        }
}
