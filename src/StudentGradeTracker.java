// ============================================================
//  FILE STRUCTURE (single file for simplicity, can be split)
//
//  1. Student.java         — Model class
//  2. GradeTracker.java    — Service / Logic class
//  3. ConsoleApp.java      — Console-based interface
//  4. GradeTrackerGUI.java — Swing GUI interface
//  5. Main.java            — Entry point (choose mode)
// ============================================================

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


// ============================================================
//  1. STUDENT MODEL CLASS
// ============================================================
class Student {
    private static int counter = 1;
    private int id;
    private String name;
    private ArrayList<Double> grades;

    public Student(String name, ArrayList<Double> grades) {
        this.id     = counter++;
        this.name   = name;
        this.grades = grades;
    }

    // Getters & Setters
    public int               getId()     { return id; }
    public String            getName()   { return name; }
    public ArrayList<Double> getGrades() { return grades; }
    public void setName(String n)              { this.name   = n; }
    public void setGrades(ArrayList<Double> g) { this.grades = g; }

    // Statistics
    public double getAverage() {
        double sum = 0;
        for (double g : grades) sum += g;
        return Math.round((sum / grades.size()) * 100.0) / 100.0;
    }
    public double getHighest() { return Collections.max(grades); }
    public double getLowest()  { return Collections.min(grades); }

    public String getLetterGrade() {
        double a = getAverage();
        if (a >= 97) return "A+"; if (a >= 93) return "A";  if (a >= 90) return "A-";
        if (a >= 87) return "B+"; if (a >= 83) return "B";  if (a >= 80) return "B-";
        if (a >= 77) return "C+"; if (a >= 73) return "C";  if (a >= 70) return "C-";
        if (a >= 67) return "D+"; if (a >= 60) return "D";
        return "F";
    }
    public String getStatus() { return getAverage() >= 40 ? "PASS" : "FAIL"; }

    @Override
    public String toString() {
        return String.format("%-4d %-20s %6.2f%%  %6.1f  %6.1f   %-4s  %s",
                id, name, getAverage(), getHighest(), getLowest(),
                getLetterGrade(), getStatus());
    }
}


// ============================================================
//  2. GRADE TRACKER SERVICE CLASS
// ============================================================
class GradeTracker {
    private ArrayList<Student> students = new ArrayList<>();

    public void addStudent(Student s)    { students.add(s); }
    public ArrayList<Student> getAll()   { return students; }
    public int size()                    { return students.size(); }

    public boolean deleteById(int id) {
        return students.removeIf(s -> s.getId() == id);
    }

    public Student findById(int id) {
        for (Student s : students)
            if (s.getId() == id) return s;
        return null;
    }

    public ArrayList<Student> searchByName(String kw) {
        ArrayList<Student> res = new ArrayList<>();
        for (Student s : students)
            if (s.getName().toLowerCase().contains(kw.toLowerCase())) res.add(s);
        return res;
    }

    public double getClassAverage() {
        if (students.isEmpty()) return 0;
        double sum = 0;
        for (Student s : students) sum += s.getAverage();
        return Math.round((sum / students.size()) * 100.0) / 100.0;
    }

    public Student getTopStudent() {
        return students.isEmpty() ? null :
                Collections.max(students, Comparator.comparingDouble(Student::getAverage));
    }

    public Student getLowestStudent() {
        return students.isEmpty() ? null :
                Collections.min(students, Comparator.comparingDouble(Student::getAverage));
    }

    public long countByGrade(String letter) {
        return students.stream().filter(s -> s.getLetterGrade().equals(letter)).count();
    }

    public int getPassCount() {
        int c = 0; for (Student s : students) if (s.getAverage() >= 40) c++; return c;
    }

    public ArrayList<Student> getSortedByAvg() {
        ArrayList<Student> copy = new ArrayList<>(students);
        copy.sort((a, b) -> Double.compare(b.getAverage(), a.getAverage()));
        return copy;
    }
}


// ============================================================
//  3. CONSOLE-BASED APP
// ============================================================
class ConsoleApp {
    private static final Scanner sc = new Scanner(System.in);
    private static final GradeTracker gt = new GradeTracker();
    private static final String LINE  = "=".repeat(65);
    private static final String DLINE = "-".repeat(65);

