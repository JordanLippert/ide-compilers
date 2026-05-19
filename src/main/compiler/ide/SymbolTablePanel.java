package compiler.ide;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel de visualização da tabela de símbolos
 *
 * @author Jordan Lippert
 * @author André Melo
 */
public class SymbolTablePanel extends JPanel {

    private static final String[] COLUMNS = {"Nome", "Tipo", "Escopo", "Inicializado", "Usado"};

    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final Color BG_DARK    = new Color(33, 37, 43);
    private static final Color BG_HEADER  = new Color(40, 44, 52);
    private static final Color FG_TEXT    = new Color(171, 178, 191);
    private static final Color BG_ROW_ALT = new Color(38, 42, 50);

    public SymbolTablePanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setBackground(BG_DARK);
        table.setForeground(FG_TEXT);
        table.setGridColor(new Color(60, 65, 75));
        table.setRowHeight(22);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(60, 65, 80));
        table.setSelectionForeground(FG_TEXT);

        // Header styling
        table.getTableHeader().setBackground(BG_HEADER);
        table.getTableHeader().setForeground(FG_TEXT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Nome
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Tipo
        table.getColumnModel().getColumn(2).setPreferredWidth(90);  // Escopo
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Inicializado
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Usado

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Title label
        JLabel title = new JLabel("  Tabela de Símbolos");
        title.setForeground(FG_TEXT);
        title.setBackground(BG_HEADER);
        title.setOpaque(true);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Updates the table with fresh symbol data.
     * Rows: [name, type, scope, initialized, used]
     */
    public void update(List<Object[]> rows) {
        tableModel.setRowCount(0);
        if (rows != null) {
            for (Object[] row : rows) {
                tableModel.addRow(row);
            }
        }
    }

    public void clear() {
        tableModel.setRowCount(0);
    }
}
