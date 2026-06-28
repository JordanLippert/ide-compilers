package ide.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel de visualização da tabela de símbolos
 *
 * @author Jordan Lippert
 * @author André Melo
 */
public class SymbolTablePanel extends JPanel {

    private static final String[] COLUMNS = {
            "Nome",
            "Tipo",
            "Escopo",
            "Inicializado",
            "Usado",
            "Parâmetro",
            "Posição Parâmetro",
            "Array",
            "Matriz",
            "Por Referência",
            "Função",
            "Valor"
    };

    private static final Color BG_DARK    = new Color(33, 37, 43);
    private static final Color BG_HEADER  = new Color(40, 44, 52);
    private static final Color FG_TEXT    = new Color(171, 178, 191);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private boolean showValues = false;
    private List<Object[]> lastRows = new ArrayList<>();

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

        table.getTableHeader().setBackground(BG_HEADER);
        table.getTableHeader().setForeground(FG_TEXT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);

        applyColumnWidths();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JLabel title = new JLabel("  Tabela de Símbolos");
        title.setForeground(FG_TEXT);
        title.setBackground(BG_HEADER);
        title.setOpaque(true);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void update(List<Object[]> rows) {
        lastRows = rows != null ? new ArrayList<>(rows) : new ArrayList<>();
        repopulate();
    }

    public void clear() {
        lastRows.clear();
        tableModel.setRowCount(0);
    }

    public void setShowValues(boolean show) {
        this.showValues = show;
        tableModel.setColumnIdentifiers(COLUMNS);
        repopulate();
        applyColumnWidths();
    }

    private void repopulate() {
        tableModel.setRowCount(0);
        for (Object[] row : lastRows) {
            tableModel.addRow(row);
        }
    }

    private void applyColumnWidths() {
        int cols = tableModel.getColumnCount();
        for (int i = 0; i < cols; i++) {
            int width = (i == cols - 1 && showValues) ? 80 : 30;
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
    }
}
