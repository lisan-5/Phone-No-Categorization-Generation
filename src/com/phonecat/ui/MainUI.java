package com.phonecat.ui;

import com.phonecat.core.Categorizer;
import com.phonecat.core.Generator;
import com.phonecat.model.CategorizationResult;
import com.phonecat.model.GeneratedNumber;
import com.phonecat.model.ScoringConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.text.ParseException;

/**
 * The main graphical user interface (GUI) for the application.
 * Uses Java Swing for the UI components.
 */
public class MainUI extends JFrame {

    // --- Theme palette ---
    private static class Theme {
        static final Color PRIMARY = new Color(99, 102, 241);      // indigo
        static final Color PRIMARY_DARK = new Color(79, 70, 229);
        static final Color ACCENT = new Color(236, 72, 153);       // pink
        static final Color SUCCESS = new Color(16, 185, 129);      // green
        static final Color WARNING = new Color(245, 158, 11);      // amber
        static final Color DANGER = new Color(239, 68, 68);        // red
        static final Color BG = new Color(248, 250, 252);          // slate-50
        static final Color CARD = Color.WHITE;
        static final Color TEXT = new Color(31, 41, 55);           // slate-800
        static final Color MUTED = new Color(226, 232, 240);       // slate-200
        static final Color MUTED_TEXT = new Color(100, 116, 139);  // slate-500
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setFocusPainted(false);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setOpaque(true);
    b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Shared config (used by Categorizer and Generator)
    private final ScoringConfig config = new ScoringConfig();
    private final Categorizer categorizer = new Categorizer(config);
    private final Generator generator = new Generator();

    // --- UI Components ---
    // Header/Status
    private JLabel statusBar;

    // Categorizer Tab
    private JTextField numberInputField;
    private JLabel resultLabel;
    private JPanel resultCard;
    private JProgressBar scoreBar;
    private JCheckBox showJsonBox;
    private JButton copyJsonBtn;
    private JTextArea jsonArea;
    private JButton categorizeButton;
    private JButton nearbyBtn;

    // Generator Tab
    private JComboBox<Integer> digitLengthBox;
    private JComboBox<String> subcategoryBox;
    private JSpinner limitSpinner;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton generateButton;
    private JLabel etaLabel;
    private JProgressBar progressBar;

    // Config Tab
    private JComboBox<String> presetBox;
    private JTextField luckyField;
    private JTextField unluckyField;
    private JTextArea profileNotesArea;
    private JButton applyConfigButton;

    public MainUI() {
        // --- Frame Setup ---
        setTitle("Phone Number Categorization & Generation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 620);
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout());

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // --- Main Tabbed Pane ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.BG);
        tabbedPane.setForeground(new Color(30, 58, 138));
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 14f));
        tabbedPane.addTab("1. Categorize", createCategorizerPanel());
        tabbedPane.addTab("2. Generate", createGeneratorPanel());
        tabbedPane.addTab("3. Configuration", createConfigPanel());
        tabbedPane.addTab("4. Docs", createDocsPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // Status Bar
        statusBar = new JLabel(" Ready");
        statusBar.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setForeground(Theme.MUTED_TEXT);
        add(statusBar, BorderLayout.SOUTH);

        // Initialize defaults
        applyPreset("none");
        updateStatusBar();
    }

