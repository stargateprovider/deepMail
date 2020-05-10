package Commands.AdminCommands;

import Commands.DMExitCode;
import Commands.ServerCommunicator;
import picocli.CommandLine.Command;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

@Command(name = "closeserver", description = "Sends the shutdown signal to server.")
public class ShutdownServer extends ServerCommunicator {

    @Override
    public Integer call() {
        return accessServer((in, out) -> {
            // TODO: Siin võiks kontrollida, kas kasutaja on admin, ja/või siis server küsib parooli
            out.writeInt(-1);
            out.flush();
            if (in.readInt() != DMExitCode.OK) {
                throw new IOException("Shutdown failed!");
            }
        });
    }
}
