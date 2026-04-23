import java.awt.*;
import java.util.*;


/**
 * Homing missile – steers toward nearest enemy tank.
 * Destroyed on wall contact (no bounce).
 */
public class HomingMissile {


    public double x, y, angle, speed;
    public Tank owner;
    private int life = 400;
    private Tank target;


    public HomingMissile(double x, double y, double angle, Tank owner, Tank target) {
        this.x = x; this.y = y; this.angle = angle;
        this.owner  = owner;
        this.target = target;
        this.speed  = 3.5;
    }


    /** Returns true when missile should be removed. */
    public boolean update(Maze maze) {
        if (--life <= 0) return true;


        // Steer toward target
        if (target != null && target.alive) {
            double dx = target.x - x;
            double dy = target.y - y;
            double desired = Math.atan2(dy, dx);
            double diff = desired - angle;
            // Normalise to [-pi, pi]
            while (diff >  Math.PI) diff -= 2 * Math.PI;
            while (diff < -Math.PI) diff += 2 * Math.PI;
            double turn = Math.min(Math.abs(diff), 0.06) * Math.signum(diff);
            angle += turn;
        }


        double nx = x + Math.cos(angle) * speed;
        double ny = y + Math.sin(angle) * speed;


        // Destroy on wall contact
        if (maze.circleHitsWall(nx, ny, 5)) return true;
        x = nx; y = ny;
        return false;
    }


    public void draw(Graphics2D g) {
        g.translate(x, y);
        g.rotate(angle);
        g.setColor(Color.CYAN);
        int[] xs = {8, -6, -6};
        int[] ys = {0, -4,  4};
        g.fillPolygon(xs, ys, 3);
        g.setColor(new Color(0,200,200));
        g.drawPolygon(xs, ys, 3);
        // exhaust trail
        g.setColor(new Color(255,140,0,160));
        g.fillOval(-10, -2, 6, 4);
        g.rotate(-angle);
        g.translate(-x, -y);
    }


    public Rectangle getBounds() {
        return new Rectangle((int)x - 6, (int)y - 6, 12, 12);
    }
}



