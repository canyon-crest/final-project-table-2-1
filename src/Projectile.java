import java.awt.*;

public abstract class Projectile implements Drawable {

    public double x, y, angle, speed;
    public Tank owner;
    protected int life;

    public Projectile(double x, double y, double angle, double speed, Tank owner, int life) {
        this.x     = x;
        this.y     = y;
        this.angle = angle;
        this.speed = speed;
        this.owner = owner;
        this.life  = life;
    }

    public abstract boolean update(Maze maze);

    public abstract void draw(Graphics2D g);

    public Rectangle getBounds() {
        return new Rectangle((int)x - 6, (int)y - 6, 12, 12);
    }
}