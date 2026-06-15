package compiler.ide;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AsmPanel extends JPanel {

    private final JTextArea textArea;

    public AsmPanel() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Código Assembly (BIP)"));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setBackground(new Color(40, 44, 52));
        textArea.setForeground(new Color(171, 178, 191));
        textArea.setCaretColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void update(String asmCode) {
        textArea.setText(asmCode != null ? asmCode : "");
        textArea.setCaretPosition(0);
    }

    public void clear() {
        textArea.setText("");
    }
}