    private JComponent createHeader() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.PRIMARY_DARK, getWidth(), getHeight(), Theme.ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("Phone Number Algorithm");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Categorize and generate memorable numbers with cultural context.");
        subtitle.setForeground(new Color(248, 250, 252));
        JPanel text = new JPanel(new GridLayout(2,1));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);
        p.add(text, BorderLayout.WEST);
        return p;
    }

    private JPanel createCategorizerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setBackground(Theme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Input Row ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel prompt = new JLabel("Enter 4-8 Digit Number:");
        prompt.setFont(prompt.getFont().deriveFont(Font.PLAIN, 16f));
        panel.add(prompt, gbc);

        numberInputField = new JTextField(20);
        numberInputField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panel.add(numberInputField, gbc);

        categorizeButton = new JButton("Analyze");
        categorizeButton.setFont(categorizeButton.getFont().deriveFont(Font.BOLD, 16f));
        categorizeButton.setMargin(new Insets(8,16,8,16));
    styleButton(categorizeButton, Theme.PRIMARY, Color.BLACK);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(categorizeButton, gbc);

        // Shortcuts
        InputMap im = numberInputField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = numberInputField.getActionMap();
        im.put(KeyStroke.getKeyStroke("ENTER"), "analyze");
        am.put("analyze", new AbstractAction() { public void actionPerformed(ActionEvent e) { categorizeButton.doClick(); } });

        // --- Result Card ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        resultCard = new JPanel(new BorderLayout());
        resultCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(12, 12, 12, 12)
        ));
        resultCard.setBackground(Theme.CARD);

        // Top line: number + badge
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        top.setOpaque(false);
        JLabel numberLbl = new JLabel("—");
        numberLbl.setFont(new Font("Monospaced", Font.BOLD, 18));
        JLabel badge = new JLabel("—");
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        badge.setBackground(new Color(243, 244, 246));
        badge.setForeground(new Color(55, 65, 81));
        top.add(numberLbl);
        top.add(badge);

        // Middle: category
        resultLabel = new JLabel("Result will be shown here.");
        resultLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        resultLabel.setBorder(new EmptyBorder(6, 2, 6, 2));

        // Bottom: score bar + json controls
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        scoreBar = new JProgressBar(0, 100);
        scoreBar.setStringPainted(true);
        scoreBar.setForeground(Theme.PRIMARY);
        bottom.add(scoreBar, BorderLayout.CENTER);

        JPanel jsonControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        jsonControls.setOpaque(false);
        showJsonBox = new JCheckBox("Show JSON");
        showJsonBox.setFont(showJsonBox.getFont().deriveFont(Font.PLAIN, 14f));
        copyJsonBtn = new JButton("Copy JSON");
        copyJsonBtn.setFont(copyJsonBtn.getFont().deriveFont(Font.PLAIN, 14f));
        copyJsonBtn.setMargin(new Insets(6,12,6,12));
        styleButton(copyJsonBtn, new Color(226, 232, 240), Theme.TEXT);
        jsonControls.add(showJsonBox);
        jsonControls.add(copyJsonBtn);
        bottom.add(jsonControls, BorderLayout.EAST);

        jsonArea = new JTextArea(6, 20);
        jsonArea.setEditable(false);
        jsonArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane jsonScroll = new JScrollPane(jsonArea);
        jsonScroll.setVisible(false);

        resultCard.add(top, BorderLayout.NORTH);
        resultCard.add(resultLabel, BorderLayout.CENTER);
        resultCard.add(bottom, BorderLayout.SOUTH);

        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.add(resultCard, BorderLayout.NORTH);
        cardWrapper.add(jsonScroll, BorderLayout.CENTER);
        panel.add(cardWrapper, gbc);

        // Listeners
        categorizeButton.addActionListener(e -> categorizeNumber());
        numberInputField.getDocument().addDocumentListener(new SimpleDocListener(() -> validateNumberField()));
        showJsonBox.addActionListener(e -> jsonScroll.setVisible(showJsonBox.isSelected()));
        copyJsonBtn.addActionListener(e -> {
            String txt = jsonArea.getText();
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection(txt), null);
            copyJsonBtn.setText("Copied");
            Timer t = new Timer(1200, ev -> copyJsonBtn.setText("Copy JSON"));
            t.setRepeats(false); t.start();
        });

        // Add Nearby Suggestions
        nearbyBtn = new JButton("Nearby Suggestions");
        nearbyBtn.setFont(nearbyBtn.getFont().deriveFont(Font.PLAIN, 14f));
        styleButton(nearbyBtn, new Color(226, 232, 240), Theme.TEXT);
        nearbyBtn.addActionListener(e -> showNearby());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(nearbyBtn);
        // Attach below result card
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0.0;
        panel.add(btnRow, gbc);

        return panel;
    }

    private JPanel createGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setBackground(Theme.BG);

        // --- Top Controls Panel ---
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JLabel dlbl = new JLabel("Digit Length:"); dlbl.setFont(dlbl.getFont().deriveFont(15f));
        controlsPanel.add(dlbl);
        digitLengthBox = new JComboBox<>(new Integer[]{4, 5, 6, 7, 8});
        digitLengthBox.setFont(digitLengthBox.getFont().deriveFont(15f));
        controlsPanel.add(digitLengthBox);

        JLabel slbl = new JLabel("Subcategory:"); slbl.setFont(slbl.getFont().deriveFont(15f));
        controlsPanel.add(slbl);
        subcategoryBox = new JComboBox<>(new String[]{"Premium", "Platinum", "Gold", "Silver", "Bronze"});
        subcategoryBox.setFont(subcategoryBox.getFont().deriveFont(15f));
        controlsPanel.add(subcategoryBox);

    JLabel llbl = new JLabel("Limit:"); llbl.setFont(llbl.getFont().deriveFont(15f));
        controlsPanel.add(llbl);
    limitSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 1000, 1));
        ((JSpinner.DefaultEditor)limitSpinner.getEditor()).getTextField().setFont(new Font("SansSerif", Font.PLAIN, 15));
        controlsPanel.add(limitSpinner);

    generateButton = new JButton("Generate");
        generateButton.setFont(generateButton.getFont().deriveFont(Font.BOLD, 16f));
        generateButton.setMargin(new Insets(8,16,8,16));
    // Use accent background with black text as requested
    styleButton(generateButton, Theme.ACCENT, Color.BLACK);
        controlsPanel.add(generateButton);

        JPanel north = new JPanel(new BorderLayout());
        north.add(controlsPanel, BorderLayout.NORTH);
        panel.add(north, BorderLayout.NORTH);

        // --- Results Table ---
        String[] columnNames = {"Generated Number", "Score", "Tier"};
        tableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 16));
        resultsTable.getTableHeader().setFont(resultsTable.getTableHeader().getFont().deriveFont(Font.BOLD, 15f));
        resultsTable.getTableHeader().setBackground(new Color(239, 246, 255));
        resultsTable.getTableHeader().setForeground(new Color(30, 64, 175));
        resultsTable.setRowHeight(26);
        resultsTable.setAutoCreateRowSorter(true);
        // Zebra striping and alignment
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int col){
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
                if(!isSelected){ c.setBackground(row%2==0? new Color(250,250,252): Color.WHITE); }
                if(col==1){ setHorizontalAlignment(SwingConstants.CENTER);} else { setHorizontalAlignment(SwingConstants.LEFT);} 
                return c;
            }
        };
        for (int i=0;i<resultsTable.getColumnCount();i++) resultsTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        panel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        
        // --- Progress Bar ---
        JPanel progressPanel = new JPanel(new BorderLayout(8, 4));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setFont(progressBar.getFont().deriveFont(Font.BOLD, 14f));
        progressBar.setForeground(Theme.PRIMARY);
        etaLabel = new JLabel("ETA: —");
        etaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(etaLabel, BorderLayout.EAST);
        panel.add(progressPanel, BorderLayout.SOUTH);

        // --- Action Listener ---
        generateButton.addActionListener(e -> generateNumbers());

        return panel;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(Theme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Preset selector
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(new JLabel("Cultural Preset:"), gbc);
        presetBox = new JComboBox<>(new String[]{
                "none", "east_asia", "western", "ethiopia", "china", "japan", "korea", "vietnam",
                "india", "india_south", "afghanistan", "bulgaria", "italy", "spain_greece",
                "germany", "spain_mexico", "norway", "catholic", "russia", "global"
        });
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panel.add(presetBox, gbc);

        // Lucky tokens
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panel.add(new JLabel("Lucky Tokens (comma-separated):"), gbc);
        luckyField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        panel.add(luckyField, gbc);

        // Unlucky tokens
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(new JLabel("Unlucky Tokens (comma-separated):"), gbc);
        unluckyField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panel.add(unluckyField, gbc);

        // Apply button
        applyConfigButton = new JButton("Apply Configuration");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
    styleButton(applyConfigButton, Theme.PRIMARY, Color.BLACK);
        panel.add(applyConfigButton, gbc);

        // Notes area
        profileNotesArea = new JTextArea(8, 40);
        profileNotesArea.setEditable(false);
        profileNotesArea.setLineWrap(true);
        profileNotesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(profileNotesArea);
        notesScroll.setBorder(BorderFactory.createTitledBorder("Cultural References"));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(notesScroll, gbc);

        // Events
        presetBox.addActionListener(e -> { applyPreset((String) presetBox.getSelectedItem()); updateStatusBar();});
        applyConfigButton.addActionListener(e -> { applyConfigFromUI(); updateStatusBar();});

        return panel;
    }

    private JPanel createDocsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
    ta.setText(
        "Project Overview\n" +
        "Single algorithm categorizes 4–8 digit numbers and generates numbers matching requested subcategories using a configurable scoring system.\n" +
        "Java Swing desktop UI; cultural presets and custom tokens supported.\n" +
        "\n" +
        "Algorithm Flow\n" +
        "- Validate: ensure 4–8 digits.\n" +
        "- Score: weighted sum of features (repetition, sequences, patterns, periodicity, alternation, rhythm, unique digits, cultural).\n" +
        "- Clamp: cap score to [0, 100].\n" +
        "- Map: convert score to subcategory (Premium, Platinum, Gold, Silver, Bronze).\n" +
        "\n" +
        "Scoring Formula\n" +
        "total =\n" +
        "  repetitionScore * repetitionWeight +\n" +
        "  sequenceScore   * sequenceWeight   +\n" +
        "  patternScore    * patternWeight    +\n" +
        "  periodicScore   * periodicWeight   +\n" +
        "  alternationScore* alternationWeight+\n" +
        "  rhythmScore     * rhythmWeight     +\n" +
        "  uniqueDigitScore* uniqueDigitWeight+\n" +
        "  culturalScore\n" +
        "total = clamp(total, 0, 100)\n" +
        "(Weights are length-aware: effective weights adapt for 4–8 digits.)\n" +
        "\n" +
        "Pseudocode\n" +
        "categorize(number, cfg):\n" +
        "  if not digits or len not in [4..8]: error\n" +
        "  score = calculateScore(number, cfg)\n" +
        "  tier  = tierFromScore(score)\n" +
        "  return {number, digit_category: len+\"-digit\", subcategory: tier, score}\n" +
        "\n" +
        "generate(len, tier, count, cfg):\n" +
        "  C = set()  # templates\n" +
        "  addTemplates(C,len)  # repeats, palindromes, sequences, AB/ABC, pairs\n" +
        "  R = []\n" +
        "  for n in C: if tierFromScore(score(n,cfg))==tier: R.add(n)\n" +
        "  while |R|<count and attempts<MAX:\n" +
        "    n = random len-digit number (no leading 0)\n" +
        "    if n not in C and tierFromScore(score(n,cfg))==tier: R.add(n)\n" +
        "    C.add(n)\n" +
        "  return top count by score (unique)\n" +
        "\n" +
        "calculateScore(s, cfg):\n" +
        "  repetition   = sum length(run)^2.5\n" +
        "  sequences    = +15 per asc/desc window len>=3 (overlap)\n" +
        "  patterns     = +25 palindrome; +20 AABB/ABAB (4-digit); +14 pairs-all-equal (even len)\n" +
        "  periodicity  = if minimal period p < len: (len/p - 1)*12\n" +
        "  alternation  = +15 if global period=2 with a!=b\n" +
        "  rhythm       = (len / number_of_runs) * 6\n" +
        "  uniqueDigits = (len - unique_count) * 5\n" +
        "  cultural     = +5 per lucky token; -10 per unlucky token (distinct)\n" +
        "  return weighted sum (clamped)\n" +
        "\n" +
        "Example Outputs\n" +
        "{\"number\":\"1234\",\"digit_category\":\"4-digit\",\"subcategory\":\"Gold\",\"score\":82}\n" +
        "[\n  {\"number\":\"7777\",\"digit_category\":\"4-digit\",\"subcategory\":\"Premium\",\"score\":100},\n  {\"number\":\"1221\",\"digit_category\":\"4-digit\",\"subcategory\":\"Gold\",\"score\":85}\n]\n" +
        "\n" +
        "Generation Strategy & Controls\n" +
        "- Template-first narrows search (repeats, palindromes, sequences, AB/ABC, pair blocks).\n" +
        "- Random fallback is bounded and deduplicated.\n" +
        "- UI supports Pause / Resume / Cancel with phase progress and ETA.\n" +
        "- Nearby Suggestions provide higher-scoring variants for a given number.\n" +
        "\n" +
        "Cultural Presets (examples)\n" +
        "- east_asia: Lucky 6/8/9; Unlucky 4.\n" +
        "- japan: Lucky 7; Unlucky 9,43.\n" +
        "- india: Unlucky 8,26.\n" +
        "- afghanistan: Unlucky 39.\n" +
        "- bulgaria: Unlucky specific number 0888 888 888 (context).\n" +
        "- italy: Lucky 13; Unlucky 17.\n" +
        "- spain_greece: Unlucky 13.\n" +
        "- western / ethiopia: Lucky 7; Unlucky 13,666.\n" +
        "\n" +
        "Performance Tips\n" +
        "- Template-first drastically reduces search space.\n" +
        "- Use sets to avoid rescoring duplicates; cap random attempts.\n" +
        "- Early clamp if accumulating total ≥ 100.\n" +
        "- Batch/CLI can parallelize scoring; UI remains single-threaded for safety.\n" +
        "\n" +
        "Troubleshooting\n" +
        "- If JSON export fails, add gson-2.10.1.jar to lib and run with it on the classpath.\n"
    );
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);
        return panel;
    }

    private void categorizeNumber() {
        try {
            String number = numberInputField.getText().trim();
            // Ensure latest UI config is applied
            applyConfigFromUI();
            CategorizationResult result = categorizer.categorize(number);

            // Badge color by tier
            String tier = result.subcategory.toLowerCase();
            Color badgeBg = new Color(243, 244, 246), badgeFg = new Color(55, 65, 81);
            if ("premium".equals(tier)) { badgeBg = new Color(237, 233, 254); badgeFg = new Color(109, 40, 217);} 
            else if ("platinum".equals(tier)) { badgeBg = new Color(229, 231, 235); badgeFg = new Color(55, 65, 81);} 
            else if ("gold".equals(tier)) { badgeBg = new Color(254, 243, 199); badgeFg = new Color(146, 64, 14);} 
            else if ("silver".equals(tier)) { badgeBg = new Color(224, 242, 254); badgeFg = new Color(7, 89, 133);} 
            else { badgeBg = new Color(243, 244, 246); badgeFg = new Color(75, 85, 99);} 

            JLabel numberLbl = (JLabel)((JPanel)resultCard.getComponent(0)).getComponent(0);
            JLabel badge = (JLabel)((JPanel)resultCard.getComponent(0)).getComponent(1);
            numberLbl.setText(result.number);
            badge.setText(result.subcategory);
            badge.setBackground(badgeBg);
            badge.setForeground(badgeFg);

            resultLabel.setText(String.format("<html><div style='font-size:13px'><b>Category:</b> %s &nbsp; <b>Digits:</b> %s</div></html>",
                    result.digit_category, result.number.length()+""));

            scoreBar.setValue(result.score);
            scoreBar.setForeground(Theme.PRIMARY); // indigo

            // JSON
            try { jsonArea.setText(result.toJson()); } catch (Throwable t) { jsonArea.setText("(Gson not on classpath)"); }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
            resultLabel.setText("Please enter a valid number.");
        }
    }

    private void generateNumbers() {
    int digitLength = (int) digitLengthBox.getSelectedItem();
    String subcategory = (String) subcategoryBox.getSelectedItem();
    try { limitSpinner.commitEdit(); } catch (ParseException ignored) {}
    int limit = (int) limitSpinner.getValue();

        // --- Prepare UI for generation ---
        tableModel.setRowCount(0); // Clear previous results
    generateButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Generating...");
        etaLabel.setText("ETA: —");
        
        // Ensure config is up-to-date
        applyConfigFromUI();

    AtomicReference<Generator.Control> ctrlRef = new AtomicReference<>(new Generator.Control());

        // --- Use SwingWorker for background processing ---
        SwingWorker<Void, GeneratedNumber> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Generator.Control control = ctrlRef.get();
                generator.generateFast(digitLength, subcategory, limit, config,
                        control,
                        p -> SwingUtilities.invokeLater(() -> updateProgress(p)),
                        this::publish,
                        () -> {});
                return null;
            }
            @Override
            protected void process(List<GeneratedNumber> chunks) {
                for (GeneratedNumber gn : chunks) {
                    String tier = Categorizer.getSubcategoryForScore(gn.score);
                    tableModel.addRow(new Object[]{gn.number, gn.score, tier});
                }
                if (!progressBar.isIndeterminate()) {
                    int lim = Math.max(1, limit);
                    int pct = Math.min(100, (int) Math.round(100.0 * tableModel.getRowCount() / lim));
                    progressBar.setValue(pct);
                }
            }
            @Override
            protected void done() {
                generateButton.setEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                progressBar.setString(tableModel.getRowCount() == 0 ? "No numbers found." : "Generation Complete!");
                if (etaLabel.getText() == null || etaLabel.getText().isEmpty()) etaLabel.setText("ETA: —");
            }
        };
        
        worker.execute();
    }

    private void updateProgress(Generator.Progress p) {
        if (p.phase == 1) {
            progressBar.setIndeterminate(false);
            progressBar.setString("Templates: " + (int) Math.round(p.fraction * 100) + "%");
            progressBar.setValue((int) Math.round(p.fraction * 70));
        } else {
            progressBar.setString("Random: " + (int) Math.round(p.fraction * 100) + "%");
            progressBar.setValue(70 + (int) Math.round(p.fraction * 30));
        }
        if (p.etaMillis >= 0) {
            long sec = Math.max(0, p.etaMillis / 1000);
            etaLabel.setText("ETA: " + sec + "s");
        }
    }

    private void showNearby() {
        String number = numberInputField.getText().trim();
        if (!number.matches("\\d{4,8}")) {
            JOptionPane.showMessageDialog(this, "Enter a valid 4–8 digit number first.", "Nearby Suggestions", JOptionPane.WARNING_MESSAGE);
            return;
        }
        applyConfigFromUI();
        List<GeneratedNumber> suggestions = generator.suggestNearby(number, 10, config);
        if (suggestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No higher-scoring nearby suggestions found.", "Nearby Suggestions", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (GeneratedNumber g : suggestions) sb.append(g.number).append("  (score: ").append(g.score).append(")\n");
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(360, 220));
        JOptionPane.showMessageDialog(this, sp, "Nearby Suggestions", JOptionPane.PLAIN_MESSAGE);
    }

    // --- Config helpers ---
    private void applyConfigFromUI() {
        // Parse tokens from text fields
        Set<String> lucky = Arrays.stream(luckyField.getText().trim().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        Set<String> unlucky = Arrays.stream(unluckyField.getText().trim().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        config.luckyTokens.clear();
        config.luckyTokens.addAll(lucky);
        config.unluckyTokens.clear();
        config.unluckyTokens.addAll(unlucky);
        config.profileName = (String) presetBox.getSelectedItem();
        // Keep per-digit sets in sync for backward compatibility
        config.luckyDigits.clear();
        for (String t : lucky) if (t.length() == 1 && Character.isDigit(t.charAt(0))) config.luckyDigits.add(t.charAt(0));
        config.unluckyDigits.clear();
        for (String t : unlucky) if (t.length() == 1 && Character.isDigit(t.charAt(0))) config.unluckyDigits.add(t.charAt(0));
    }

    private void applyPreset(String key) {
        Map<String, Preset> presets = presets();
        Preset p = presets.getOrDefault(key, presets.get("none"));
        presetBox.setSelectedItem(key);
        luckyField.setText(String.join(", ", p.lucky));
        unluckyField.setText(String.join(", ", p.unlucky));
        profileNotesArea.setText(p.notes);
        applyConfigFromUI();
    }

    private void updateStatusBar() {
        String left = " Profile: " + config.profileName + "  |  Lucky: " + String.join(", ", config.luckyTokens) + "  |  Unlucky: " + String.join(", ", config.unluckyTokens);
        String right = "  |  Built for Internship Project | Phone Number Algorithm";
        statusBar.setText(left + right);
    }

    private String tokensMatchedString(String number) {
        String luckyHits = config.luckyTokens.stream().filter(number::contains).collect(Collectors.joining(", "));
        String unluckyHits = config.unluckyTokens.stream().filter(number::contains).collect(Collectors.joining(", "));
        StringBuilder sb = new StringBuilder();
        if (!luckyHits.isEmpty()) sb.append("Lucky [").append(luckyHits).append("] ");
        if (!unluckyHits.isEmpty()) sb.append("Unlucky [").append(unluckyHits).append("] ");
        return sb.toString().trim();
    }

    // Minimal preset model
    private static class Preset {
        final String[] lucky;
        final String[] unlucky;
        final String notes;
        Preset(String[] lucky, String[] unlucky, String notes) {
            this.lucky = lucky; this.unlucky = unlucky; this.notes = notes;
        }
    }

    private Map<String, Preset> presets() {
        Map<String, Preset> m = new LinkedHashMap<>();
        m.put("none", new Preset(new String[]{}, new String[]{}, "No cultural weighting applied (pure pattern scoring). Choose this for neutral comparisons."));

        // East Asia block
        m.put("east_asia", new Preset(
                new String[]{"6","8","9"}, new String[]{"4"},
                "East Asia (China, Japan, Korea, Vietnam): 6/8/9 auspicious; 4 inauspicious."
        ));
        m.put("china", new Preset(
                new String[]{"6","8","9"}, new String[]{"4"},
                "China: 6/8/9 auspicious; 4 (death) unlucky."
        ));
        m.put("korea", new Preset(
                new String[]{"6","8","9"}, new String[]{"4"},
                "Korea: 6/8/9 positive; 4 sometimes avoided."
        ));
        m.put("vietnam", new Preset(
                new String[]{"6","8","9"}, new String[]{"4"},
                "Vietnam: Chinese-influenced; 8/9 lucky; 4 unlucky."
        ));
        m.put("japan", new Preset(
                new String[]{"7"}, new String[]{"9","43"},
                "Japan: 7 lucky; 9 (suffering) and 43 (unlucky pair) avoided."
        ));

        // India
        m.put("india", new Preset(
                new String[]{}, new String[]{"8","26"},
                "India: 8 and 26 considered inauspicious in some contexts."
        ));
        m.put("india_south", new Preset(
                new String[]{"3"}, new String[]{"8"},
                "South India: 8 linked to Sani (inauspicious); 3 sometimes positive."
        ));

        // Afghanistan
        m.put("afghanistan", new Preset(
                new String[]{}, new String[]{"39"},
                "Afghanistan: 39 has negative slang association."
        ));

        // Bulgaria (specific phone number folklore)
        m.put("bulgaria", new Preset(
                new String[]{}, new String[]{"08888888888"},
                "Bulgaria: specific number 0888 888 888 is considered cursed (folklore). Note: token length exceeds 4–8 digits used by this app."
        ));

        // Italy
        m.put("italy", new Preset(
                new String[]{"13"}, new String[]{"17"},
                "Italy: 13 lucky (e.g., lottery phrase 'fare tredici'); 17 unlucky."
        ));

        // Spain & Greece
        m.put("spain_greece", new Preset(
                new String[]{}, new String[]{"13"},
                "Spain & Greece: 13 considered unlucky."
        ));

        // Western & Ethiopia
        m.put("western", new Preset(
                new String[]{"7"}, new String[]{"13","666"},
                "Western: 7 lucky; 13 and 666 unlucky."
        ));
        m.put("ethiopia", new Preset(
                new String[]{"7"}, new String[]{"13","666"},
                "Ethiopia: 7 often favorable; 13/666 negative in some contexts."
        ));

        // Keep previous examples
        m.put("global", new Preset(
                new String[]{"3","7","8","9","13","15","39","666"},
                new String[]{"4","7","8","9","13","39","666"},
                "Global mix sample across regions (broad illustrative preset)."
        ));
        m.put("germany", new Preset(
                new String[]{"4"}, new String[]{},
                "Germany: 4 associated with four-leaf clover (luck)."
        ));
        m.put("spain_mexico", new Preset(
                new String[]{"15","3"}, new String[]{},
                "Spain & Mexico: 15 often considered lucky."
        ));
        m.put("norway", new Preset(
                new String[]{"9"}, new String[]{},
                "Norway: 9 has Norse sacred connotations."
        ));
        m.put("catholic", new Preset(
                new String[]{"39","3"}, new String[]{"666"},
                "Catholic contexts: 39/3 positive; 666 negative."
        ));
        m.put("russia", new Preset(
                new String[]{}, new String[]{},
                "Russia: various beliefs; even counts sometimes somber; kept neutral here."
        ));
        return m;
    }

    // Simple DocumentListener adapter
    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r; SimpleDocListener(Runnable r){ this.r = r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e){ r.run(); }
    }

    private void validateNumberField() {
        String s = numberInputField.getText().trim();
        boolean valid = s.matches("\\d{4,8}");
        categorizeButton.setEnabled(valid);
    }
}
