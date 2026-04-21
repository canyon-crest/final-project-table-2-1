import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TankTrouble extends JPanel implements ActionListener {
    private javax.swing.Timer timer;
    private Tank p1, p2;
    private int score1 = 0, score2 = 0;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private final int GRID_SIZE = 10, CELL_SIZE = 60;
    private boolean[][] vWalls, hWalls;

    public TankTrouble() {
        setFocusable(true);
        resetMap();
        timer = new javax.swing.Timer(16, this);
        timer.start();
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { p1.handleInput(e.getKeyCode(), true); p2.handleInput(e.getKeyCode(), true); }
            public void keyReleased(KeyEvent e) { p1.handleInput(e.getKeyCode(), false); p2.handleInput(e.getKeyCode(), false); }
        });
    }

    private void resetMap() {
        bullets.clear();
        generateConnectedMaze();
        p1 = new Tank(30, 30, Color.GREEN, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_R);
        p2 = new Tank(570, 570, Color.RED, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE);
    }

    private void generateConnectedMaze() {
        vWalls = new boolean[GRID_SIZE + 1][GRID_SIZE];
        hWalls = new boolean[GRID_SIZE][GRID_SIZE + 1];
        for (int i = 0; i <= GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) { vWalls[i][j] = true; hWalls[j][i] = true; }
        }
        Stack<Point> stack = new Stack<>();
        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        Point curr = new Point(0, 0);
        visited[0][0] = true;
        int visitedCount = 1;
        while (visitedCount < GRID_SIZE * GRID_SIZE) {
            ArrayList<Point> neighbors = new ArrayList<>();
            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : dirs) {
                int nx = curr.x + d[0], ny = curr.y + d[1];
                if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && !visited[nx][ny]) neighbors.add(new Point(nx, ny));
            }
            if (!neighbors.isEmpty()) {
                Point next = neighbors.get(new Random().nextInt(neighbors.size()));
                removeWall(curr, next);
                stack.push(curr);
                curr = next;
                visited[curr.x][curr.y] = true;
                visitedCount++;
            } else if (!stack.isEmpty()) curr = stack.pop();
        }
        Random r = new Random();
        for (int i = 1; i < GRID_SIZE; i++) {
            for (int j = 1; j < GRID_SIZE; j++) { if (r.nextDouble() < 0.2) vWalls[i][j] = false; }
        }
    }

    private void removeWall(Point a, Point b) {
        if (a.x == b.x) hWalls[a.x][Math.max(a.y, b.y)] = false;
        else vWalls[Math.max(a.x, b.x)][a.y] = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw Maze
        g2.setStroke(new BasicStroke(4));
        g2.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (vWalls[i][j]) g2.drawLine(i * CELL_SIZE, j * CELL_SIZE, i * CELL_SIZE, (j + 1) * CELL_SIZE);
                if (hWalls[j][i]) g2.drawLine(j * CELL_SIZE, i * CELL_SIZE, (j + 1) * CELL_SIZE, i * CELL_SIZE);
            }
        }
        
        p1.draw(g2); p2.draw(g2);
        for (Bullet b : bullets) b.draw(g2);

        // Scoreboard
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Green: " + score1 + "  |  Red: " + score2, 220, 630);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        p1.update(vWalls, hWalls, bullets);
        p2.update(vWalls, hWalls, bullets);
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (b.update(vWalls, hWalls)) { bullets.remove(i); continue; }

            boolean p1Hit = p1.getBounds().contains(b.x, b.y);
            boolean p2Hit = p2.getBounds().contains(b.x, b.y);

            if (p1Hit || p2Hit) {
                if (p1Hit) score2++;
                else score1++;
                
                if (score1 >= 5 || score2 >= 5) {
                    String winner = score1 >= 5 ? "Green" : "Red";
                    JOptionPane.showMessageDialog(this, winner + " Wins the Match!");
                    score1 = 0; score2 = 0;
                }
                resetMap();
                break;
            }
        }
        repaint();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Tank Trouble");
        f.add(new TankTrouble());
        f.setSize(615, 680); // Adjusted for scoreboard
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}

class Tank {
    double x, y, angle = 0;
    Color color;
    int up, down, left, right, fireKey;
    boolean uP, dP, lP, rP, fireReady;
    long lastFire = 0;
    final int SIZE = 26;

    public Tank(int x, int y, Color c, int u, int d, int l, int r, int f) {
        this.x = x; this.y = y; this.color = c;
        this.up = u; this.down = d; this.left = l; this.right = r; this.fireKey = f;
    }

    public Rectangle getBounds() { return new Rectangle((int) x - SIZE / 2, (int) y - SIZE / 2, SIZE, SIZE); }

    public void handleInput(int k, boolean pressed) {
        if (k == up) uP = pressed; if (k == down) dP = pressed;
        if (k == left) lP = pressed; if (k == right) rP = pressed;
        if (k == fireKey && pressed) fireReady = true;
    }

    public void update(boolean[][] v, boolean[][] h, ArrayList<Bullet> bList) {
        double speed = (uP ? 2.2 : 0) + (dP ? -2.2 : 0);
        angle += (lP ? -0.08 : 0) + (rP ? 0.08 : 0);
        double nx = x + Math.cos(angle) * speed, ny = y + Math.sin(angle) * speed;
        if (!hitsWall(nx, ny, v, h, 16)) { x = nx; y = ny; }
        if (fireReady && System.currentTimeMillis() - lastFire > 400) {
            // Bullet speed increased by using 5.5 magnitude
            bList.add(new Bullet(x + Math.cos(angle) * 25, y + Math.sin(angle) * 25, angle));
            lastFire = System.currentTimeMillis();
            fireReady = false;
        }
    }

    private boolean hitsWall(double nx, double ny, boolean[][] v, boolean[][] h, int b) {
        int gx = (int) nx / 60, gy = (int) ny / 60;
        if (nx < b || nx > 600 - b || ny < b || ny > 600 - b) return true;
        if (v[gx][gy] && nx % 60 < b) return true;
        if (v[gx + 1][gy] && nx % 60 > 60 - b) return true;
        if (h[gx][gy] && ny % 60 < b) return true;
        if (h[gx][gy + 1] && ny % 60 > 60 - b) return true;
        return false;
    }

    public void draw(Graphics2D g) {
        g.translate(x, y); g.rotate(angle);
        g.setColor(color); g.fillRect(-12, -10, 24, 20);
        g.setColor(Color.BLACK); g.fillRect(0, -2, 18, 4);
        g.rotate(-angle); g.translate(-x, -y);
    }
}

class Bullet {
    double x, y, vx, vy;
    int life = 300;

    public Bullet(double x, double y, double a) {
        this.x = x; this.y = y;
        this.vx = Math.cos(a) * 5.5; // Faster velocity
        this.vy = Math.sin(a) * 5.5;
    }

    public boolean update(boolean[][] v, boolean[][] h) {
        double nx = x + vx, ny = y + vy;
        int gx = (int) x / 60, gy = (int) y / 60;
        if (gx >= 0 && gx < 10 && gy >= 0 && gy < 10) {
            if ((vx < 0 && v[gx][gy] && nx % 60 < 4) || (vx > 0 && v[gx + 1][gy] && nx % 60 > 56)) { vx *= -1; nx = x; }
            if ((vy < 0 && h[gx][gy] && ny % 60 < 4) || (vy > 0 && h[gx][gy + 1] && ny % 60 > 56)) { vy *= -1; ny = y; }
        }
        x += vx; y += vy;
        return --life <= 0;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillOval((int) x - 3, (int) y - 3, 7, 7);
    }
}
