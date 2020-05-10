package Commands.FileCommands;

import Commands.Account;
import Commands.ServerCommunicator;
import picocli.CommandLine.*;

import java.util.Arrays;

@Command(name = "delete", description = "Delete files or folders from the cloud")
public class Delete extends ServerCommunicator {
    @Parameters(arity = "1..*", description = "Numbers of the files you want to delete")
    int[] itemNumbers;

    Account account;
    public Delete(Account account) {
        this.account = account;
    }

    @Override
    public Integer call() {
        itemNumbers = Arrays.stream(itemNumbers).filter(nr -> 0 < nr && nr <= account.getFiles().size()).toArray();
        return accessServer((in, out) -> {
            out.writeInt(6);

            // Linked HashMap peaks tagama järjekorra
            String[] filenames = account.getFiles().keySet().toArray(String[]::new);
            out.writeInt(itemNumbers.length);

            for (int nr : itemNumbers) {
                out.writeUTF(account.getFiles().get(filenames[nr - 1]).getOwner());
                out.writeUTF(filenames[nr - 1]);
                account.removeFile(filenames[nr - 1]);
            }
            out.flush();
        });
    }
}
