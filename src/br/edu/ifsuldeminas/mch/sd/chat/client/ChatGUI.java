package br.edu.ifsuldeminas.mch.sd.chat.client;

import br.edu.ifsuldeminas.mch.sd.chat.ChatException; // Correção no import
import br.edu.ifsuldeminas.mch.sd.chat.ChatFactory;
import br.edu.ifsuldeminas.mch.sd.chat.MessageContainer;
import br.edu.ifsuldeminas.mch.sd.chat.Sender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Classe ChatGUI que representa a interface gráfica do cliente de chat UDP.
 * Implementa a interface MessageContainer para exibir mensagens recebidas.
 */
public class ChatGUI extends JFrame implements MessageContainer {

    // Componentes da interface gráfica para entrada e exibição
    private JTextField localPortField;
    private JTextField remotePortField;
    private JTextArea messageDisplayArea;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton connectButton; // Botão para iniciar a conexão com o chat
    
    private Sender sender; // Objeto responsável pelo envio de mensagens
    private int localPort;
    private int remotePort;

    /**
     * Construtor da classe ChatGUI.
     * Configura a janela principal, seus componentes e o layout.
     */
    public ChatGUI() {
        super("Chat UDP"); // Define o título da janela
        
        // Configurações da janela
        setSize(500, 400); // Define o tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Define a ação ao fechar a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setLayout(new BorderLayout(10, 10)); // Define o layout principal com espaçamento

        // Painel para as configurações de porta (local e remota)
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        portPanel.setBorder(BorderFactory.createTitledBorder("Configurações de Porta"));

        portPanel.add(new JLabel("Porta Local:"));
        localPortField = new JTextField("5000", 6); // Campo para a porta local com valor padrão
        portPanel.add(localPortField);

        portPanel.add(new JLabel("Porta Remota:"));
        remotePortField = new JTextField("5001", 6); // Campo para a porta remota com valor padrão
        portPanel.add(remotePortField);

        connectButton = new JButton("Conectar");
        portPanel.add(connectButton);
        add(portPanel, BorderLayout.NORTH); // Adiciona o painel de portas na parte superior da janela

        // Área de texto para exibir todas as mensagens (enviadas e recebidas)
        messageDisplayArea = new JTextArea();
        messageDisplayArea.setEditable(false); // Impede que o usuário edite a área de exibição
        messageDisplayArea.setLineWrap(true); // Ativa a quebra automática de linha
        messageDisplayArea.setWrapStyleWord(true); // Quebra a linha em palavras completas
        JScrollPane scrollPane = new JScrollPane(messageDisplayArea); // Adiciona barra de rolagem à área de mensagens
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mensagens"));
        add(scrollPane, BorderLayout.CENTER); // Adiciona a área de mensagens ao centro da janela

        // Painel para o campo de escrita de mensagens e o botão de envio
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Escreva sua mensagem"));

        messageInputField = new JTextField();
        messageInputField.setEnabled(false); // Desabilita o campo de escrita até a conexão ser estabelecida
        inputPanel.add(messageInputField, BorderLayout.CENTER);

        sendButton = new JButton("Enviar");
        sendButton.setEnabled(false); // Desabilita o botão de envio até a conexão ser estabelecida
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH); // Adiciona o painel de entrada na parte inferior da janela

        // Configura os ouvintes de eventos para os componentes interativos
        addListeners();
    }

    /**
     * Configura os ActionListeners para os botões e campo de texto de mensagem.
     */
    private void addListeners() {
        // Listener para o botão 'Conectar'
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    localPort = Integer.parseInt(localPortField.getText());
                    remotePort = Integer.parseInt(remotePortField.getText());

                    // Constrói o objeto Sender (e implicitamente o Receiver) utilizando a ChatFactory.
                    // O parâmetro 'false' indica que será utilizada a comunicação UDP.
                    // 'ChatGUI.this' é passado como o MessageContainer,
                    // permitindo que esta janela receba e exiba as mensagens.
                    sender = ChatFactory.build(false, "localhost", remotePort, localPort, ChatGUI.this);
                    
                    // Desabilita os campos de porta e o botão 'Conectar' após a conexão bem-sucedida
                    localPortField.setEditable(false);
                    remotePortField.setEditable(false);
                    connectButton.setEnabled(false);

                    // Habilita o campo de entrada de mensagem e o botão 'Enviar'
                    messageInputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    messageInputField.requestFocusInWindow(); // Move o foco para o campo de mensagem
                    
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

        // Listener para o botão 'Enviar' e para o evento de 'Enter' no campo de mensagem
        ActionListener sendActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageInputField.getText();
                if (!message.trim().isEmpty()) { // Verifica se a mensagem não está vazia
                    try {
                        sender.send(message); // Envia a mensagem através do objeto Sender
                        messageDisplayArea.append("Você: " + message + "\n"); // Exibe a mensagem enviada na área de display
                        messageInputField.setText(""); // Limpa o campo de entrada após o envio
                    } catch (ChatException ex) {
                        JOptionPane.showMessageDialog(ChatGUI.this,
                                "Erro ao enviar mensagem: " + ex.getMessage(),
                                "Erro de Envio", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        sendButton.addActionListener(sendActionListener);
        messageInputField.addActionListener(sendActionListener); // Permite o envio da mensagem ao pressionar Enter
    }

    /**
     * Implementação do método newMessage da interface MessageContainer.
     * Este método é invocado pelo Receiver quando uma nova mensagem é recebida,
     * garantindo que a mensagem seja exibida na interface gráfica.
     *
     * @param message A mensagem recebida.
     */
    @Override
    public void newMessage(String message) {
        // Assegura que a atualização da interface gráfica ocorra na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            messageDisplayArea.append(":> " + message + "\n");
            // Rola automaticamente a área de texto para mostrar a mensagem mais recente
            messageDisplayArea.setCaretPosition(messageDisplayArea.getDocument().getLength());
        });
    }

    /**
     * Método principal para iniciar a aplicação ChatGUI.
     * Garante que a interface seja criada e executada na Event Dispatch Thread (EDT).
     * @param args Argumentos de linha de comando (não utilizados neste caso).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatGUI().setVisible(true);
        });
    }
}
