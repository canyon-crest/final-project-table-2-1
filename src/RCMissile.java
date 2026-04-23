import java.awt.*;
import java.awt.event.KeyEvent;


/**
 * RC Missile – player steers it with their turn keys after firing.
 */
public class RCMissile {


    public double x, y, angle, speed;
    public Tank owner;
    private int life = 500;


    // Which keys turn the missile (borrowed from owner's left/right bindings)
    private int leftKey, rightKey;
    private boolean turnLeft, turnRight;


    public RCMissile(double x, double y, double angle, Tank owner) {
        this.x = x; this.y = y; this.angle = angle;
        this.owner = owner;
        this.speed = 3.0;
        this.leftKey  = owner.leftKey;
        this.rightKey = owner.rightKey;
    }


    public void handleInput(int k, boolean pressed) {
        if (k == leftKey)  turnLeft  = pressed;
        if (k == rightKey) turnRight = pressed;
    }


    /** Returns true when missile should be removed. */
    public boolean update(Maze maze) {
        if (--life <= 0) return true;
        angle += (turnLeft  ? -0.06 : 0);
        angle += (turnRight ?  0.06 : 0);


        double nx = x + Math.cos(angle) * speed;
        double ny = y + Math.sin(angle) * speed;


        if (maze.circleHitsWall(nx, ny, 5)) return true;
        x = nx; y = ny;
        return false;
    }


    public void draw(Graphics2D g) {
        g.translate(x, y);
        g.rotate(angle);
        g.setColor(Color.MAGENTA);
        int[] xs = {8, -6, -6};
        int[] ys = {0, -4,  4};
        g.fillPolygon(xs, ys, 3);
        g.setColor(Color.PINK);
        g.drawPolygon(xs, ys, 3);
        g.setColor(new Color(255,200,0,160));
        g.fillOval(-10, -2, 6, 4);
        g.rotate(-angle);
        g.translate(-x, -y);
    }


    public Rectangle getBounds() {
        return new Rectangle((int)x - 6, (int)y - 6, 12, 12);
    }
}



