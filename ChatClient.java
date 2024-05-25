import java.io.*;
import java.net.*;
import java.util.UUID;

public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader stdIn;
    private Socket socket;
    private String clientId;
    private String userName;

    public ChatClient(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        clientId = UUID.randomUUID().toString();
        System.out.print("Digite seu nome: ");
        userName = stdIn.readLine();
    }

    private void start() {
        new Thread(new IncomingReader()).start();

        String userInput;
        try {
            while ((userInput = stdIn.readLine()) != null) {
                out.println(clientId + ":" + userName + ": " + userInput);
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
                    // Extrai o clientId da mensagem recebida
                    String receivedClientId = message.substring(0, message.indexOf(":"));
                    
                    // Verifica se o clientId recebido é diferente do clientId deste cliente
                    if (!receivedClientId.equals(clientId)) {
                        // Exibe a mensagem sem o clientId
                        System.out.println("Mensagem de " + message.substring(message.indexOf(":") + 1));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java ChatClient <endereço do servidor> <número da porta>");
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
