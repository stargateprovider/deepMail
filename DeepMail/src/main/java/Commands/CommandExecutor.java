package Commands;

import picocli.CommandLine;

import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Loob programmi sees käsurea, millelt võib etteantud käske sisestada
 */
public class CommandExecutor {
    HashMap<String, Callable<Integer>> commands;
    static Scanner globalScanner = new Scanner(System.in);

    public CommandExecutor(HashMap<String, Callable<Integer>> commands) {
        this.commands = commands;
        this.commands.put("back", new Back());
        this.commands.put("close", new Back());
    }

    public void run() {
        int result = 0;
        while (result != -1) {
            System.out.print("> ");
            String input = globalScanner.nextLine();
            result = execute(input);
        }
    }

    public int execute(String cmdName, String argsString) {
        if (commands.containsKey(cmdName)) {
            String[] options = new String[0];
            if (!argsString.isEmpty()) {
                options = Pattern.compile("\"([^\"]*)\"|(\\S+)")
                        .matcher(argsString)
                        .results()
                        .map(MatchResult::group)
                        .toArray(String[]::new);
            }
            return new CommandLine(commands.get(cmdName)).execute(options);
        }

        System.out.println("Command '" + cmdName + "' not found.");
        return 2;
    }

    public int execute(String inputStr) {
        String[] input = inputStr.split(" ", 2);
        if (input.length > 1) {
            return execute(input[0], input[1]);
        }
        return execute(input[0], "");
    }
}
