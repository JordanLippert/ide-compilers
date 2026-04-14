package compiler.ide;

import compiler.factory.ComponentFactory;
import compiler.model.CompilationResult;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Painel do console de mensagens
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class ConsolePanel extends JPanel {
    
    private final JTextArea textArea;
    private final JScrollPane scrollPane;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public ConsolePanel() {
        setLayout(new BorderLayout());
        
        textArea = ComponentFactory.createConsole();
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        add(scrollPane, BorderLayout.CENTER);
        
        displayWelcome();
    }
    
    private void displayWelcome() {
        appendInfo("=== IDE Compilador - Console de Mensagens ===");
        appendInfo("Pronto para compilar.");
    }
    
    public void display(CompilationResult result) {
        if (result.isSuccess()) {
            appendSuccess("✓ Compilação concluída com sucesso!");
        } else {
            appendError(result.toString());
        }
        scrollToBottom();
    }
    
    public void clear() {
        textArea.setText("");
        displayWelcome();
    }
    
    public void appendInfo(String message) {
        append("[INFO]", message);
    }
    
    public void appendSuccess(String message) {
        append("[SUCESSO]", message);
    }
    
    public void appendError(String message) {
        append("[ERRO]", message);
    }
    
    public void appendWarning(String message) {
        append("[AVISO]", message);
    }
    
    private void append(String prefix, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        textArea.append(String.format("[%s] %s %s\n", timestamp, prefix, message));
    }
    
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    public JTextArea getTextArea() {
        return textArea;
    }
}
