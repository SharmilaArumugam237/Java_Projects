import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Server extends JFrame implements ActionListener {
    private JPanel messagePanel;
    private JTextField inputField;
    private JButton sendButton;

    ServerSocket serverSocket;
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    public Server() {
        setTitle("Server Chat");
        setSize(400, 600);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(messagePanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        inputField.addActionListener(this);

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        setVisible(true);

        try {
            serverSocket = new ServerSocket(6001);
            System.out.println("Waiting for client...");
            socket = serverSocket.accept();
            System.out.println("Client connected.");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Thread to read messages
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = reader.readLine()) != null) {
                        addMessage(msg, false); // false = received (left)
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String msg, boolean isSentByMe) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(isSentByMe ? new Color(37, 211, 102) : new Color(230, 230, 230));
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubble.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));

        JLabel messageLabel = new JLabel("<html><p style=\"width: 200px;\">" + msg + "</p></html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        messageLabel.setForeground(isSentByMe ? Color.WHITE : Color.BLACK);

        bubble.add(messageLabel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        if (isSentByMe) {
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            wrapper.add(bubble, BorderLayout.WEST);
        }
        messagePanel.add(wrapper);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.revalidate();

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) messagePanel.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            addMessage(msg, true);
            writer.println(msg);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
