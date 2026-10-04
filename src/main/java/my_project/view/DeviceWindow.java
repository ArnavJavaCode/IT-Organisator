package my_project.view;

import KAGO_framework.control.DatabaseController;
import my_project.model.EmpolyeeItem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Fenster zur Geräteverwaltung. Das Layout stammt aus DeviceWindow.form (Swing UI Designer).
 * Die Felder unten müssen im UI Designer exakt so als "field name" gesetzt sein.
 */
public class DeviceWindow {

    // ---- vom UI Designer gefüllte Komponenten ----
    private JPanel mainPanel;
    private JTextField searchField;
    private JComboBox<String> typeFilterBox;
    private JCheckBox availableOnlyCheck;
    private JButton resetButton;
    private JTabbedPane tabbedPane;
    private JTable deviceTable;
    private JTable loanTable;
    private JComboBox<EmpolyeeItem> employeeBox;
    private JButton lendButton;
    private JButton returnButton;
    private JLabel statusLabel;

    // ---- eigene Attribute ----
    private final DatabaseController db;
    private DefaultTableModel deviceModel;
    private DefaultTableModel loanModel;
    private TableRowSorter<DefaultTableModel> deviceSorter;

    public DeviceWindow(DatabaseController db) {
        this.db = db;

        // Geräte-Tabelle (nicht editierbar, ID und Verfügbar als Zahlen sortierbar)
        deviceModel = new DefaultTableModel(new Object[]{"ID", "Typ", "Bezeichnung", "Verfügbar"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return (col == 0 || col == 3) ? Integer.class : String.class;
            }
        };
        deviceTable.setModel(deviceModel);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceSorter = new TableRowSorter<>(deviceModel);
        deviceTable.setRowSorter(deviceSorter);

        // Verleih-Tabelle: Spalte 0 (Verleih-ID) steckt im Modell, wird aber ausgeblendet
        loanModel = new DefaultTableModel(new Object[]{"ID", "Gerät", "Typ", "Ausgeliehen von", "Seit"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        loanTable.setModel(loanModel);
        loanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loanTable.removeColumn(loanTable.getColumnModel().getColumn(0));

        // Suche und Filter: bei jeder Änderung neu filtern
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        typeFilterBox.addActionListener(e -> applyFilter());
        availableOnlyCheck.addActionListener(e -> applyFilter());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            typeFilterBox.setSelectedIndex(0);
            availableOnlyCheck.setSelected(false);
        });

        lendButton.addActionListener(e -> lendSelectedDevice());
        returnButton.addActionListener(e -> returnSelectedLoan());

        loadEmployees();
        refreshData();
    }

    /**
     * Füllt die Mitarbeiter-Auswahl aus der Datenbank.
     */
    private void loadEmployees() {
        employeeBox.removeAllItems();
        String[][] employees = db.loadEmployees();
        for (String[] row : employees) {
            employeeBox.addItem(new EmpolyeeItem(Integer.parseInt(row[0]), row[1]));
        }
    }

    /**
     * Lädt Geräte und Verleihe neu aus der Datenbank und zeigt sie in den Tabellen.
     */
    private void refreshData() {
        deviceModel.setRowCount(0);
        for (String[] row : db.loadDevices()) {
            deviceModel.addRow(new Object[]{
                    Integer.parseInt(row[0]), row[1], row[2], Integer.parseInt(row[3])});
        }

        loanModel.setRowCount(0);
        for (String[] row : db.loadLoans()) {
            loanModel.addRow(new Object[]{Integer.parseInt(row[0]), row[1], row[2], row[3], row[4]});
        }
    }

    private void lendSelectedDevice() {
        int row = deviceTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Bitte zuerst ein Gerät auswählen.");
            return;
        }
        EmpolyeeItem employee = (EmpolyeeItem) employeeBox.getSelectedItem();
        if (employee == null) {
            statusLabel.setText("Bitte einen Mitarbeiter auswählen.");
            return;
        }

