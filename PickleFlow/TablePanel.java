import java.awt.BorderLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class TablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;

    public TablePanel(String... columns) {
        super(new BorderLayout());
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] r : rows) model.addRow(r);
    }

 
    public String selectedId() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.", "PickleFlow", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return model.getValueAt(table.convertRowIndexToModel(r), 0).toString();
    }
}
