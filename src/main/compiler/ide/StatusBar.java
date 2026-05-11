package compiler.ide;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel fileLabel;
    private final JLabel caretLabel;

    private static final Color BG = new Color(0, 122, 204);
    private static final Color FG = Color.WHITE;
    private static final Font STATUS_FONT = new Font("SansSerif", Font.PLAIN, 12);

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(2, 8, 2, 8));

        fileLabel = new JLabel("Sem título");
        fileLabel.setForeground(FG);
        fileLabel.setFont(STATUS_FONT);

        caretLabel = new JLabel("Ln 1, Col 1");
        caretLabel.setForeground(FG);
        caretLabel.setFont(STATUS_FONT);

        add(fileLabel, BorderLayout.WEST);
        add(caretLabel, BorderLayout.EAST);
    }

    public void updateFile(String fileName) {
        fileLabel.setText(fileName);
    }

    public void updateCaret(int line, int col) {
        caretLabel.setText("Ln " + line + ", Col " + col);
    }
}
