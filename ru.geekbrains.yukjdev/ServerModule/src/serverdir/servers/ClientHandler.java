import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientHandler {
    private final ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);
    public boolean isClosedConnections;
    String name;
    DataInputStream in;
    DataOutputStream out;
    Socket socket;
    Server server;
    boolean authorized = false;

    public ClientHandler(Socket socket, Server server){
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            if (isAuthorized()) {
                start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public void start() {
        setAuthorized(true);
        Thread th = new Thread(() -> {
            try {
                while (true) {
                    try {
                        authenticate();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (socket.isConnected()) {
                        try {
                            readMessage();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            } finally {
                setAuthorized(false);
                closeConnections();
            }

        });
        th.start();
    }

    public String getName() {
        return name;
    }

    public void authenticate() throws IOException {
        //   checkGuestAuth("You have two minutes to authorize, otherwise the connection will be closed.");
            System.out.println("Client auth is on going...");
            String loginInfo;
            loginInfo = in.readUTF();
            if (loginInfo.startsWith("-auth")) {
                String[] splittedLoginInfo = loginInfo.split(" ");

                User maybeClient = server.getAuthenticationService().findByLoginAndPassword(
                        splittedLoginInfo[0],
                        splittedLoginInfo[1]
                );

                if (maybeClient != null) {
                    if (!server.checkLogin(maybeClient.getLogin())) {
                        sendMessage("status: auth ok");
                        name = maybeClient.getName();
                        server.broadcast(String.format("%s came in", name));
                        System.out.println("Client auth completed");
                        server.subscribe(this);
                    }

                    } else {
                        sendMessage(String.format("%s already logged in", maybeClient.getName()));
                    }

                } else if (socket.isClosed()) {
                    return;
                } else {
                    sendMessage("Incorrect credentials");
                    checkGuestAuth("You have two minutes to authorize, otherwise the connection will be closed.");

                }

    }

    public void checkGuestAuth(String messageIn) {
        sendMessage(messageIn);
        scheduledExecutor.schedule(this::closeConnections, 10, TimeUnit.SECONDS);
        scheduledExecutor.shutdown();
    }

    public void closeConnections() {

        server.broadcast(String.format("%s left", name));
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.unsubscribe(this);
        scheduledExecutor.shutdown();
        isClosedConnections = true;
        System.exit(0);


    }

    public void readMessage() throws IOException {

        while (true) {
            String message = in.readUTF();
            if (message.startsWith("-")) {
                if (message.equalsIgnoreCase("-exit")) {
                    closeConnections();
                    break;
                }
            } else {
                //    System.out.println(message);
                if (message.startsWith("/w")) {
                    checkPrivateMessage(message);
                }
                continue;
            }
            String formatterMessage = String.format("Message from %s: %s", name, message);
            System.out.println(formatterMessage);

            server.broadcast(formatterMessage);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPrivateMessage(final String messageIn) {

        final String[] prvMsg = messageIn.split(" ", 3);
        server.privateMsg(this, prvMsg[1], prvMsg[2]); // Посылаем сообщение конкретному пользователю
    }

}