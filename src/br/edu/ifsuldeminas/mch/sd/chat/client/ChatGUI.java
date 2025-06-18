package br.edu.ifsuldeminas.mch.sd.chat.client;

import br.edu.ifsuldeminas.mch.sd.chat.ChatException;
import br.edu.ifsuldeminas.mch.sd.chat.ChatFactory;
import br.edu.ifsuldeminas.mch.sd.chat.MessageContainer;
import br.edu.ifsuldeminas.mch.sd.chat.Sender;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChatGUI extends JFrame implements MessageContainer {

    private JTextField localPortField;
    private JTextField remoteIpField;
    private JTextField remotePortField;
    private JCheckBox protocolCheckBox;
    private JTextArea messageDisplayArea;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton connectButton;
    private JTextField nameField;
    private String userName;

    private Sender sender;
    private int localPort;
    private int remotePort;

    public ChatGUI() {
        super("Pedro Ferreira Franco - CHAT UDP/TCP");
        setSize(600, 500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        Color backgroundColor = Color.WHITE;
        Color accentColor = Color.ORANGE;
        getContentPane().setBackground(backgroundColor);

        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBackground(backgroundColor);
        Border orangeBorder = BorderFactory.createLineBorder(accentColor, 1);
        connectionPanel.setBorder(BorderFactory.createTitledBorder(orangeBorder, " Configurações de Conexão "));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        connectionPanel.add(new JLabel("Seu Nome:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3; 
        nameField = new JTextField("", 10);
        connectionPanel.add(nameField, gbc);
        gbc.gridwidth = 1; 
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        connectionPanel.add(new JLabel("Porta Local:"), gbc);

        gbc.gridx = 1;
        localPortField = new JTextField("5000", 10);
        connectionPanel.add(localPortField, gbc);

        gbc.gridx = 2;
        connectionPanel.add(new JLabel("IP Remoto:"), gbc);

        gbc.gridx = 3;
        remoteIpField = new JTextField("localhost", 10);
        connectionPanel.add(remoteIpField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        connectionPanel.add(new JLabel("Porta Remota:"), gbc);

        gbc.gridx = 1;
        remotePortField = new JTextField("5001", 10);
        connectionPanel.add(remotePortField, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        protocolCheckBox = new JCheckBox("Usar TCP");
        protocolCheckBox.setBackground(backgroundColor);
        connectionPanel.add(protocolCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        connectButton = new JButton("Conectar");
        connectButton.setBackground(accentColor);
        connectButton.setForeground(Color.BLACK);
        connectionPanel.add(connectButton, gbc);
        
        add(connectionPanel, BorderLayout.NORTH);

        messageDisplayArea = new JTextArea();
        messageDisplayArea.setEditable(false);
        messageDisplayArea.setLineWrap(true);
        messageDisplayArea.setWrapStyleWord(true);
        messageDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(messageDisplayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(orangeBorder, " Mensagens "));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(backgroundColor);
        inputPanel.setBorder(BorderFactory.createTitledBorder(orangeBorder, " Escreva sua mensagem "));

        messageInputField = new JTextField();
        messageInputField.setEnabled(false);
        inputPanel.add(messageInputField, BorderLayout.CENTER);

        sendButton = new JButton("Enviar");
        sendButton.setEnabled(false);
        sendButton.setBackground(accentColor);
        sendButton.setForeground(Color.BLACK);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        addListeners();
    }

    private void addListeners() {
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    userName = nameField.getText().trim();
                    if (userName.isEmpty()) {
                        JOptionPane.showMessageDialog(ChatGUI.this,
                                "O campo 'Seu Nome' não pode estar vazio.",
                                "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    localPort = Integer.parseInt(localPortField.getText());
                    remotePort = Integer.parseInt(remotePortField.getText());
                    String remoteIp = remoteIpField.getText();
                    boolean useTcp = protocolCheckBox.isSelected();

                    if (remoteIp.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(ChatGUI.this,
                                "O campo 'IP Remoto' não pode estar vazio.",
                                "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    sender = ChatFactory.build(useTcp, remoteIp, remotePort, localPort, ChatGUI.this);

                    nameField.setEditable(false); // Desabilita o campo de nome
                    localPortField.setEditable(false);
                    remoteIpField.setEditable(false);
                    remotePortField.setEditable(false);
                    protocolCheckBox.setEnabled(false);
                    connectButton.setEnabled(false);

                    messageInputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    messageInputField.requestFocusInWindow();

                    String protocol = useTcp ? "TCP" : "UDP";
                    newMessage(String.format("Conectado com sucesso como '%s' via %s!", userName, protocol));

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ChatGUI.this,
                            "Por favor, insira números de porta válidos.",
                            "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                } catch (ChatException ex) {
                    JOptionPane.showMessageDialog(ChatGUI.this,
                            "Erro ao iniciar o chat: " + ex.getMessage() + "\nCausa: " + ex.getCause().getMessage(),
                            "Erro no Chat", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        ActionListener sendActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageText = messageInputField.getText();
                if (!messageText.trim().isEmpty()) {
                    try {
                        // --- INÍCIO DA MODIFICAÇÃO (FORMATA A MENSAGEM COM O NOME) ---
                        String formattedMessage = userName + ": " + messageText;
                        sender.send(formattedMessage);
                        // --- FIM DA MODIFICAÇÃO ---

                        // Exibe a mensagem localmente como "Você"
                        messageDisplayArea.append("Você: " + messageText + "\n");
                        messageInputField.setText("");
                    } catch (ChatException ex) {
                        JOptionPane.showMessageDialog(ChatGUI.this,
                                "Erro ao enviar mensagem: " + ex.getMessage(),
                                "Erro de Envio", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        sendButton.addActionListener(sendActionListener);
        messageInputField.addActionListener(sendActionListener);
    }

    @Override
    public void newMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            // A mensagem recebida já virá formatada com o nome do remetente
            messageDisplayArea.append(message + "\n");
            messageDisplayArea.setCaretPosition(messageDisplayArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatGUI().setVisible(true);
        });
    }
}
