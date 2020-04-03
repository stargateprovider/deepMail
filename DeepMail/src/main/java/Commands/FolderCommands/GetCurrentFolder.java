package Commands.FolderCommands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "folder", mixinStandardHelpOptions = true)
public class GetCurrentFolder implements Callable<Integer> {
    FolderNavigation folderNav;

    public GetCurrentFolder(FolderNavigation folderNav) {
        this.folderNav = folderNav;
    }

    @Override
    public Integer call() {
        System.out.println(folderNav.currentFolder);
        return 0;
    }
}
