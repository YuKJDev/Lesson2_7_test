import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientApplicationOne {

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    ClientHandler ch;

    public ClientApplicationOne(Socket socket, DataInputStream in, DataOutputStream out, ClientHandler ch) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.ch = ch;
        connect();
    }

    public void connect () {


        new Thread(() -> {
                while (true) {
                    try {
                        System.out.println("For authorization type -auth login password");
                        in.readUTF();
                        loadHistory();
                   } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }).start();

    }

    private void SaveHistory() {

        try {
            File history = new File("history.log");
            if (!history.exists()) {
                System.out.println("There is no history file, let's create it");
                history.createNewFile();
            }
            PrintWriter fileWriter = new PrintWriter(new FileWriter(history, false));

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(in.readUTF());
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory() throws IOException {
        int posHistory = 100;
        File history = new File("history.log");
        List<String> historyList = new ArrayList<>();
        FileInputStream in = new FileInputStream(history);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder buf = new StringBuilder();

        String temp;
        while ((temp = bufferedReader.readLine()) != null) {
            historyList.add(temp);
        }

        if (historyList.size() > posHistory) {
            for (int i = historyList.size() - posHistory; i <= (historyList.size() - 1); i++) {
                buf.append(historyList.get(i) + "\n");

            }

        } else {
            for (int i = 0; i < posHistory; i++) {
                System.out.println(historyList.get(i));
            }
        }
    }


}


