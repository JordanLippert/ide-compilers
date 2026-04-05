
import br.compiler.ide.CompilerIDE;

import javax.swing.*;

/**
 * Ponto de entrada da aplicação IDE Compilador
 * Trabalho T2 - Compiladores
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Define o Look and Feel do sistema operacional
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Não foi possível configurar Look and Feel: " + e.getMessage());
            }
            
            // Inicia a IDE
            CompilerIDE ide = new CompilerIDE();
            ide.setVisible(true);
        });
    }
}