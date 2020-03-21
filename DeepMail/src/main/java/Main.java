import picocli.CommandLine;
import Commands.*;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class Main {
    public static void main(String[] args) {
        printHeader(30);
        //test();

        HashMap<String, Callable<Integer>> commands = new HashMap<>();
        commands.put("printecho", new PrintEcho());
        commands.put("readmail", new ReadMail());

        CommandExecutor cmdExecutor = new CommandExecutor(commands);
        cmdExecutor.run();
    }

    public static void printHeader(int width) {
        System.out.println("=".repeat(width));
        System.out.printf("%" + width / 2 + "s%-" + width / 2 + "s\n", "Deep", "Mail");
        System.out.println("=".repeat(width));
        //String cmds = String.join("|", "list", "read", "sync", "write");
    }

    @CommandLine.Command(name = "test", mixinStandardHelpOptions = true)
    public static void test() {
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
    }
}
