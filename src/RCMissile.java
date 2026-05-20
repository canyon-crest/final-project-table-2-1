import java.awt.*;

public class RCMissile extends Projectile {

    private int leftKey, rightKey;
    private boolean turnLeft, turnRight;

    public RCMissile(double x, double y, double angle, Tank owner) {
        super(x, y, angle, 3.0, owner, 500);
        this.leftKey  = owner.leftKey;
        this.rightKey = owner.rightKey;
    }

    public void handleInput(int k, boolean pressed) {
        if (k == leftKey)  turnLeft  = pressed;
        if (k == rightKey) turnRight = pressed;
    }

    @Override
    public boolean update(Maze maze) {
        if (--life <= 0) return true;

        if (turnLeft)  angle -= 0.06;
        if (turnRight) angle += 0.06;

        double nx = x + Math.cos(angle) * speed;
        double ny = y + Math.sin(angle) * speed;
        if (maze.circleHitsWall(nx, ny, 5)) return true;
        x = nx; y = ny;
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        g.translate(x, y);
        g.rotate(angle);

        g.setColor(Color.MAGENTA);
        int[] xs = {8, -6, -6};
        int[] ys = {0, -4,  4};
        g.fillPolygon(xs, ys, 3);
        g.setColor(Color.PINK);
        g.drawPolygon(xs, ys, 3);

        g.setColor(new Color(255, 200, 0, 160));
        g.fillOval(-10, -2, 6, 4);

        g.rotate(-angle);
        g.translate(-x, -y);
    }
}