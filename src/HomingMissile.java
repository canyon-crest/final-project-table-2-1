import java.awt.*;

public class HomingMissile extends Projectile {

    private Tank target;

    public HomingMissile(double x, double y, double angle, Tank owner, Tank target) {
        super(x, y, angle, 3.5, owner, 400);
        this.target = target;
    }

    @Override
    public boolean update(Maze maze) {
        if (--life <= 0) return true;

        if (target != null && target.alive) {
            double dx = target.x - x;
            double dy = target.y - y;
            double desired = Math.atan2(dy, dx);
            double diff = desired - angle;
            while (diff >  Math.PI) diff -= 2 * Math.PI;
            while (diff < -Math.PI) diff += 2 * Math.PI;
            angle += Math.min(Math.abs(diff), 0.06) * Math.signum(diff);
        }

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

        g.setColor(Color.CYAN);
        int[] xs = {8, -6, -6};
        int[] ys = {0, -4,  4};
        g.fillPolygon(xs, ys, 3);
        g.setColor(new Color(0, 200, 200));
        g.drawPolygon(xs, ys, 3);

        g.setColor(new Color(255, 140, 0, 160));
        g.fillOval(-10, -2, 6, 4);

        g.rotate(-angle);
        g.translate(-x, -y);
    }
}