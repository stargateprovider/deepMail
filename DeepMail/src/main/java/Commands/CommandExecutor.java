package Commands;

import picocli.CommandLine;

import java.io.Console;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Loob programmi sees käsurea, millelt võib etteantud käske sisestada
 */
public class CommandExecutor {
    HashMap<String, Callable<Integer>> commands;
    final static Scanner globalScanner = new Scanner(System.in);

    // Loob antud commandidega käsurea
    public CommandExecutor(HashMap<String, Callable<Integer>> commands) {
        this.commands = commands;

        Back backCmd = new Back();
        this.commands.put("back", backCmd);
        this.commands.put("close", backCmd);

        String helpMsg = "Available commands:\n" + String.join("|", commands.keySet().toArray(String[]::new));
        Help helpCmd = new Help(helpMsg);
        this.commands.put("help", helpCmd);
        this.commands.put("?", helpCmd);
    }
    public CommandExecutor() {
        this(new HashMap<>());
    }

    public void add(String name, Callable<Integer> command) {
        commands.put(name, command);
    }

    // Käivitab käsurea
    public void run() {
        int result = DMExitCode.USAGE;
        while (result != DMExitCode.EXITMENU) {
            System.out.print("> ");
            String input = globalScanner.nextLine();
            result = execute(input);
        }
    }

    public int execute(String cmdName, String argsString) {
        if (cmdName.isEmpty()) {
            return DMExitCode.OK;
        }
        if (commands.containsKey(cmdName)) {
            String[] options = new String[0];
            if (!argsString.isEmpty()) {
                options = Pattern.compile("\"([^\"]*)\"|(\\S+)")
                        .matcher(argsString)
                        .results()
                        .map(m -> m.group(1) != null ? m.group(1) : m.group(2))
                        .toArray(String[]::new);
            }
            return new CommandLine(commands.get(cmdName)).execute(options);
        }

        System.out.println("Command '" + cmdName + "' not found.");
        return DMExitCode.USAGE;
    }

    public int execute(String inputStr) {
        String[] input = inputStr.split(" ", 2);
        if (input.length > 1) {
            return execute(input[0], input[1]);
        }
        return execute(input[0], "");
    }

    // Numbriga loendist valimine
    public static int quickChoice(List<String> options, String separator) {
        String optionsString = "";
        for (int i = 0; i < options.size(); i++) {
            optionsString += (i + 1) + ") " + options.get(i);
            if (i + 1 < options.size()) {
                optionsString += separator;
            }
        }
        System.out.print(optionsString + "\nEnter nr: ");

        while (true) {
            String input = globalScanner.nextLine();
            if (input.matches("\\d+")) {
                int option = Integer.parseInt(input);
                if (0 < option && option <= options.size())
                    return option - 1;
            }
            System.out.print("No such option.\n> ");
        }
    }

    public static String quickInput(String prompt) {
        System.out.print(prompt);
        return globalScanner.nextLine();
    }

    public static char[] readPassword() {
        System.out.print("Enter password: ");
        Console console = System.console();
        if (console != null) {
            return console.readPassword();
        }
        return globalScanner.nextLine().toCharArray();
    }
}
