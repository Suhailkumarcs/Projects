import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class NotebookApp {

    private static final String DB_FILE = "notebook.db";
    private static final Map<String, String> notes = new LinkedHashMap<>();

    // GUI Components
    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> noteList;
    private JTextArea bodyArea;
    private JTextField titleField;
    private JTextField searchField;
    private JLabel statusLabel;

    // Colors
    private static final Color BG_DARK    = new Color(30, 30, 30);
    private static final Color BG_PANEL   = new Color(40, 44, 52);
    private static final Color BG_INPUT   = new Color(50, 55, 65);
    private static final Color ACCENT     = new Color(97, 175, 254);
    private static final Color TEXT_WHITE = new Color(220, 220, 220);
    private static final Color TEXT_MUTED = new Color(140, 140, 140);
    private static final Color BTN_ADD    = new Color(80, 161, 79);
    private static final Color BTN_DEL    = new Color(190, 70, 70);
    private static final Color BTN_SAVE   = new Color(60, 130, 200);

    public static void main(String[] args) {
        load();
        SwingUtilities.invokeLater(() -> new NotebookApp().createAndShow());
    }

    private void createAndShow() {
        frame = new JFrame("📓 My Notebook");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setMinimumSize(new Dimension(700, 450));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_DARK);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                save();
                System.exit(0);
            }
        });

        frame.setLayout(new BorderLayout(0, 0));
        frame.add(buildTopBar(), BorderLayout.NORTH);
        frame.add(buildLeftPanel(), BorderLayout.WEST);
        frame.add(buildCenterPanel(), BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        frame.setVisible(true);
        setStatus("Notebook opened — " + notes.size() + " notes found");
    }

    // ── TOP BAR ──────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(25, 25, 25));
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("📓  My Notebook");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT);
        bar.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);

        searchField = styledField("🔍  Search notes...", 20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterNotes(); }
            public void removeUpdate(DocumentEvent e)  { filterNotes(); }
            public void changedUpdate(DocumentEvent e) { filterNotes(); }
        });

        JButton clearBtn = smallBtn("✕", BTN_DEL);
        clearBtn.addActionListener(e -> { searchField.setText(""); loadAllNotes(); });

        searchPanel.add(searchField);
        searchPanel.add(clearBtn);
        bar.add(searchPanel, BorderLayout.EAST);

        return bar;
    }

    // ── LEFT PANEL ────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_PANEL);
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(new EmptyBorder(12, 10, 12, 10));

        JLabel lbl = new JLabel("All Notes");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 4, 6, 0));

        listModel = new DefaultListModel<>();
        loadAllNotes();

        noteList = new JList<>(listModel);
        noteList.setBackground(BG_INPUT);
        noteList.setForeground(TEXT_WHITE);
        noteList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noteList.setSelectionBackground(ACCENT);
        noteList.setSelectionForeground(Color.WHITE);
        noteList.setBorder(new EmptyBorder(4, 6, 4, 6));
        noteList.setFixedCellHeight(32);

        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) openSelectedNote();
        });

        JScrollPane scroll = new JScrollPane(noteList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 75)));
        scroll.getViewport().setBackground(BG_INPUT);

        JButton addBtn  = bigBtn("＋  New Note", BTN_ADD);
        JButton delBtn  = bigBtn("🗑  Delete", BTN_DEL);
        JButton saveBtn = bigBtn("💾  Save All", BTN_SAVE);

        addBtn.addActionListener(e -> addNewNote());
        delBtn.addActionListener(e -> deleteSelectedNote());
        saveBtn.addActionListener(e -> { save(); setStatus("✓ All notes saved successfully!"); });

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        btnPanel.setOpaque(false);
        btnPanel.add(addBtn);
        btnPanel.add(delBtn);
        btnPanel.add(saveBtn);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ── CENTER PANEL (Editor) ─────────────────────────────────────────────────
    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        titleField = new JTextField();

        titleField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleField.setBackground(BG_INPUT);
        titleField.setForeground(TEXT_WHITE);

        titleField.setCaretColor(ACCENT);
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 75, 90)),
            new EmptyBorder(8, 12, 8, 12)
        ));

        bodyArea = new JTextArea();
        bodyArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bodyArea.setBackground(BG_INPUT);
        bodyArea.setForeground(TEXT_WHITE);
        bodyArea.setCaretColor(ACCENT);

        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        bodyScroll.getViewport().setBackground(BG_INPUT);


        JButton saveNoteBtn = bigBtn("✓  Save Note", BTN_SAVE);
        saveNoteBtn.addActionListener(e -> saveCurrentNote());
        JPanel saveRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        saveRow.setOpaque(false);
        saveRow.add(saveNoteBtn);

        JLabel titleLbl = label("Title:");
        JLabel bodyLbl  = label("Start writing your note:");

        panel.add(wrap(titleLbl, titleField), BorderLayout.NORTH);
        panel.add(bodyScroll, BorderLayout.CENTER);
        panel.add(wrap(bodyLbl, saveRow), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel wrap(JComponent top, JComponent bottom) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(top, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // ── STATUS BAR ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(25, 25, 25));
        bar.setBorder(new EmptyBorder(5, 16, 5, 16));
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_MUTED);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ── ACTIONS ───────────────────────────────────────────────────────────────
    private void addNewNote() {
        titleField.setText("");
        bodyArea.setText("");
        titleField.requestFocus();
        noteList.clearSelection();
        setStatus("New note — enter a title above to get started");
    }

    private void saveCurrentNote() {
        String title = titleField.getText().trim();
        String body  = bodyArea.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Title cannot be empty!",
                "Error", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }

        boolean isNew = !notes.containsKey(title);
        notes.put(title, body);
        save();

        if (isNew) listModel.addElement(title);
        noteList.setSelectedValue(title, true);
        setStatus("✓ \"" + title + "\" saved successfully!");
    }

    private void openSelectedNote() {
        String selected = noteList.getSelectedValue();
        if (selected == null) return;
        titleField.setText(selected);
        bodyArea.setText(notes.getOrDefault(selected, ""));
        bodyArea.setCaretPosition(0);
        setStatus("Opened \"" + selected + "\" — ready to edit");
    }

    private void deleteSelectedNote() {
        String selected = noteList.getSelectedValue();
        if (selected == null) {
            setStatus("Please select a note first!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(frame,
            "Are you sure you want to delete \"" + selected + "\"?",
            "Delete Note", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            notes.remove(selected);
            listModel.removeElement(selected);
            titleField.setText("");
            bodyArea.setText("");
            save();
            setStatus("\"" + selected + "\" has been deleted");
        }
    }

    private void filterNotes() {
        String q = searchField.getText().toLowerCase(Locale.ROOT).trim();
        listModel.clear();
        for (Map.Entry<String, String> e : notes.entrySet()) {
            if (e.getKey().toLowerCase().contains(q) ||
                e.getValue().toLowerCase().contains(q)) {
                listModel.addElement(e.getKey());
            }
        }
        setStatus(listModel.size() + " note(s) found for \"" + q + "\"");
    }

    private void loadAllNotes() {
        listModel.clear();
        for (String t : notes.keySet()) listModel.addElement(t);
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JButton bigBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private JButton smallBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(4, 8, 4, 8));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private JTextField styledField(String placeholder, int cols) {
        JTextField f = new JTextField(cols);
        f.setText(placeholder);
        f.setForeground(TEXT_MUTED);
        f.setBackground(BG_INPUT);
        f.setCaretColor(TEXT_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 75, 90)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText(""); f.setForeground(TEXT_WHITE);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder); f.setForeground(TEXT_MUTED);
                }
            }
        });
        return f;
    }

    // ── PERSISTENCE ───────────────────────────────────────────────────────────
    private static void load() {
        notes.clear();
        Path p = Paths.get(DB_FILE);
        if (!Files.exists(p)) return;
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line, currentTitle = null;
            StringBuilder body = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("TITLE=")) {
                    currentTitle = line.substring(6); body = new StringBuilder();
                } else if (line.equals("ENDNOTE")) {
                    if (currentTitle != null && body != null) {
                        String b = body.toString();
                        if (b.endsWith(System.lineSeparator()))
                            b = b.substring(0, b.length() - System.lineSeparator().length());
                        notes.put(currentTitle, b);
                    }
                    currentTitle = null; body = null;
                } else if (!line.equals("BODY") && body != null) {
                    body.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: load failed: " + e.getMessage());
        }
    }

    private static void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DB_FILE),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Map.Entry<String, String> e : notes.entrySet()) {
                bw.write("TITLE=" + e.getKey()); bw.newLine();
                bw.write("BODY"); bw.newLine();
                for (String l : (e.getValue() == null ? "" : e.getValue()).split("\\R", -1)) {
                    bw.write(l); bw.newLine();
                }
                bw.write("ENDNOTE"); bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Warning: save failed: " + e.getMessage());
        }
    }
}