import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatClientGUI extends JFrame {
    private JTextField userNameField;
    private JTextPane chatArea;
    private StyledDocument chatDocument;
    private ChatClient client;
    private JTextField messageField;

    public ChatClientGUI() {
        createUI();
    }
    public class ChatClient {
    private String serverAddress;
    private int serverPort;
    private String userName;
    private JTextPane chatOutputArea;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatClient(String serverAddress, int serverPort, String userName, JTextPane chatOutputArea) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.userName = userName;
        this.chatOutputArea = chatOutputArea;
    }

    public void start() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        // Send the username to the server
        writer.println(userName);

        // Start a new thread to read messages from the server
        Thread listenerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        final String finalServerMessage = serverMessage; // Defina como final
                        SwingUtilities.invokeLater(() -> {
                            try {
                                int colonIndex = finalServerMessage.indexOf(":");
                                String user = finalServerMessage.substring(0, colonIndex);
                                String message = finalServerMessage.substring(colonIndex + 2);

                                Style style;
                                if (user.equals(userName)) {
                                    style = chatDocument.getStyle("UserStyle");
                                } else {
                                    style = chatDocument.getStyle("OtherUserStyle");
                                }

                                chatDocument.insertString(chatDocument.getLength(), user + ": ", style);
                                chatDocument.insertString(chatDocument.getLength(), message + "\n", null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Erro ao ler do servidor: " + e.getMessage(), "Erro de Leitura", JOptionPane.ERROR_MESSAGE);
                } finally {
                    closeConnection();
                }
            }
        });
        listenerThread.start();
    }

    private void closeConnection() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao fechar a conexão: " + e.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void sendMessage(String message) {
        if (writer != null) {
            // Inclui o nome do usuário na mensagem
            writer.println(userName + ": " + message);
        }
    }
    }

    private void createUI() {
        setTitle("ChatBox 0.2.0");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel for user inputs and room buttons
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        userNameField = new JTextField();
        inputPanel.add(new JLabel("Usuário:"));
        inputPanel.add(userNameField);

        // Panel for room buttons
        JPanel roomPanel = new JPanel(new GridLayout(1, 5));
        String[] roomNames = {"Geral", "Pendencias", "Adm", "Vendas", "Peças/Estoque", "Recepção/Expresso"};
        int[] ports = {8000, 8001, 8002, 8003, 8004, 8005}; // Exemplo de portas para cada sala

        for (int i = 0; i < roomNames.length; i++) {
            JButton roomButton = new JButton(roomNames[i]);
            int port = ports[i];
            roomButton.addActionListener(e -> connectToRoom(port));
            roomPanel.add(roomButton);
        }
        inputPanel.add(roomPanel);

        add(inputPanel, BorderLayout.NORTH);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatDocument = chatArea.getStyledDocument();
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Message input field
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        add(messageField, BorderLayout.SOUTH); // Adiciona o campo de mensagem na parte inferior

        createStyles(); // Assegura que os estilos são criados

        setVisible(true);
    }

    private void createStyles() {
        Style userStyle = chatArea.addStyle("UserStyle", null);
        StyleConstants.setForeground(userStyle, Color.BLUE);

        Style otherUserStyle = chatArea.addStyle("OtherUserStyle", null);
        StyleConstants.setForeground(otherUserStyle, Color.RED);
    }

    private void connectToRoom(int port) {
        try {
            if (client == null || client.socket.isClosed()) {
                String userName = userNameField.getText();
                client = new ChatClient("localhost", port, userName, chatArea);
                client.start();
                JOptionPane.showMessageDialog(this, "Conectado à sala na porta: " + port, "Conexão", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Você já está conectado em uma sala!", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro ao conectar ao servidor: " + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageField.setText(""); // Clear the input field after sending
        }
    }

    public static void main(String[] args) {
        new ChatClientGUI();
    }
};
