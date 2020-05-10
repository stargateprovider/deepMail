package Commands.FileCommands;

import Commands.Account;
import Commands.ServerCommunicator;
import picocli.CommandLine.*;


@Command(name = "share", description = "Share files or folders with other users")
public class Share extends ServerCommunicator {
    @Parameters(index = "0", description = "User, with whom to share")
    String username;
    @Parameters(index = "1", arity = "1..*", description = "Numbers of the files you want to share")
    int[] itemNumbers;

    @Option(names = {"-r", "--readonly"}, defaultValue = "true", description = "Give user readonly access")
    boolean readonly;

    Account account;
    public Share(Account account) {
        this.account = account;
    }

    @Override
    public Integer call() {
        return accessServer((in, out) -> {
            out.writeInt(7);
            out.writeUTF(account.getUsername());
            out.writeUTF(username);

            String[] filenames = account.getFiles().keySet().toArray(String[]::new);
            out.writeInt(itemNumbers.length);
            for (int nr : itemNumbers) {
                out.writeUTF(filenames[nr - 1]);
                out.writeBoolean(readonly);
            }
            out.flush();
        });
    }
}
