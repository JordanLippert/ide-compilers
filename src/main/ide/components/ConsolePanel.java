package ide.components;

import compiler.models.CompilationResult;
import ide.factories.ComponentFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Color COLOR_TIMESTAMP = new Color(92, 99, 112);
    private static final Color COLOR_INFO      = new Color(171, 178, 191);
    private static final Color COLOR_SUCCESS   = new Color(152, 195, 121);
    private static final Color COLOR_WARNING   = new Color(229, 192, 123);
    private static final Color COLOR_ERROR     = new Color(224, 108, 117);
    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    
    public ConsolePanel() {
        setLayout(new BorderLayout());

        textPane = ComponentFactory.createConsole();
        scrollPane = new JScrollPane(textPane);
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
        textPane.setText("");
        displayWelcome();
    }

    public void appendInfo(String message) {
        append("[INFO]", message, COLOR_INFO);
    }

    public void appendSuccess(String message) {
        append("[SUCESSO]", message, COLOR_SUCCESS);
    }

    public void appendError(String message) {
        append("[ERRO]", message, COLOR_ERROR);
    }

    public void appendWarning(String message) {
        append("[AVISO]", message, COLOR_WARNING);
    }

    private void append(String prefix, String message, Color messageColor) {
        StyledDocument doc = textPane.getStyledDocument();
        String timestamp = LocalDateTime.now().format(timeFormatter);

        SimpleAttributeSet tsStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(tsStyle, COLOR_TIMESTAMP);

        SimpleAttributeSet msgStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(msgStyle, messageColor);

        try {
            doc.insertString(doc.getLength(), "[" + timestamp + "] ", tsStyle);
            doc.insertString(doc.getLength(), prefix + " " + message + "\n", msgStyle);
        } catch (BadLocationException e) {
            // unreachable: appending at valid end position
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
