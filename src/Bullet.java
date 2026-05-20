import java.awt.*;

public class Bullet extends Projectile {

    private static final int MAX_BOUNCES = 5;
    private int bounces = 0;

    public Bullet(double x, double y, double angle, Tank owner) {
        super(x, y, angle, 5.5, owner, 3100);
    }

    @Override
    public boolean update(Maze maze) {
        if (--life <= 0) return true;

        final int STEPS = 8;
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