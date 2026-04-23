import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class ScreenManager extends JPanel {


    private final CardLayout cards = new CardLayout();
    private JFrame parentFrame;


    private int  aiDifficulty = 1;
    private int  targetScore  = 5;
    private GamePanel.GameMode selectedMode = GamePanel.GameMode.ONE_PLAYER;
    private GamePanel activeGame = null;


    public ScreenManager(JFrame frame) {
        this.parentFrame = frame;
        setLayout(cards);
        setPreferredSize(new Dimension(620, 700));
        add(buildTitleScreen(),    "TITLE");
        add(buildModeScreen(),     "MODE_SELECT");
        add(buildControlsScreen(), "CONTROLS");
        add(buildSettingsScreen(), "SETTINGS");
        cards.show(this, "TITLE");
    }


    private JPanel buildTitleScreen() {
        JPanel p = darkPanel();
        JLabel title = centreLabel("TANK TROUBLE", 54, Color.WHITE);
        title.setBounds(60, 120, 500, 70);
        p.add(title);
        JLabel sub = centreLabel("Table 2", 16, new Color(180, 180, 180));
        sub.setBounds(60, 190, 500, 25);
        p.add(sub);
        JButton btnPlay     = makeBigBtn("> Play");
        JButton btnHowTo    = makeBigBtn("How to play");
        JButton btnSettings = makeBigBtn("Settings");
        btnPlay    .setBounds(210, 280, 200, 52);
        btnHowTo   .setBounds(210, 350, 200, 52);
        btnSettings.setBounds(210, 420, 200, 52);
        btnPlay    .addActionListener(e -> cards.show(this, "MODE_SELECT"));
        btnHowTo   .addActionListener(e -> cards.show(this, "CONTROLS"));
        btnSettings.addActionListener(e -> cards.show(this, "SETTINGS"));
        p.add(btnPlay); p.add(btnHowTo); p.add(btnSettings);
        return p;
    }


    private JPanel buildModeScreen() {
        JPanel p = darkPanel();
        JLabel lbl = centreLabel("Choose players", 26, Color.WHITE);
        lbl.setBounds(110, 60, 400, 40);
        p.add(lbl);
        JToggleButton btn1P = modeToggle("1P vs AI");
        JToggleButton btn2P = modeToggle("2P local");
        ButtonGroup grp = new ButtonGroup();
        grp.add(btn1P); grp.add(btn2P);
        btn1P.setSelected(true);
        btn1P.setBounds(110, 130, 160, 80);
        btn2P.setBounds(350, 130, 160, 80);
        p.add(btn1P); p.add(btn2P);
        JLabel scoreLbl = centreLabel("Score to win:", 16, new Color(200,200,200));
        scoreLbl.setBounds(110, 250, 200, 28);
        p.add(scoreLbl);
        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        styleSpinner(scoreSpinner);
        scoreSpinner.setBounds(320, 248, 70, 30);
        p.add(scoreSpinner);
        JButton btnBack  = makeSmallBtn("< Back");
        JButton btnStart = makeBigBtn("Start match >");
        btnBack .setBounds(30,  620, 120, 40);
        btnStart.setBounds(360, 615, 200, 50);
        p.add(btnBack); p.add(btnStart);
        btn1P.addActionListener(e -> selectedMode = GamePanel.GameMode.ONE_PLAYER);
        btn2P.addActionListener(e -> selectedMode = GamePanel.GameMode.TWO_PLAYER);
        btnBack .addActionListener(e -> cards.show(this, "TITLE"));
        btnStart.addActionListener(e -> {
            targetScore = (int) scoreSpinner.getValue();
            startGame();
        });
        return p;
    }


    private JPanel buildControlsScreen() {
        JPanel p = darkPanel();
        JLabel lbl = centreLabel("Controls", 30, Color.WHITE);
        lbl.setBounds(110, 40, 400, 42);
        p.add(lbl);
        String[][] rows = {
            {"Player 1 (Green)", "Player 2 (Red / AI)"},
            {"W / S - forward / back", "Up / Down - forward / back"},
            {"A / D - rotate", "Left / Right - rotate"},
            {"R - fire / use item", "SPACE - fire / use item"},
            {"ENTER - pause", "P - pause"},
            {"", ""},
            {"Power-ups appear as crates. Drive over to collect.", ""},
            {"L=Laser  S=Shield  F=Frag Bomb  G=Shotgun", ""},
            {"H=Homing Missile  R=RC Missile", ""},
        };
        int y = 120;
        for (String[] row : rows) {
            JLabel l1 = new JLabel(row[0]);
            JLabel l2 = new JLabel(row[1]);
            l1.setForeground(new Color(100, 220, 100));
            l2.setForeground(new Color(220, 100, 100));
            l1.setFont(new Font("SansSerif", Font.PLAIN, 13));
            l2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            l1.setBounds(30, y, 280, 22);
            l2.setBounds(330, y, 280, 22);
            p.add(l1); p.add(l2);
            y += 26;
        }
        JButton back = makeSmallBtn("< Back");
        back.setBounds(30, 640, 120, 38);
        back.addActionListener(e -> cards.show(this, "TITLE"));
        p.add(back);
        return p;
    }


    private JPanel buildSettingsScreen() {
        JPanel p = darkPanel();
        JLabel title = centreLabel("Settings", 28, Color.WHITE);
        title.setBounds(110, 40, 400, 42);
        p.add(title);
        p.add(sliderRow(p, "Music volume",  70, 160));
        p.add(sliderRow(p, "Sound effects", 75, 220));
        JLabel diffLbl = new JLabel("Difficulty (AI)");
        diffLbl.setForeground(Color.WHITE);
        diffLbl.setFont(new Font("Arial", Font.BOLD, 14));
        diffLbl.setBounds(60, 290, 160, 26);
        p.add(diffLbl);
        JSlider diffSlider = new JSlider(0, 2, aiDifficulty);
        diffSlider.setMajorTickSpacing(1);
        diffSlider.setPaintTicks(true);
        diffSlider.setSnapToTicks(true);
        diffSlider.setBackground(new Color(30, 30, 50));
        diffSlider.setForeground(Color.WHITE);
        java.util.Hashtable<Integer,JLabel> labels = new java.util.Hashtable<>();
        labels.put(0, styledSliderLabel("Easy"));
        labels.put(1, styledSliderLabel("Medium"));
        labels.put(2, styledSliderLabel("Hard"));
        diffSlider.setLabelTable(labels);
        diffSlider.setPaintLabels(true);
        diffSlider.setBounds(280, 285, 270, 50);
        diffSlider.addChangeListener(e -> aiDifficulty = diffSlider.getValue());
        p.add(diffSlider);
        JButton back = makeSmallBtn("< Back");
        back.setBounds(30, 640, 120, 38);
        back.addActionListener(e -> cards.show(this, "TITLE"));
        p.add(back);
        return p;
    }


    private JLabel styledSliderLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.PLAIN, 11));
        return l;
    }


    private JSlider sliderRow(JPanel parent, String label, int val, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setBounds(60, y, 160, 26);
        parent.add(lbl);
        JLabel valLbl = new JLabel(val + "%");
        valLbl.setForeground(new Color(180,180,180));
        valLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        valLbl.setBounds(570, y, 40, 26);
        parent.add(valLbl);
        JSlider s = new JSlider(0, 100, val);
        s.setBackground(new Color(30,30,50));
        s.setBounds(230, y, 320, 26);
        s.addChangeListener(e -> valLbl.setText(s.getValue() + "%"));
        parent.add(s);
        return s;
    }


    private void startGame() {
        if (activeGame != null) remove(activeGame);
        activeGame = new GamePanel(selectedMode, targetScore, aiDifficulty, () -> {
            remove(activeGame);
            activeGame = null;
            cards.show(this, "TITLE");
            parentFrame.pack();
        });
        add(activeGame, "GAME");
        cards.show(this, "GAME");
        parentFrame.pack();
        activeGame.requestFocusInWindow();
    }


    private JPanel darkPanel() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(15, 15, 25));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setPreferredSize(new Dimension(620, 700));
        return p;
    }


    private JLabel centreLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Impact", Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }


    private JButton makeBigBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(40, 40, 70));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(70,70,120)); }
            public void mouseExited (MouseEvent e) { b.setBackground(new Color(40,40, 70)); }
        });
        return b;
    }


    private JButton makeSmallBtn(String text) {
        JButton b = makeBigBtn(text);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        return b;
    }


    private JToggleButton modeToggle(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setBackground(new Color(40, 40, 70));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addChangeListener(e -> b.setBackground(b.isSelected() ? new Color(80,80,160) : new Color(40,40,70)));
        return b;
    }


    private void styleSpinner(JSpinner s) {
        s.setBackground(new Color(40,40,70));
        s.setForeground(Color.WHITE);
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(new Color(40, 40, 70));
            tf.setForeground(Color.WHITE);
            tf.setFont(new Font("Arial", Font.BOLD, 14));
        }
    }
}



