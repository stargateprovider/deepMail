package Commands;

import picocli.CommandLine;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(description = "Prints (echoes) the provided string.",
        name = "printecho", mixinStandardHelpOptions = true)
public class PrintEcho implements Callable<Integer> {

    @Option(names = {"-d", "--delay"}, description = "echo delay (ms) [ei tee midagi]")
    private int delay = 0;

    @Parameters(paramLabel = "STRING", description = "string to echo", arity = "1")
    private String msg;

    /*@Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;*/

    public static void main(String[] args) {
        System.exit(new CommandLine(new PrintEcho()).execute(args));
    }

    @Override
    public Integer call(){
        System.out.println(this.msg);
        return DMExitCode.OK;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
