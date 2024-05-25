import java.io.*;
import java.net.*;
import java.util.UUID;

public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader stdIn;
    private Socket socket;
    private String clientId;

    public ChatClient(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        clientId = UUID.randomUUID().toString();
    }

    private void start() {
        new Thread(new IncomingReader()).start();

        String userInput;
        try {
            while ((userInput = stdIn.readLine()) != null) {
                out.println(clientId + ": " + userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (!message.startsWith(clientId)) {
                        System.out.println("Received: " + message.substring(message.indexOf(":") + 2));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <server address> <port number>");
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            ChatClient client = new ChatClient(serverAddress, port);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