    public static void run() {
        preload();
        int choice;
        do {
            printMenu();
            choice = readInt();
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAll();
                case 3 -> searchStudent();
                case 4 -> editStudent();
                case 5 -> deleteStudent();
                case 6 -> summaryReport();
                case 7 -> rankingReport();
                case 0 -> System.out.println("\n Goodbye! Exiting Console Mode...\n");
                default -> System.out.println("  Invalid choice.");
            }
        } while (choice != 0);
    }

    static void printMenu() {
        System.out.println("\n" + LINE);
        System.out.println("         STUDENT GRADE TRACKER  [CONSOLE MODE]");
        System.out.println(LINE);
        System.out.println("  1. Add Student          5. Delete Student");
        System.out.println("  2. View All Students    6. Summary Report");
        System.out.println("  3. Search Student       7. Top Performers");
        System.out.println("  4. Edit Student         0. Exit");
        System.out.println(LINE);
        System.out.print("  Choice: ");
    }

    static void header(String t) {
        System.out.println("\n" + LINE);
        System.out.println("  " + t);
        System.out.println(LINE);
    }

    static void tableHeader() {
        System.out.printf("%-4s %-20s %-8s %-8s %-8s %-5s %s%n",
                "ID","Name","Avg","High","Low","Grade","Status");
        System.out.println(DLINE);
    }

    // ── Options ──────────────────────────────
    static void addStudent() {
        header("ADD STUDENT");
        System.out.print("  Name            : "); String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  Name required."); return; }
        System.out.print("  Number of grades: "); int n = readInt();
        ArrayList<Double> grades = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            double g;
            do { System.out.printf("  Grade %d (0-100) : ", i); g = readDouble(); }
            while (g < 0 || g > 100);
            grades.add(g);
        }
        gt.addStudent(new Student(name, grades));
        System.out.println("  Student added successfully!");
    }

    static void viewAll() {
        header("ALL STUDENTS");
        if (gt.size() == 0) { System.out.println("  No students."); return; }
        tableHeader();
        for (Student s : gt.getAll()) System.out.println(s);
        System.out.println(DLINE);
        System.out.printf("  Total: %d  |  Class Avg: %.2f%%%n", gt.size(), gt.getClassAverage());
    }

    static void searchStudent() {
        header("SEARCH");
        System.out.print("  Keyword: "); String kw = sc.nextLine().trim();
        ArrayList<Student> res = gt.searchByName(kw);
        if (res.isEmpty()) { System.out.println("  No matches found."); return; }
        tableHeader();
        for (Student s : res) System.out.println(s);
    }

    static void editStudent() {
        header("EDIT STUDENT");
        System.out.print("  Enter ID: "); int id = readInt();
        Student s = gt.findById(id);
        if (s == null) { System.out.println("  Not found."); return; }
        System.out.println("  Found: " + s.getName());
        System.out.print("  New name (Enter=keep): "); String nm = sc.nextLine().trim();
        if (!nm.isEmpty()) s.setName(nm);
        System.out.print("  Update grades? (y/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("  Number of grades: "); int n = readInt();
            ArrayList<Double> ng = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                double g;
                do { System.out.printf("  Grade %d: ", i); g = readDouble(); }
                while (g < 0 || g > 100);
                ng.add(g);
            }
            s.setGrades(ng);
        }
        System.out.println("  Updated successfully!");
    }

    static void deleteStudent() {
        header("DELETE STUDENT");
        System.out.print("  Enter ID: "); int id = readInt();
        Student s = gt.findById(id);
        if (s == null) { System.out.println("  Not found."); return; }
        System.out.print("  Delete \"" + s.getName() + "\"? (y/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            gt.deleteById(id);
            System.out.println("  Deleted.");
        }
    }

    static void summaryReport() {
        header("CLASS SUMMARY REPORT");
        if (gt.size() == 0) { System.out.println("  No data."); return; }
        Student top = gt.getTopStudent(), low = gt.getLowestStudent();
        System.out.printf("  Total Students   : %d%n",   gt.size());
        System.out.printf("  Class Average    : %.2f%%%n", gt.getClassAverage());
        System.out.printf("  Top Performer    : %s (%.2f%%)%n", top.getName(), top.getAverage());
        System.out.printf("  Needs Support    : %s (%.2f%%)%n", low.getName(), low.getAverage());
        System.out.printf("  Passing          : %d%n", gt.getPassCount());
        System.out.printf("  Failing          : %d%n", gt.size() - gt.getPassCount());
        System.out.println("\n  Grade Distribution:");
        for (String g : new String[]{"A+","A","A-","B+","B","B-","C+","C","C-","D+","D","F"}) {
            long cnt = gt.countByGrade(g);
            if (cnt > 0) System.out.printf("    %-3s : %d student(s)%n", g, cnt);
        }
    }

    static void rankingReport() {
        header("TOP PERFORMERS RANKING");
        if (gt.size() == 0) { System.out.println("  No data."); return; }
        tableHeader();
        int rank = 1;
        for (Student s : gt.getSortedByAvg())
            System.out.printf("#%-3d %s%n", rank++, s);
    }

    static void preload() {
        String[][] data = {
            {"Aarav Sharma","92","88","95","78","90"},
            {"Priya Patel","85","91","80","88","76"},
            {"Rohan Singh","70","65","72","68","74"},
            {"Sneha Gupta","98","95","97","99","100"},
            {"Vikram Rao","55","60","58","62","50"}
        };
        for (String[] d : data) {
            ArrayList<Double> g = new ArrayList<>();
            for (int i = 1; i < d.length; i++) g.add(Double.parseDouble(d[i]));
            gt.addStudent(new Student(d[0], g));
        }
    }

    static int readInt() {
        try { int v = Integer.parseInt(sc.nextLine().trim()); return v; }
        catch (Exception e) { return -1; }
    }
    static double readDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (Exception e) { return -1; }
    }
}


