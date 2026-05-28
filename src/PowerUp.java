import java.awt.*;
import java.util.Random;

// powerup crates that spawn in the maze and do different things when you pick em up
public class PowerUp implements Drawable {

	public static final int SHIELD    = 0;
	public static final int AMMO      = 1;
	public static final int AIM_GUIDE = 2;
	public int type; // wich powerup this one is

    public double x, y;
    private int bobTimer = 0;
    private boolean collected = false;
    private static final int SIZE = 20;
    private static final Random RNG = new Random();

    public PowerUp(double x, double y) {
        this.x = x; this.y = y;
        int r = RNG.nextInt(10);
        this.type = (r < 5) ? SHIELD : (r < 8) ? AIM_GUIDE : AMMO;
    }

    public boolean isCollected() { return collected; }

    // bobs the powerup up and down with a sine wave so its easier to notice
    public boolean update() {
        bobTimer++;
        return collected;
    }

    public void collect() { collected = true; }

    public boolean overlaps(Tank t) {
        Rectangle r = t.getBounds();
        return r.intersects(new Rectangle((int)x - SIZE/2, (int)y - SIZE/2, SIZE, SIZE));
    }

    @Override
    public void draw(Graphics2D g) {
        double bob = Math.sin(bobTimer * 0.08) * 3;
        int cx = (int)x, cy = (int)(y + bob);

        g.setColor(new Color(180, 130, 50));
        g.fillRoundRect(cx - SIZE/2, cy - SIZE/2, SIZE, SIZE, 6, 6);
        g.setColor(new Color(220, 180, 80));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(cx - SIZE/2, cy - SIZE/2, SIZE, SIZE, 6, 6);

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        String icon = iconFor(type);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(icon, cx - fm.stringWidth(icon) / 2, cy + fm.getAscent() / 2 - 1);
    }

    private static String iconFor(int t) {
    	if (t == SHIELD)    return "S";
        if (t == AMMO)      return "A";
        if (t == AIM_GUIDE) return "X";
        return "?";
    }

    // tries to spawn the powerup somewhere not to close to either tank
    public static PowerUp spawn(Maze maze, Tank t1, Tank t2) {
        Random rng = new Random();
        for (int attempt = 0; attempt < 100; attempt++) {
            int col = rng.nextInt(Maze.GRID);
            int row = rng.nextInt(Maze.GRID);
            double px = col * Maze.CELL + Maze.CELL / 2.0;
            double py = row * Maze.CELL + Maze.CELL / 2.0;
            double d1 = Math.hypot(px - t1.x, py - t1.y);
            double d2 = Math.hypot(px - t2.x, py - t2.y);
            if (d1 > 90 && d2 > 90) return new PowerUp(px, py);
        }
        return new PowerUp(Maze.PX / 2.0, Maze.PX / 2.0);
    }
}
