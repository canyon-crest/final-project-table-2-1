import java.awt.*;
import java.util.List;


/**
 * Standard bouncing bullet fired by tanks.
 */
public class Bullet {


    public double x, y, vx, vy;
    public int life;
    public Tank owner;           // who fired it (to avoid instant self-hit)
    private static final int   MAX_BOUNCES = 5;
    private int bounces = 0;


    public Bullet(double x, double y, double angle, Tank owner) {
        this.x  = x;  this.y  = y;
        this.vx = Math.cos(angle) * 5.5;
        this.vy = Math.sin(angle) * 5.5;
        this.owner = owner;
        this.life  = 3100;
    }


    /**
     * Move bullet; bounce off walls.
     * Uses 8 substeps (~0.69 px each) so the bullet can never skip through
     * a wall or corner in a single frame.
     * On a bounce the position is NOT advanced that substep – only the
     * velocity is reversed, so the bullet always stays on the correct side.
     * @return true when bullet should be removed.
     */
    public boolean update(Maze maze) {
        if (--life <= 0) return true;


        final int STEPS = 8;
        for (int s = 0; s < STEPS; s++) {
            double nx = x + vx / STEPS;
            double ny = y + vy / STEPS;


            char bounce = maze.bulletBounce(x, y, nx, ny, vx, vy);
            if (bounce == 'x' || bounce == 'b') vx = -vx;
            if (bounce == 'y' || bounce == 'b') vy = -vy;
            if (bounce != 0) {
                bounces++;
                if (bounces > MAX_BOUNCES) return true;
                // Don't advance position; reversed velocity carries next substep away
            } else {
                x = nx;
                y = ny;
            }
        }
        return false;
    }


    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)x - 2, (int)y - 2, 4, 4);
        g.setColor(Color.ORANGE);
        g.drawOval((int)x - 2, (int)y - 2, 4, 4);
    }


    /** True once this bullet has bounced off at least one wall (safe to hit owner then). */
    public boolean hasBounced() { return bounces > 0; }


    public Rectangle getBounds() {
        return new Rectangle((int)x - 2, (int)y - 2, 4, 4);
    }
}



