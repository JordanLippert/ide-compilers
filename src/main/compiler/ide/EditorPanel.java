package compiler.ide;

import compiler.factory.ComponentFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Painel do editor de código
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class EditorPanel extends JPanel {
    
    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    
    public EditorPanel() {
        setLayout(new BorderLayout());
        
        textPane = ComponentFactory.createEditor();
        scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public String getText() {
        return textPane.getText();
    }
    
    public void setText(String text) {
        textPane.setText(text);
        textPane.setCaretPosition(0);
    }
    
    public void clear() {
        textPane.setText("");
    }
    
    public JTextPane getTextPane() {
        return textPane;
    }
}
