import java.awt.*;

// this is the base class for all the things that get shot (bullets lasers etc)
public abstract class Projectile implements Drawable {

    // x and y is where the projectile is on the screen, angle is wich direction it goes
    public double x, y, angle, speed;
    public Tank owner; // the tank that shoot this
    protected int life; // how many bounces or frames it has left before it disapears

    // sets up all the stuff when a new projectile is made
    public Projectile(double x, double y, double angle, double speed, Tank owner, int life) {
        this.x     = x;
        this.y     = y;
        this.angle = angle;
        this.speed = speed;
        this.owner = owner; // remember who fired it so it doesnt hit itself 
        this.life  = life;
    }

    // every projectile has to have its own update, like bullets bounce different then lasers
    public abstract boolean update(Maze maze);

    // each type draws itself different so this is abstract to
    public abstract void draw(Graphics2D g);

    // returns a box around the projectile for colision checking
    public Rectangle getBounds() {
        // the box is 12x12 and centered on the projectile
        return new Rectangle((int)x - 6, (int)y - 6, 12, 12);
    }
}
