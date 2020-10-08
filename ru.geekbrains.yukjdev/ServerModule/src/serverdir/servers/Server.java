import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {

    private final Set<ClientHandler> clientHandlers;
    private final AuthenticationService authenticationService;

    public Server() {
        this.clientHandlers = new HashSet<>();
        this.authenticationService = new AuthenticationService();
        start(8883);
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    private void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
              listenClients(serverSocket);  // порт уже открыт
        } catch (IOException e) {
            throw new RuntimeException("SWW during server start-up");
        }
    }

    private void listenClients(ServerSocket serverSocket) throws IOException {
        while (true) {
            System.out.println("Server is looking for a client...");
                Socket socket = serverSocket.accept();
                    System.out.println("Client accepted: " + socket);
                    new ClientHandler(socket, this);
        }

    }

    public void broadcast(String incomingMessage) {
        for (ClientHandler ch : clientHandlers) {
             ch.sendMessage(incomingMessage);
        }
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o : clientHandlers) {
            sb.append(o.getName() + " ");
        }
        broadcast(sb.toString());
    }

    public synchronized void subscribe(ClientHandler client) {
        clientHandlers.add(client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clientHandlers.remove(client);
    }

    public boolean checkLogin(String name) {
        for (ClientHandler ch : clientHandlers) {
            if (ch.getName().equals(name)) {
                return true;

            }
        }
        return false;
    }
    public synchronized void privateMsg(ClientHandler ch, String distClient, String message) {
        for (ClientHandler o : clientHandlers) {
            if (distClient.equalsIgnoreCase(o.getName())) {
                o.sendMessage("[" + distClient + "] <- [" + ch.getName() + "] : " + message);
                ch.sendMessage("[" + ch.getName() + "] -> [" + distClient + "] : " + message);
                return;
            }
        }
        ch.sendMessage("User " + distClient + " offline or does not exist ");
    }
}
