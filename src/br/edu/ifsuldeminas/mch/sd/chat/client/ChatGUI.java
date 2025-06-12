package br.edu.ifsuldeminas.mch.sd.chat.client;

import br.edu.ifsuldeminas.mch.sd.chat.ChatException;
import br.edu.ifsuldeminas.mch.sd.chat.ChatFactory;
import br.edu.ifsuldeminas.mch.sd.chat.MessageContainer;
import br.edu.ifsuldeminas.mch.sd.chat.Sender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChatGUI extends JFrame implements MessageContainer {

    private JTextField localPortField;
    private JTextField remotePortField;
    private JTextArea messageDisplayArea;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton connectButton;
    
    private Sender sender;
    private int localPort;
    private int remotePort;

    public ChatGUI() {
        super("Chat UDP");
        
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        portPanel.setBorder(BorderFactory.createTitledBorder("Configurações de Porta"));

        portPanel.add(new JLabel("Porta Local:"));
        localPortField = new JTextField("5000", 6);
        portPanel.add(localPortField);

        portPanel.add(new JLabel("Porta Remota:"));
        remotePortField = new JTextField("5001", 6);
        portPanel.add(remotePortField);

        connectButton = new JButton("Conectar");
        portPanel.add(connectButton);
        add(portPanel, BorderLayout.NORTH);

        messageDisplayArea = new JTextArea();
        messageDisplayArea.setEditable(false);
        messageDisplayArea.setLineWrap(true);
        messageDisplayArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageDisplayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mensagens"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Escreva sua mensagem"));

        messageInputField = new JTextField();
        messageInputField.setEnabled(false);
        inputPanel.add(messageInputField, BorderLayout.CENTER);

        sendButton = new JButton("Enviar");
        sendButton.setEnabled(false);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        addListeners();
    }

    private void addListeners() {
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    localPort = Integer.parseInt(localPortField.getText());
                    remotePort = Integer.parseInt(remotePortField.getText());

                    sender = ChatFactory.build(false, "localhost", remotePort, localPort, ChatGUI.this);
                    
                    localPortField.setEditable(false);
                    remotePortField.setEditable(false);
                    connectButton.setEnabled(false);

                    messageInputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    messageInputField.requestFocusInWindow();
                    
                    newMessage("Conectado com sucesso! Você pode começar a enviar mensagens.");

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
                String message = messageInputField.getText();
                if (!message.trim().isEmpty()) {
                    try {
                        sender.send(message);
                        messageDisplayArea.append("Você: " + message + "\n");
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
            messageDisplayArea.append(":> " + message + "\n");
            messageDisplayArea.setCaretPosition(messageDisplayArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatGUI().setVisible(true);
        });
    }
}
