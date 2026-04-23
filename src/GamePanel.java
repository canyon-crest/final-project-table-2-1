import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
 * GamePanel – the in-game panel.
 *
 * Manages the game loop, all entities, scoring, pausing, and
 * transitions back to the main menu.
 */
public class GamePanel extends JPanel implements ActionListener {


    // ── Game config ──
    public enum GameMode { ONE_PLAYER, TWO_PLAYER }
    private GameMode mode;
    private int targetScore;
    private int aiDifficulty;   // 0 easy, 1 medium, 2 hard


    // ── State ──
    private enum State { PLAYING, PAUSED_P1, PAUSED_P2, ROUND_END }
    private State state = State.PLAYING;


    // ── Entities ──
    private Maze         maze;
    private PlayerTank   p1;
    private Tank         p2;    // PlayerTank or AITank
    private int          score1 = 0, score2 = 0;


    private final ArrayList<Bullet>       bullets   = new ArrayList<>();
    private final ArrayList<HomingMissile> missiles  = new ArrayList<>();
    private final ArrayList<RCMissile>    rcMissiles = new ArrayList<>();
    private final ArrayList<Laser>        lasers    = new ArrayList<>();
    private final ArrayList<FragBomb>     bombs     = new ArrayList<>();
    private final ArrayList<PowerUp>      powerUps  = new ArrayList<>();


    // ── Timer / loop ──
    private final javax.swing.Timer timer;
    private static final int FPS = 62;


    // ── Power-up spawn ──
    private int powerUpSpawnTimer = 0;
    private static final int POWERUP_INTERVAL = 70; // ~7 s


    // ── Pause buttons ──
    private JButton btnResume, btnRestart, btnMenu;
    private JButton btnResume2, btnRestart2, btnMenu2;
    private JPanel  pausePanel1, pausePanel2;


    // ── Callback ──
    private final Runnable onReturnToMenu;


    // ── Round-end overlay ──
    private int roundEndTimer = 0;
    private String roundEndMsg = "";


    // ── Pause key tracking ──
    private boolean escPressedP1 = false;
    private boolean escPressedP2 = false;


    // P1 pause = ENTER, P2 pause = P
    private static final int PAUSE_KEY_P1 = KeyEvent.VK_ENTER;
    private static final int PAUSE_KEY_P2 = KeyEvent.VK_P;


    public GamePanel(GameMode mode, int targetScore, int aiDifficulty, Runnable onReturnToMenu) {
        this.mode           = mode;
        this.targetScore    = targetScore;
        this.aiDifficulty   = aiDifficulty;
        this.onReturnToMenu = onReturnToMenu;


        setPreferredSize(new Dimension(620, 700));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);


        buildPauseUI();
        resetRound();


