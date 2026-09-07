package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import model.Airport;

public class AirportListSidePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int CODE_COLUMN = 1;
    private static final int NAME_COLUMN = 2;

    private final AirportVisibilityTableModel tableModel;
    private final TableRowSorter<AirportVisibilityTableModel> sorter;

    public AirportListSidePanel(List<Airport> airports) {
        super(new BorderLayout());
        setBorder(new TitledBorder("Aerodromi (prikaz na mapi)"));

        tableModel = new AirportVisibilityTableModel(airports);
        sorter = new TableRowSorter<>(tableModel);

        JTable table = new JTable(tableModel);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        add(buildSearchPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setPreferredSize(new Dimension(260, 0));
    }

    private JPanel buildSearchPanel() {
        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter(searchField.getText());
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(new JLabel("Pretraga:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        return searchPanel;
    }

    private void applySearchFilter(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        // pretraga po kodu ili nazivu, bez obzira na velika/mala slova
        String pattern = "(?i)" + Pattern.quote(trimmed);
        sorter.setRowFilter(RowFilter.regexFilter(pattern, CODE_COLUMN, NAME_COLUMN));
    }

    public void setOnVisibilityChanged(Runnable callback) {
        tableModel.setOnVisibilityChanged(callback);
    }

    public Predicate<Airport> getVisibilityFilter() {
        return tableModel.getVisibilityFilter();
    }

    public void refresh() {
        tableModel.refresh();
    }
}
