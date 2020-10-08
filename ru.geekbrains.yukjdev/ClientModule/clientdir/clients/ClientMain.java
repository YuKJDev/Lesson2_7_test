import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientMain {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8883);
        Server server = new Server();
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        ClientHandler ch = new ClientHandler(socket, server);

        new ClientApplicationOne(socket, in, out, ch);
    }

}