        // Wegen Sortierung/Filter muss die Zeile ins Modell umgerechnet werden
        int modelRow = deviceTable.convertRowIndexToModel(row);
        int deviceId = (Integer) deviceModel.getValueAt(modelRow, 0);
        String deviceName = (String) deviceModel.getValueAt(modelRow, 2);

        String error = db.lendDevice(deviceId, employee.getId());
        if (error != null) {
            statusLabel.setText(error);
            return;
        }
        statusLabel.setText(deviceName + " wurde an " + employee + " verliehen.");
        refreshData();
    }

    private void returnSelectedLoan() {
        int row = loanTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Bitte zuerst einen Verleih auswählen.");
            return;
        }
        int loanId = (Integer) loanModel.getValueAt(loanTable.convertRowIndexToModel(row), 0);
        String deviceName = (String) loanModel.getValueAt(loanTable.convertRowIndexToModel(row), 1);

        String error = db.returnDevice(loanId);
        if (error != null) {
            statusLabel.setText(error);
            return;
        }
        statusLabel.setText(deviceName + " wurde zurückgegeben.");
        refreshData();
    }

    /**
     * Filtert die Geräteliste nach Suchtext, Typ und Verfügbarkeit.
     */
    private void applyFilter() {
        final String text = searchField.getText().trim().toLowerCase();
        final String type = (String) typeFilterBox.getSelectedItem();
        final boolean onlyAvailable = availableOnlyCheck.isSelected();

        deviceSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String deviceType = entry.getStringValue(1);
                String deviceName = entry.getStringValue(2);
                int available = (Integer) entry.getValue(3);

                boolean matchesText = text.isEmpty()
                        || deviceName.toLowerCase().contains(text)
                        || deviceType.toLowerCase().contains(text);
                boolean matchesType = type == null || type.equals("Alle") || type.equals(deviceType);
                boolean matchesAvailable = !onlyAvailable || available > 0;

                return matchesText && matchesType && matchesAvailable;
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Öffnet das Fenster, z. B. aus dem ProgramController.
     */
    public static void open(DatabaseController db) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Geräteverleih");
            frame.setContentPane(new DeviceWindow(db).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        mainPanel.add(panel1, BorderLayout.NORTH);
        final JLabel label1 = new JLabel();
        label1.setText("Suche:");
        panel1.add(label1);
        searchField = new JTextField();
        searchField.setColumns(20);
        panel1.add(searchField);
        final JLabel label2 = new JLabel();
        label2.setText("Typ:");
        panel1.add(label2);
        typeFilterBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Alle");
        defaultComboBoxModel1.addElement("Laptop");
        defaultComboBoxModel1.addElement("Monitor");
        defaultComboBoxModel1.addElement("Headset");
        defaultComboBoxModel1.addElement("Sonstiges");
        typeFilterBox.setModel(defaultComboBoxModel1);
        panel1.add(typeFilterBox);
        availableOnlyCheck = new JCheckBox();
        availableOnlyCheck.setText("Nur verfügbare");
        panel1.add(availableOnlyCheck);
        resetButton = new JButton();
        resetButton.setText("Zurücksetzen");
        panel1.add(resetButton);
        tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Geräte", panel2);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, BorderLayout.CENTER);
        deviceTable = new JTable();
        scrollPane1.setViewportView(deviceTable);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Verliehen", panel3);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel3.add(scrollPane2, BorderLayout.CENTER);
        loanTable = new JTable();
        scrollPane2.setViewportView(loanTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        mainPanel.add(panel4, BorderLayout.SOUTH);
        final JLabel label3 = new JLabel();
        label3.setText("Mitarbeiter:");
        panel4.add(label3);
        employeeBox = new JComboBox();
        panel4.add(employeeBox);
        lendButton = new JButton();
        lendButton.setText("Ausleihen");
        panel4.add(lendButton);
        returnButton = new JButton();
        returnButton.setText("Zurückgeben");
        panel4.add(returnButton);
        statusLabel = new JLabel();
        statusLabel.setText(" ");
        panel4.add(statusLabel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}