package Commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public abstract class ServerCommunicator implements Callable<Integer>{
    private static final String defaultErrorMsg = "Error occurred when communicating with server.";
    private static final String host = "127.0.0.1";
    private static final int port = 1337;
    protected static Object returnObject;

    protected static int accessServer(ObjectStreamConsumer consumer, String errorMsg) {
        try (final Socket socket = new Socket(host, port);
             final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             final ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            consumer.accept(in, out);

        } catch (Exception e) {
            System.out.println(errorMsg + '\n' + e.getMessage());
            return DMExitCode.SOFTWARE;
        }
        return DMExitCode.OK;
    }

    protected static int accessServer(ObjectStreamConsumer consumer) {
        return accessServer(consumer, defaultErrorMsg);
    }

    @FunctionalInterface
    public interface ObjectStreamConsumer {
        void accept(ObjectInputStream in, ObjectOutputStream out)
                throws IOException, ClassNotFoundException, NoSuchAlgorithmException;
    }
}