// ============================================================
//  4. SWING GUI APP
// ============================================================
class GradeTrackerGUI extends JFrame {

    // ── Colours ──────────────────────────────
    private static final Color BG       = new Color(15, 23, 42);
    private static final Color PANEL_BG = new Color(30, 41, 59);
    private static final Color ACCENT   = new Color(99, 102, 241);
    private static final Color GREEN    = new Color(34, 197, 94);
    private static final Color RED      = new Color(239, 68, 68);
    private static final Color AMBER    = new Color(245, 158, 11);
    private static final Color TEXT     = new Color(226, 232, 240);
    private static final Color MUTED    = new Color(100, 116, 139);

    private final GradeTracker gt = new GradeTracker();

    // Table model
    private DefaultTableModel tableModel;
    private JTable table;

    // Input fields
    private JTextField tfName, tfGrades, tfSearch;

    // Stat labels
    private JLabel lblTotal, lblAvg, lblTop, lblLow, lblPass, lblFail;

    public GradeTrackerGUI() {
        super("Student Grade Tracker — GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG);

        preload();
        buildUI();
        refreshTable(gt.getAll());
        updateStats();
    }

    // ─────────────────────────────────────────
    //  BUILD UI
    // ─────────────────────────────────────────
    void buildUI() {
        add(buildHeader(),   BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);
        add(buildSidebar(),  BorderLayout.EAST);
    }

    // Header bar
    JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(30, 58, 138));
        p.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("🎓  Student Grade Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Manage students, grades, and reports");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(165, 180, 252));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title); left.add(sub);
        p.add(left, BorderLayout.WEST);
        return p;
    }

    // Center: search bar + table
    JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 16, 12, 8));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);
        tfSearch = styledField("Search by name...", 200);
        JButton btnSearch = styledButton("🔍 Search", ACCENT);
        JButton btnReset  = styledButton("↺ Reset",  MUTED);
        btnSearch.addActionListener(e -> {
            String kw = tfSearch.getText().trim();
            refreshTable(kw.isEmpty() ? gt.getAll() : gt.searchByName(kw));
        });
        btnReset.addActionListener(e -> { tfSearch.setText(""); refreshTable(gt.getAll()); });
        searchPanel.add(new JLabel((Icon) colored("Search: ", TEXT)));
        searchPanel.add(tfSearch); searchPanel.add(btnSearch); searchPanel.add(btnReset);
        p.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID","Name","Avg %","Highest","Lowest","Grade","Status","Grades"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setBackground(PANEL_BG);
        table.setForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(51, 65, 85));
        table.setSelectionBackground(new Color(99, 102, 241, 100));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(30, 58, 138));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Color renderer for Grade & Status columns
        table.getColumnModel().getColumn(5).setCellRenderer(gradeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(PANEL_BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        scroll.getViewport().setBackground(PANEL_BG);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // Right sidebar: input form + stat cards
    JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(12, 10, 12, 14));
        p.setPreferredSize(new Dimension(270, 0));

        // ── Input Form ──
        p.add(sectionLabel("➕  Student Form"));
        p.add(Box.createVerticalStrut(6));

        p.add(fieldLabel("Name")); tfName = styledField("e.g. Rahul Verma", 230); p.add(tfName);
        p.add(Box.createVerticalStrut(6));
        p.add(fieldLabel("Grades (comma separated)"));
        tfGrades = styledField("e.g. 85,90,78,92,88", 230); p.add(tfGrades);
        p.add(Box.createVerticalStrut(10));

        JButton btnAdd  = styledButton("Add Student",    GREEN);
        JButton btnEdit = styledButton("Update Selected", AMBER);
        JButton btnDel  = styledButton("Delete Selected", RED);
        for (JButton b : new JButton[]{btnAdd, btnEdit, btnDel}) {
            b.setMaximumSize(new Dimension(240, 34)); p.add(b); p.add(Box.createVerticalStrut(5));
        }

        btnAdd.addActionListener(e -> addStudent());
        btnEdit.addActionListener(e -> editSelected());
        btnDel.addActionListener(e -> deleteSelected());

        p.add(Box.createVerticalStrut(16));

        // ── Stats ──
        p.add(sectionLabel("📊  Class Statistics"));
        p.add(Box.createVerticalStrut(8));

        lblTotal = statLabel("Total Students : -");
        lblAvg   = statLabel("Class Average  : -");
        lblTop   = statLabel("Top Performer  : -");
        lblLow   = statLabel("Lowest         : -");
        lblPass  = statLabel("Passing        : -");
        lblFail  = statLabel("Failing        : -");

        for (JLabel l : new JLabel[]{lblTotal,lblAvg,lblTop,lblLow,lblPass,lblFail}) {
            p.add(l); p.add(Box.createVerticalStrut(4));
        }

        p.add(Box.createVerticalStrut(16));

        JButton btnReport = styledButton("📄 Show Full Report", ACCENT);
        btnReport.setMaximumSize(new Dimension(240, 34));
        btnReport.addActionListener(e -> showReport());
        p.add(btnReport);

        return p;
    }

    // ─────────────────────────────────────────
    //  ACTIONS
    // ─────────────────────────────────────────
    void addStudent() {
        String name = tfName.getText().trim();
        String raw  = tfGrades.getText().trim();
        if (name.isEmpty() || raw.isEmpty()) {
            showMsg("Please fill Name and Grades.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            ArrayList<Double> grades = parseGrades(raw);
            gt.addStudent(new Student(name, grades));
            tfName.setText(""); tfGrades.setText("");
            refreshTable(gt.getAll()); updateStats();
            showMsg("Student \"" + name + "\" added!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showMsg("Invalid grades. Use comma-separated numbers 0-100.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showMsg("Select a student to edit.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        Student s = gt.findById(id);
        if (s == null) return;

        String name = tfName.getText().trim();
        String raw  = tfGrades.getText().trim();
        if (!name.isEmpty()) s.setName(name);
        if (!raw.isEmpty()) {
            try { s.setGrades(parseGrades(raw)); }
            catch (Exception e) { showMsg("Invalid grades.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        }
        tfName.setText(""); tfGrades.setText("");
        refreshTable(gt.getAll()); updateStats();
        showMsg("Updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showMsg("Select a student to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        Student s = gt.findById(id);
        int res = JOptionPane.showConfirmDialog(this,
                "Delete \"" + s.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            gt.deleteById(id); refreshTable(gt.getAll()); updateStats();
        }
    }

    void showReport() {
        if (gt.size() == 0) { showMsg("No students to report.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("       CLASS SUMMARY REPORT\n");
        sb.append("══════════════════════════════════════\n");
        sb.append(String.format("Total Students  : %d%n", gt.size()));
        sb.append(String.format("Class Average   : %.2f%%%n", gt.getClassAverage()));
        sb.append(String.format("Top Performer   : %s (%.2f%%)%n",
                gt.getTopStudent().getName(), gt.getTopStudent().getAverage()));
        sb.append(String.format("Needs Support   : %s (%.2f%%)%n",
                gt.getLowestStudent().getName(), gt.getLowestStudent().getAverage()));
        sb.append(String.format("Passing / Failing: %d / %d%n",
                gt.getPassCount(), gt.size() - gt.getPassCount()));
        sb.append("\nGrade Distribution:\n");
        for (String g : new String[]{"A+","A","A-","B+","B","B-","C+","C","C-","D+","D","F"}) {
            long cnt = gt.countByGrade(g);
            if (cnt > 0) sb.append(String.format("  %-3s : %d student(s)%n", g, cnt));
        }
        sb.append("\nTop Performers Ranking:\n");
        int rank = 1;
        for (Student s : gt.getSortedByAvg())
            sb.append(String.format("  #%d  %-20s %.2f%%  %s%n",
                    rank++, s.getName(), s.getAverage(), s.getLetterGrade()));

        JTextArea ta = new JTextArea(sb.toString(), 22, 44);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Full Report", JOptionPane.PLAIN_MESSAGE);
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    void refreshTable(ArrayList<Student> list) {
        tableModel.setRowCount(0);
        for (Student s : list) {
            tableModel.addRow(new Object[]{
                s.getId(), s.getName(),
                String.format("%.2f", s.getAverage()),
                s.getHighest(), s.getLowest(),
                s.getLetterGrade(), s.getStatus(),
                s.getGrades().toString()
            });
        }
    }

    void updateStats() {
        lblTotal.setText("Total Students : " + gt.size());
        lblAvg.setText(String.format("Class Average  : %.2f%%", gt.getClassAverage()));
        if (gt.size() > 0) {
            lblTop.setText("Top Performer  : " + gt.getTopStudent().getName());
            lblLow.setText("Lowest         : " + gt.getLowestStudent().getName());
        }
        lblPass.setText("Passing        : " + gt.getPassCount());
        lblFail.setText("Failing        : " + (gt.size() - gt.getPassCount()));
    }

    ArrayList<Double> parseGrades(String raw) {
        ArrayList<Double> list = new ArrayList<>();
        for (String t : raw.split(",")) {
            double v = Double.parseDouble(t.trim());
            if (v < 0 || v > 100) throw new IllegalArgumentException();
            list.add(v);
        }
        if (list.isEmpty()) throw new IllegalArgumentException();
        return list;
    }

    // ── Cell Renderers ───────────────────────
    TableCellRenderer gradeRenderer() {
        return (tbl, val, sel, foc, row, col) -> {
            JLabel l = new JLabel(val.toString(), SwingConstants.CENTER);
            l.setOpaque(true);
            String g = val.toString();
            Color c = g.startsWith("A") ? GREEN : g.startsWith("B") ? new Color(59,130,246) :
                      g.startsWith("C") ? AMBER  : g.startsWith("D") ? new Color(249,115,22) : RED;
            l.setForeground(c); l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBackground(sel ? new Color(99,102,241,80) : PANEL_BG);
            return l;
        };
    }

    TableCellRenderer statusRenderer() {
        return (tbl, val, sel, foc, row, col) -> {
            JLabel l = new JLabel(val.toString(), SwingConstants.CENTER);
            l.setOpaque(true);
            l.setForeground("PASS".equals(val) ? GREEN : RED);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBackground(sel ? new Color(99,102,241,80) : PANEL_BG);
            return l;
        };
    }

    // ── Styled Components ────────────────────
    JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    JTextField styledField(String hint, int width) {
        JTextField f = new JTextField();
        f.setBackground(new Color(15, 23, 42)); f.setForeground(TEXT);
        f.setCaretColor(TEXT); f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51,65,85)),
            new EmptyBorder(5,8,5,8)));
        f.setPreferredSize(new Dimension(width, 32));
        f.setMaximumSize(new Dimension(240, 32));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    JLabel sectionLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(new Color(165,180,252));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    JLabel statLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    JLabel colored(String t, Color c) {
        JLabel l = new JLabel(t); l.setForeground(c); return l;
    }

    void showMsg(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    void preload() {
        String[][] data = {
            {"Aarav Sharma","92","88","95","78","90"},
            {"Priya Patel","85","91","80","88","76"},
            {"Rohan Singh","70","65","72","68","74"},
            {"Sneha Gupta","98","95","97","99","100"},
            {"Vikram Rao","55","60","58","62","50"}
        };
        for (String[] d : data) {
            ArrayList<Double> g = new ArrayList<>();
            for (int i = 1; i < d.length; i++) g.add(Double.parseDouble(d[i]));
            gt.addStudent(new Student(d[0], g));
        }
    }
}


// ============================================================
//  5. MAIN — Choose Console or GUI mode
// ============================================================
public class StudentGradeTracker {
    public static void main(String[] args) {

        // ── If no args → ask mode via dialog ──
        String[] options = {"🖥  GUI Mode (Swing)", "⌨  Console Mode"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Welcome to Student Grade Tracker!\nChoose your interface:",
            "Student Grade Tracker",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]
        );

        if (choice == 0 || choice == JOptionPane.CLOSED_OPTION) {
            // Launch Swing GUI
            SwingUtilities.invokeLater(() -> {
                new GradeTrackerGUI().setVisible(true);
            });
        } else {
            // Launch Console Mode
            ConsoleApp.run();
        }
    }
}
