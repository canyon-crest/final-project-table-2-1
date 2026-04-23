import java.awt.*;
import java.util.*;


/**
 * PowerUp crate that appears randomly in the maze.
 *
 * Types: SHIELD (passive protection), AMMO (instant +5-10 bullets)
 */
public class PowerUp {


    public enum Type { SHIELD, AMMO, AIM_GUIDE }


    public double x, y;
    public Type type;
    private int bobTimer = 0;
    private boolean collected = false;


    private static final int SIZE = 20;
    private static final Random RNG = new Random();


    public PowerUp(double x, double y) {
        this.x    = x;
        this.y    = y;
        // 50% SHIELD, 30% AIM_GUIDE, 20% AMMO
        int r = RNG.nextInt(10);
        this.type = (r < 5) ? Type.SHIELD : (r < 8) ? Type.AIM_GUIDE : Type.AMMO;
    }


    public boolean isCollected() { return collected; }


    public boolean update() {
        bobTimer++;
        return collected;
    }


    /** Call when a tank's bounds overlap this power-up. */
    public void collect() { collected = true; }


    public boolean overlaps(Tank t) {
        Rectangle r = t.getBounds();
        return r.intersects(new Rectangle((int)x - SIZE/2, (int)y - SIZE/2, SIZE, SIZE));
    }


    public void draw(Graphics2D g) {
        double bob = Math.sin(bobTimer * 0.08) * 3;
        int cx = (int)x, cy = (int)(y + bob);


        // Crate box
        g.setColor(new Color(180, 130, 50));
        g.fillRoundRect(cx - SIZE/2, cy - SIZE/2, SIZE, SIZE, 6, 6);
        g.setColor(new Color(220, 180, 80));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(cx - SIZE/2, cy - SIZE/2, SIZE, SIZE, 6, 6);


        // Icon
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        String icon = iconFor(type);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(icon, cx - fm.stringWidth(icon)/2, cy + fm.getAscent()/2 - 1);
    }


    private static String iconFor(Type t) {
        switch (t) {
            case SHIELD:     return "S";
            case AMMO:       return "A";
            case AIM_GUIDE:  return "X";
            default:         return "?";
        }
    }


    /**
     * Spawn a random power-up at a grid-cell centre that isn't too close to either tank.
     */
    public static PowerUp spawn(Maze maze, Tank t1, Tank t2) {
        Random rng = new Random();
        int attempts = 0;
        while (attempts++ < 100) {
            int col = rng.nextInt(Maze.GRID);
            int row = rng.nextInt(Maze.GRID);
            double px = col * Maze.CELL + Maze.CELL / 2.0;
            double py = row * Maze.CELL + Maze.CELL / 2.0;
            double d1 = Math.hypot(px - t1.x, py - t1.y);
            double d2 = Math.hypot(px - t2.x, py - t2.y);
            if (d1 > 90 && d2 > 90)
                return new PowerUp(px, py);
        }
        // Fallback: center
        return new PowerUp(Maze.PX / 2.0, Maze.PX / 2.0);
    }
}



    