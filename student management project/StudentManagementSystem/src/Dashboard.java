import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class Dashboard extends JFrame {

    // ---------------- Manage Students tab fields ----------------
    JTextField idField;
    JTextField nameField;
    JTextField courseField;
    JTextField emailField;
    JTextField phoneField;
    JTextField searchField;

    JLabel statusLabel;

    JTable table;
    DefaultTableModel model;

    StudentDAO dao = new StudentDAO();

    // ---------------- Dashboard tab fields ----------------
    JLabel totalStudentsValue;
    JLabel totalCoursesValue;
    JPanel chartContainer;
    JTabbedPane tabbedPane;

    // ---- Colors used throughout (change these to re-theme the whole app) ----
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_COLOR = new Color(41, 98, 255);
    private final Color ADD_COLOR = new Color(46, 160, 67);
    private final Color UPDATE_COLOR = new Color(255, 165, 0);
    private final Color DELETE_COLOR = new Color(220, 53, 69);
    private final Color SEARCH_COLOR = new Color(41, 98, 255);
    private final Color CARD_STUDENTS_COLOR = new Color(41, 98, 255);
    private final Color CARD_COURSES_COLOR = new Color(46, 160, 67);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    public Dashboard() {

        setTitle("Student Management System");
        setSize(1000, 700);
        setLocationRelativeTo(null); // window screen ke center mein khule
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // ---------------- HEADER ----------------
        JLabel headerLabel = new JLabel("Student Management System", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(HEADER_COLOR);
        headerLabel.setBorder(new EmptyBorder(15, 10, 15, 10));
        add(headerLabel, BorderLayout.PAGE_START);

        // ---------------- TABS ----------------
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("Dashboard", buildDashboardTab());
        tabbedPane.addTab("Manage Students", buildManageStudentsTab());

        // refresh the dashboard stats/chart every time the user switches to that tab
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                refreshDashboardStats();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        loadStudents();
        refreshDashboardStats();
        setVisible(true);
    }

    // =========================================================
    //  DASHBOARD TAB (stat cards + chart)
    // =========================================================

    private JPanel buildDashboardTab() {

        JPanel dashboardPanel = new JPanel(new BorderLayout(15, 15));
        dashboardPanel.setBackground(BG_COLOR);
        dashboardPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // ---- stat cards row ----
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsPanel.setBackground(BG_COLOR);
        cardsPanel.setPreferredSize(new Dimension(0, 110));

        totalStudentsValue = new JLabel("0");
        totalCoursesValue = new JLabel("0");

        cardsPanel.add(createStatCard("Total Students", totalStudentsValue, CARD_STUDENTS_COLOR));
        cardsPanel.add(createStatCard("Total Courses", totalCoursesValue, CARD_COURSES_COLOR));

        dashboardPanel.add(cardsPanel, BorderLayout.NORTH);

        // ---- chart area (populated/refreshed by updateChart()) ----
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        dashboardPanel.add(chartContainer, BorderLayout.CENTER);

        return dashboardPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(valueLabel);

        return card;
    }

    // Reloads the stat cards and rebuilds the pie chart from the database.
    void refreshDashboardStats() {
        try {
            int totalStudents = dao.getStudentCount();
            int totalCourses = dao.getDistinctCourseCount();

            totalStudentsValue.setText(String.valueOf(totalStudents));
            totalCoursesValue.setText(String.valueOf(totalCourses));

            updateChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateChart() throws Exception {

        Map<String, Integer> courseCounts = dao.getCourseWiseCount();

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> entry : courseCounts.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Students by Course",
                dataset,
                true,   // legend
                true,   // tooltips
                false   // urls
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 350));

        chartContainer.removeAll();
        if (courseCounts.isEmpty()) {
            JLabel emptyLabel = new JLabel("No data yet -- add some students to see the chart.", SwingConstants.CENTER);
            emptyLabel.setFont(LABEL_FONT);
            chartContainer.add(emptyLabel, BorderLayout.CENTER);
        } else {
            chartContainer.add(chartPanel, BorderLayout.CENTER);
        }
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    // =========================================================
    //  MANAGE STUDENTS TAB (your original form + table screen)
    // =========================================================

    private JPanel buildManageStudentsTab() {

        JPanel manageTab = new JPanel(new BorderLayout(10, 10));
        manageTab.setBackground(BG_COLOR);

        // ---------------- FORM PANEL (labels + textfields) ----------------
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 30, 10, 30));
        formPanel.setBackground(BG_COLOR);

        idField = new JTextField();
        nameField = new JTextField();
        courseField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();

        idField.setEditable(false); // ID auto-generated, so user should not type it

        styleField(idField);
        styleField(nameField);
        styleField(courseField);
        styleField(emailField);
        styleField(phoneField);

        formPanel.add(styledLabel("ID"));
        formPanel.add(idField);

        formPanel.add(styledLabel("Name"));
        formPanel.add(nameField);

        formPanel.add(styledLabel("Course"));
        formPanel.add(courseField);

        formPanel.add(styledLabel("Email"));
        formPanel.add(emailField);

        formPanel.add(styledLabel("Phone"));
        formPanel.add(phoneField);

        // ---------------- SEARCH BAR ----------------
        searchField = new JTextField();
        styleField(searchField);
        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn, SEARCH_COLOR);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setBorder(new EmptyBorder(0, 30, 10, 30));
        searchPanel.add(styledLabel("Search Name:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        // ---------------- BUTTON PANEL (Add + Update + Delete) ----------------
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        styleButton(addBtn, ADD_COLOR);
        styleButton(updateBtn, UPDATE_COLOR);
        styleButton(deleteBtn, DELETE_COLOR);
        styleButton(clearBtn, Color.GRAY);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        // ---------------- TOP WRAPPER (stack everything above the table) ----------------
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BG_COLOR);
        topPanel.add(formPanel);
        topPanel.add(buttonPanel);
        topPanel.add(searchPanel);

        manageTab.add(topPanel, BorderLayout.NORTH);

        // ---------------- TABLE ----------------
        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Course", "Email", "Phone"}, 0);

        table = new JTable(model);
        table.setFont(FIELD_FONT);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(HEADER_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(204, 228, 255));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 30, 0, 30));
        manageTab.add(scrollPane, BorderLayout.CENTER);

        // ---------------- STATUS LABEL (bottom) ----------------
        statusLabel = new JLabel(" ");
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setBorder(new EmptyBorder(8, 30, 8, 30));
        manageTab.add(statusLabel, BorderLayout.SOUTH);

        // ---------------- ACTIONS ----------------
        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        clearBtn.addActionListener(e -> clearForm());
        searchBtn.addActionListener(e -> searchStudent());

        // click on a table row -> auto-fill the form fields (makes update/delete easier)
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                idField.setText(model.getValueAt(row, 0).toString());
                nameField.setText(model.getValueAt(row, 1).toString());
                courseField.setText(model.getValueAt(row, 2).toString());
                emailField.setText(model.getValueAt(row, 3).toString());
                phoneField.setText(model.getValueAt(row, 4).toString());
            }
        });

        return manageTab;
    }

    // ---------- Helper methods just for styling (no business logic here) ----------

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(BUTTON_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        courseField.setText("");
        emailField.setText("");
        phoneField.setText("");
        table.clearSelection();
        statusLabel.setText(" ");
    }

    // ---------- CRUD actions (same idea as your original, just with clearer messages) ----------

    void addStudent() {
        if (nameField.getText().trim().isEmpty()) {
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Name is required.");
            return;
        }
        try {
            Student s = new Student();
            s.setName(nameField.getText());
            s.setCourse(courseField.getText());
            s.setEmail(emailField.getText());
            s.setPhone(phoneField.getText());

            dao.addStudent(s);
            loadStudents();
            refreshDashboardStats();
            clearForm();
            statusLabel.setForeground(new Color(46, 125, 50));
            statusLabel.setText("Student added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    void updateStudent() {
        if (idField.getText().trim().isEmpty()) {
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Select a student from the table first.");
            return;
        }
        try {
            Student s = new Student();
            s.setId(Integer.parseInt(idField.getText()));
            s.setName(nameField.getText());
            s.setCourse(courseField.getText());
            s.setEmail(emailField.getText());
            s.setPhone(phoneField.getText());

            dao.updateStudent(s);
            loadStudents();
            refreshDashboardStats();
            statusLabel.setForeground(new Color(46, 125, 50));
            statusLabel.setText("Student updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    void deleteStudent() {
        if (idField.getText().trim().isEmpty()) {
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Select a student from the table first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this student?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            dao.deleteStudent(Integer.parseInt(idField.getText()));
            loadStudents();
            refreshDashboardStats();
            clearForm();
            statusLabel.setForeground(new Color(46, 125, 50));
            statusLabel.setText("Student deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    // simple search: reloads all students, then keeps only matching rows
    void searchStudent() {
        String keyword = searchField.getText().trim().toLowerCase();
        try {
            model.setRowCount(0);
            List<Student> students = dao.getAllStudents();
            for (Student s : students) {
                if (keyword.isEmpty()
                        || s.getName().toLowerCase().contains(keyword)
                        || s.getCourse().toLowerCase().contains(keyword)) {
                    model.addRow(new Object[]{
                            s.getId(), s.getName(), s.getCourse(), s.getEmail(), s.getPhone()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    void loadStudents() {
        try {
            model.setRowCount(0);
            List<Student> students = dao.getAllStudents();
            for (Student s : students) {
                model.addRow(new Object[]{
                        s.getId(), s.getName(), s.getCourse(), s.getEmail(), s.getPhone()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setForeground(DELETE_COLOR);
            statusLabel.setText("Error loading students: " + e.getMessage());
        }
    }
}
