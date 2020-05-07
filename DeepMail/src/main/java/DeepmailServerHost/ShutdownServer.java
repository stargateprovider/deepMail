package DeepmailServerHost;

import Commands.DMExitCode;
import picocli.CommandLine.Command;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

@Command(name = "closeserver", description = "Sends the shutdown signal to server.")
public class ShutdownServer implements Callable<Integer> {

    @Override
    public Integer call() {
        try(final Socket socket = new Socket("127.0.0.1", 1337);
            final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            // TODO: Siin võiks kontrollida, kas kasutaja on admin, ja/või siis server küsib parooli
            out.writeInt(-1);
            out.flush();
            if (in.readInt() != DMExitCode.OK) {
                throw new IOException("Shutdown failed!");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return DMExitCode.SOFTWARE;
        }
        return DMExitCode.OK;
    }
}
