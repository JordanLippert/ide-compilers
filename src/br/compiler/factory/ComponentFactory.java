package br.compiler.factory;

import javax.swing.*;
import java.awt.*;

/**
 * Factory para criação de componentes da interface gráfica
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class ComponentFactory {
    
    private static final int FONT_SIZE = 14;
    private static final String MONOSPACED_FONT = "Monospaced";
    private static final String SANS_SERIF_FONT = "SansSerif";
    
    public static JTextPane createEditor() {
        JTextPane editor = new JTextPane();
        editor.setFont(new Font(MONOSPACED_FONT, Font.PLAIN, FONT_SIZE));
        editor.setBackground(Color.WHITE);
        editor.setForeground(Color.BLACK);
        editor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        editor.setCaretColor(Color.BLACK);
        return editor;
    }
    
    public static JTextArea createConsole() {
        JTextArea console = new JTextArea();
        console.setFont(new Font(MONOSPACED_FONT, Font.PLAIN, FONT_SIZE));
        console.setEditable(false);
        console.setBackground(new Color(40, 44, 52));
        console.setForeground(new Color(171, 178, 191));
        console.setCaretColor(Color.WHITE);
        console.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return console;
    }
    
    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, FONT_SIZE));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    public static JButton createCompileButton() {
        JButton button = createButton("Compilar [F9]");
        button.setBackground(new Color(76, 175, 80));
        button.setForeground(Color.WHITE);
        button.setToolTipText("Compilar código fonte (F9)");
        return button;
    }
    
    public static JButton createNewButton() {
        JButton button = createButton("Novo");
        button.setToolTipText("Novo arquivo");
        return button;
    }
    
    public static JButton createOpenButton() {
        JButton button = createButton("Abrir");
        button.setToolTipText("Abrir arquivo");
        return button;
    }
    
    public static JButton createSaveButton() {
        JButton button = createButton("Salvar");
        button.setToolTipText("Salvar arquivo");
        return button;
    }
}
