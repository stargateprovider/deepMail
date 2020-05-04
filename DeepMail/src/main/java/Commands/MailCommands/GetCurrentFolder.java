package Commands.MailCommands;

import Commands.DMExitCode;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "folder", description = {"Display current folder's name"})
public class GetCurrentFolder implements Callable<Integer> {
    FolderNavigation folderNav;

    public GetCurrentFolder(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() {
        System.out.println(folderNav.currentFolder);
        return DMExitCode.OK;
    }
}
