import java.awt.*;

// bullet class, it extends projectile so it gets all the basic stuff for free
public class Bullet extends Projectile {

    private static final int MAX_BOUNCES = 5; // after 5 bounces the bullet just disapears
    private int bounces = 0;

    public Bullet(double x, double y, double angle, Tank owner) {
        super(x, y, angle, 5.5, owner, 3100);
    }

    // moves the bullet and handles bouncing, returns true when bullet should be removed
    @Override
    public boolean update(Maze maze) {
        if (--life <= 0) return true;

        final int STEPS = 8; // splits movement into 8 substeps for more acurate colision
        for (int s = 0; s < STEPS; s++) {
            double vx = speed * Math.cos(angle);
            double vy = speed * Math.sin(angle);
            double nx = x + vx / STEPS;
            double ny = y + vy / STEPS;

            char bounce = maze.bulletBounce(x, y, nx, ny, vx, vy);
            if (bounce == 'x' || bounce == 'b') angle = Math.PI - angle;
            if (bounce == 'y' || bounce == 'b') angle = -angle;

            if (bounce != 0) {
                bounces++;
                if (bounces > MAX_BOUNCES) return true;
            } else {
                x = nx;
                y = ny;
            }
        }
        return false;
    }

    // just draws a tiny yellow circle where the bullet is
    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)x - 2, (int)y - 2, 4, 4);
        g.setColor(Color.ORANGE);
        g.drawOval((int)x - 2, (int)y - 2, 4, 4);
    }

    public boolean hasBounced() { return bounces > 0; }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x - 2, (int)y - 2, 4, 4);
    }
}