        timer = new javax.swing.Timer(1000 / FPS, this);
        timer.start();


        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { handleKey(e.getKeyCode(), true); }
            @Override public void keyReleased(KeyEvent e) { handleKey(e.getKeyCode(), false); }
        });
    }


    // ────────────────────────────────────────────
    //  Pause UI
    // ────────────────────────────────────────────
    private void buildPauseUI() {
        pausePanel1 = buildOnePausePanel("Player 1 paused", "P2 to quit / forfeit to P2 wins!",
                () -> resumeGame(),
                () -> { score1 = 0; score2 = 0; resetRound(); resumeGame(); },
                onReturnToMenu);
        pausePanel1.setBounds(160, 180, 300, 220);
        pausePanel1.setVisible(false);
        add(pausePanel1);


        pausePanel2 = buildOnePausePanel("Player 2 paused", "P1 to quit / forfeit to P1 wins!",
                () -> resumeGame(),
                () -> { score1 = 0; score2 = 0; resetRound(); resumeGame(); },
                onReturnToMenu);
        pausePanel2.setBounds(160, 180, 300, 220);
        pausePanel2.setVisible(false);
        add(pausePanel2);
    }


    private JPanel buildOnePausePanel(String title, String subtitle,
                                       Runnable onResume, Runnable onRestart,
                                       Runnable onMenu) {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(20, 20, 30, 230));
        p.setBorder(BorderFactory.createLineBorder(new Color(200, 50, 50), 2));


        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setForeground(new Color(255, 80, 80));
        lbl.setBounds(10, 10, 280, 30);
        p.add(lbl);


        JLabel sub = new JLabel("<html><center>" + subtitle + "</center></html>", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 11));
        sub.setForeground(new Color(255, 80, 80));
        sub.setBounds(10, 42, 280, 30);
        p.add(sub);


        JButton bResume  = makeMenuBtn("Resume");
        JButton bRestart = makeMenuBtn("Restart match");
        JButton bEnd     = makeMenuBtn("End match");


        bResume .setBounds(75, 85,  150, 36);
        bRestart.setBounds(75, 130, 150, 36);
        bEnd    .setBounds(75, 175, 150, 36);


        bResume .addActionListener(e -> onResume.run());
        bRestart.addActionListener(e -> onRestart.run());
        bEnd    .addActionListener(e -> { timer.stop(); onMenu.run(); });


        p.add(bResume); p.add(bRestart); p.add(bEnd);
        return p;
    }


    private JButton makeMenuBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(40, 40, 60));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(70, 70, 100)); }
            public void mouseExited (MouseEvent e) { b.setBackground(new Color(40, 40,  60)); }
        });
        return b;
    }


    // ────────────────────────────────────────────
    //  Round reset
    // ────────────────────────────────────────────
    private void resetRound() {
        bullets.clear(); missiles.clear(); rcMissiles.clear();
        lasers.clear(); bombs.clear(); powerUps.clear();
        powerUpSpawnTimer = 0;


        maze = new Maze();


        p1 = new PlayerTank(30, 30, Color.GREEN,
                KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_R);


        if (mode == GameMode.TWO_PLAYER) {
            p2 = new PlayerTank(570, 570, Color.RED,
                    KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                    KeyEvent.VK_SPACE);
        } else {
            p2 = new AITank(570, 570, Color.RED, aiDifficulty);
        }
    }


    // ────────────────────────────────────────────
    //  Input
    // ────────────────────────────────────────────
    private void handleKey(int k, boolean pressed) {
        if (pressed && k == PAUSE_KEY_P1 && state == State.PLAYING) {
            state = State.PAUSED_P1;
            pausePanel1.setVisible(true);
            pausePanel2.setVisible(false);
            return;
        }
        if (pressed && k == PAUSE_KEY_P2 && state == State.PLAYING && mode == GameMode.TWO_PLAYER) {
            state = State.PAUSED_P2;
            pausePanel2.setVisible(true);
            pausePanel1.setVisible(false);
            return;
        }


        if (state != State.PLAYING) return;


        p1.handleInput(k, pressed);
        if (mode == GameMode.TWO_PLAYER) ((PlayerTank)p2).handleInput(k, pressed);
    }


    private void resumeGame() {
        state = State.PLAYING;
        pausePanel1.setVisible(false);
        pausePanel2.setVisible(false);
        requestFocusInWindow();
    }


    // ────────────────────────────────────────────
    //  Game loop
    // ────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state != State.PLAYING) { repaint(); return; }


        // Round-end countdown
        if (state == State.ROUND_END) {
            if (--roundEndTimer <= 0) {
                state = State.PLAYING;
                resetRound();
            }
            repaint(); return;
        }


        // Update tanks
        p1.update(maze, bullets, missiles, rcMissiles, lasers, bombs, p2);
        p2.update(maze, bullets, missiles, rcMissiles, lasers, bombs, p1);


        // Update bullets
        for (int i = bullets.size()-1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (b.update(maze)) { bullets.remove(i); continue; }
            // Bullet can only hit you after it has bounced at least once (prevents instant self-hit)
            boolean canHitOwner = b.hasBounced();
            if ((canHitOwner || b.owner != p1) && p1.alive && p1.getBounds().intersects(b.getBounds())) {
                if (p1.hit()) { score2++; checkWinAndReset(); return; }
                bullets.remove(i); continue;
            }
            if ((canHitOwner || b.owner != p2) && p2.alive && p2.getBounds().intersects(b.getBounds())) {
                if (p2.hit()) { score1++; checkWinAndReset(); return; }
                bullets.remove(i);
            }
        }


        // Update homing missiles
        for (int i = missiles.size()-1; i >= 0; i--) {
            HomingMissile m = missiles.get(i);
            if (m.update(maze)) { missiles.remove(i); continue; }
            if (m.owner != p1 && p1.alive && p1.getBounds().intersects(m.getBounds())) {
                if (p1.hit()) { score2++; checkWinAndReset(); return; }
                missiles.remove(i); continue;
            }
            if (m.owner != p2 && p2.alive && p2.getBounds().intersects(m.getBounds())) {
                if (p2.hit()) { score1++; checkWinAndReset(); return; }
                missiles.remove(i);
            }
        }


        // Update RC missiles
        for (int i = rcMissiles.size()-1; i >= 0; i--) {
            RCMissile r = rcMissiles.get(i);
            // RC missile updating is handled inside PlayerTank.update already,
            // but we still need to check hits here
            if (r.owner != p1 && p1.alive && p1.getBounds().intersects(r.getBounds())) {
                if (p1.hit()) { score2++; checkWinAndReset(); return; }
                rcMissiles.remove(i); continue;
            }
            if (r.owner != p2 && p2.alive && p2.getBounds().intersects(r.getBounds())) {
                if (p2.hit()) { score1++; checkWinAndReset(); return; }
                rcMissiles.remove(i);
            }
        }


     // --- Updated Laser Logic for GamePanel.java ---
        for (int i = lasers.size() - 1; i >= 0; i--) {
            Laser l = lasers.get(i);
            if (l.update(maze)) { // Returns true when the tracer finishes its path
                lasers.remove(i);
                l.owner.showAimGuide = false; // Turn off powerup after use
                continue;
            }

            if (l.hits(p1)) { p1.hit(); score2++; checkWinAndReset(); return; }
            if (l.hits(p2)) { p2.hit(); score1++; checkWinAndReset(); return; }
        }




        // Update frag bombs
        for (int i = bombs.size()-1; i >= 0; i--) {
            FragBomb fb = bombs.get(i);
            if (fb.update()) bombs.remove(i);
        }


        // Power-up spawn
        if (++powerUpSpawnTimer >= POWERUP_INTERVAL && powerUps.size() < 3) {
            powerUps.add(PowerUp.spawn(maze, p1, p2));
            powerUpSpawnTimer = 0;
        }


        // Power-up collection
        for (int i = powerUps.size()-1; i >= 0; i--) {
            PowerUp pu = powerUps.get(i);
            pu.update();
            if (pu.overlaps(p1)) { p1.applyPowerUp(pu.type); pu.collect(); powerUps.remove(i); }
            else if (pu.overlaps(p2)) { p2.applyPowerUp(pu.type); pu.collect(); powerUps.remove(i); }
        }


        repaint();
    }


    private void checkWinAndReset() {
        boolean p1Won = score1 >= targetScore;
        boolean p2Won = score2 >= targetScore;

        if (p1Won || p2Won) {
            String winner = p1Won ? "Green (P1)" : (mode == GameMode.ONE_PLAYER ? "Red (AI)" : "Red (P2)");
            int choice = JOptionPane.showOptionDialog(this,
                    winner + " wins the match!\n\nPlay again?",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Play Again", "Main Menu"},
                    "Play Again");

            score1 = 0;
            score2 = 0;

            if (choice == 1) {
                timer.stop();
                onReturnToMenu.run();
                return;
            }
            // If they chose "Play Again", reset immediately for the new match
            resetRound();
        } else {
            // --- COOLDOWN FOR NORMAL ROUNDS ---
            // 1. Stop the game timer so everything freezes for 2 seconds
            timer.stop();

            // 2. Start a 2-second delay
            Timer cooldown = new Timer(2000, e -> {
                resetRound();
                timer.start(); // Start the game back up
            });
            cooldown.setRepeats(false);
            cooldown.start();
        }
    }



    // ────────────────────────────────────────────
    //  Rendering
    // ────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // ── Maze ──
        maze.draw(g2);


        // ── Aim guide powerup ──
        /*
        if (p1.showAimGuide)
            Laser.drawAimGuide(g2, p1.x, p1.y, p1.angle, maze, p1.aimGuideStacks);
        if (p2.showAimGuide)
            Laser.drawAimGuide(g2, p2.x, p2.y, p2.angle, maze, p2.aimGuideStacks);
        */


        // ── Power-ups ──
        for (PowerUp pu : powerUps) pu.draw(g2);


        // ── Bombs ──
        for (FragBomb fb : bombs) fb.draw(g2);


        // ── Bullets / missiles ──
        for (Bullet b       : bullets)   b.draw(g2);
        for (HomingMissile m: missiles)  m.draw(g2);
        for (RCMissile r    : rcMissiles) r.draw(g2);


        // ── Lasers ──
        for (Laser l : lasers) l.draw(g2);


        // ── Tanks ──
        p1.draw(g2);
        p2.draw(g2);


        // ── HUD ──
        drawHUD(g2);


        g2.dispose();
    }


    private void drawHUD(Graphics2D g) {
        // Score bar
        g.setColor(new Color(20, 20, 30));
        g.fillRect(0, Maze.PX, 620, 700 - Maze.PX);


        g.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        // Green score
        String gStr = "Green - " + score1;
        g.setColor(Color.GREEN);
        g.drawString(gStr, 620/2 - fm.stringWidth(gStr) - 20, Maze.PX + 38);
        // Red score
        String rStr = "Red - " + score2;
        g.setColor(Color.RED);
        g.drawString(rStr, 620/2 + 20, Maze.PX + 38);


        // Controls reminder
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.setColor(new Color(160, 160, 160));
        g.drawString("P1: WASD+R  Pause=ENTER", 10, Maze.PX + 62);
        if (mode == GameMode.TWO_PLAYER) {
            g.drawString("P2: Arrows+SPACE  Pause=P", 360, Maze.PX + 62);
        } else {
            g.drawString("AI opponent", 400, Maze.PX + 62);
        }


        // Per-player HUD
        drawPlayerHUD(g, p1, 10,  Maze.PX + 80, "P1",  Color.GREEN);
        drawPlayerHUD(g, p2, 330, Maze.PX + 80,
                      (mode == GameMode.ONE_PLAYER) ? "AI" : "P2", Color.RED);
    }


    private void drawPlayerHUD(Graphics2D g, Tank t, int x, int y, String label, Color labelColor) {
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(labelColor);
        g.drawString(label + " Ammo:", x, y);
        g.setColor(t.ammo > 0 ? Color.YELLOW : Color.RED);
        g.drawString(String.valueOf(t.ammo), x + 80, y);


        int xOff = x + 105;
        if (t.shieldActive) {
            g.setColor(new Color(100, 200, 255));
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("[SHIELD]", xOff, y);
            xOff += 70;
        }
        if (t.showAimGuide) {
            g.setColor(new Color(255, 255, 80));
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("[AIM]", xOff, y);
        }
    }
}



