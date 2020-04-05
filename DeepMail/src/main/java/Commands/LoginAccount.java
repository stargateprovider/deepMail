package Commands;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "loginAccount", mixinStandardHelpOptions = true)
public class LoginAccount implements Callable<Integer> {

    @CommandLine.Parameters(arity = "1")
    String username;

    @CommandLine.Parameters(arity = "2")
    String password;


    @Override
    public Integer call() {
        //Pooleli
        return 1;

    }
}
