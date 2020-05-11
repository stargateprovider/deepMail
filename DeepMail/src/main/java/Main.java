import Commands.MailCommands.EmailLogin;
import Commands.FileCommands.FileClient;
import Commands.AdminCommands.ShutdownServer;
import Commands.MailCommands.SendFake;
import picocli.CommandLine;
import Commands.*;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class Main {
    public static void main(String[] args) {
        printHeader(30);
        //test();

        LoginAccount currentLogin = new LoginAccount();
        HashMap<String, Callable<Integer>> commands = new HashMap<>(){{
            put("readmail", new EmailLogin(currentLogin));
            //put("sendmail", new SendMail());
            put("sendfake", new SendFake());
            put("echo", new PrintEcho());
            put("createaccount", new Account());
            put("login", currentLogin);
            put("logout", new Logout(currentLogin));
            put("addemail", new AddEmail(currentLogin));
            put("files", new FileClient(currentLogin));
            put("closeserver", new ShutdownServer());
            //put("test", Main::test);
        }};

        CommandExecutor cmdExecutor = new CommandExecutor(commands);
        cmdExecutor.run();
    }

    public static void printHeader(int width) {
        System.out.println("=".repeat(width));
        System.out.printf("%" + width / 2 + "s%-" + width / 2 + "s\n", "Deep", "Mail");
        System.out.println("=".repeat(width));
    }

    @CommandLine.Command(name = "test", mixinStandardHelpOptions = true)
    public static Integer test() {
        String[] test1 = {"Hello World"};
        String[] test2 = {"-h"};
        String[] test3 = {"-p", "Hello?"};
        int result;

        System.out.println("Test 1");
        result = new CommandLine(new PrintEcho()).execute(test1);
        if (result != 0) {
            // 0 on OK, 1 tähendab viga täitmisel, 2 tähendab vale sisendit jne.
        }

        System.out.println("\n-------------------------------\n");
        System.out.println("Test 2");
        new CommandLine(new PrintEcho()).execute(test2);

        System.out.println("\n-------------------------------\n");
        System.out.println("Test 3");
        new CommandLine(new PrintEcho()).execute(test3);

        return DMExitCode.OK;
    }
}
