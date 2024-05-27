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
    private JTextField portField;
    private JTextPane chatArea;
    private StyledDocument chatDocument;
    private JButton connectButton;
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
        setTitle("Chat Client");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel for user inputs
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        userNameField = new JTextField();
        portField = new JTextField();
        connectButton = new JButton("Connect");

        inputPanel.add(new JLabel("User Name:"));
        inputPanel.add(userNameField);
        inputPanel.add(new JLabel("Port:"));
        inputPanel.add(portField);
        inputPanel.add(new JLabel()); // Placeholder for grid alignment
        inputPanel.add(connectButton);

        // Adding components to the frame
        add(inputPanel, BorderLayout.NORTH);
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatDocument = chatArea.getStyledDocument();
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Message field at the bottom
        messageField = new JTextField();
        add(messageField, BorderLayout.SOUTH);

        // Connect button action
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    connectToServer();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erro ao conectar ao servidor: " + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Message field action
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        createStyles();

        setVisible(true);
    }

    private void createStyles() {
        Style style = chatArea.addStyle("UserStyle", null);
        StyleConstants.setForeground(style, Color.BLUE);

        style = chatArea.addStyle("OtherUserStyle", null);
        StyleConstants.setForeground(style, Color.RED);
    }

    private void connectToServer() throws IOException {
        if (client == null || client.socket.isClosed()) { // Verifica se o cliente já está conectado ou não
            String userName = userNameField.getText();
            int port = Integer.parseInt(portField.getText());
            client = new ChatClient("localhost", port, userName, chatArea);
            client.start();
            connectButton.setEnabled(false); // Desabilita o botão após a conexão
            JOptionPane.showMessageDialog(this, "Conexão bem-sucedida!", "Conexão", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Você já está conectado!", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
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
