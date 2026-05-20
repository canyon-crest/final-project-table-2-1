import java.awt.*;
import java.util.Random;

public abstract class Tank implements Drawable {

    public double x, y;
    public double angle = 0;
    public boolean alive = true;
    public Color color;

    public static final int BODY_HALF   = 10;
    public static final int COLLISION_R = 11;
    public static final int CANNON_LEN  = 14;

    public int upKey, downKey, leftKey, rightKey, fireKey;

    protected long lastFire = 0;
    protected static final long FIRE_COOLDOWN = 400;

    public int ammo = 10;

    public boolean shieldActive = false;
    private int shieldTimer = 0;
    private static final int SHIELD_DURATION = 300;

    public boolean showAimGuide = false;
    public int aimGuideStacks  = 0;
    private int aimGuideTimer  = 0;
    private static final int AIM_GUIDE_DURATION = 600;

    public Tank(double x, double y, Color color,
                int up, int down, int left, int right, int fire) {
        this.x = x; this.y = y; this.color = color;
        this.upKey    = up;    this.downKey  = down;
        this.leftKey  = left;  this.rightKey = right;
        this.fireKey  = fire;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x - BODY_HALF, (int)y - BODY_HALF,
                             BODY_HALF * 2, BODY_HALF * 2);
    }

    public void applyPowerUp(PowerUp.Type type) {
        if (type == PowerUp.Type.SHIELD) {
            shieldActive = true;
            shieldTimer  = SHIELD_DURATION;
        } else if (type == PowerUp.Type.AMMO) {
            ammo += 5 + new Random().nextInt(6);
        } else if (type == PowerUp.Type.AIM_GUIDE) {
            aimGuideStacks++;
            showAimGuide  = true;
            aimGuideTimer = AIM_GUIDE_DURATION;
        }
    }

    protected void tickShield() {
        if (shieldActive && --shieldTimer <= 0) shieldActive = false;
    }

    protected void tickAimGuide() {
        if (showAimGuide && --aimGuideTimer <= 0) showAimGuide = false;
    }

    public boolean hit() {
        if (shieldActive) {
            shieldActive = false;
            return false;
        }
        alive = false;
        return true;
    }

    public abstract void update(Maze maze,
                                java.util.List<Bullet>        bullets,
                                java.util.List<HomingMissile>  missiles,
                                java.util.List<RCMissile>      rcMissiles,
                                java.util.List<Laser>          lasers,
                                java.util.List<FragBomb>       bombs,
                                Tank enemy);

    protected void move(double speed, Maze maze) {
        double nx = x + Math.cos(angle) * speed;
        double ny = y + Math.sin(angle) * speed;
        if (!maze.circleHitsWall(nx, ny, COLLISION_R)) {
            x = nx; y = ny;
        } else {
            if (!maze.circleHitsWall(nx, y, COLLISION_R)) x = nx;
            if (!maze.circleHitsWall(x, ny, COLLISION_R)) y = ny;
        }
    }

    protected boolean canFire() {
        return System.currentTimeMillis() - lastFire > FIRE_COOLDOWN;
    }

    protected void fireStandardBullet(java.util.List<Bullet> bullets) {
        if (ammo <= 0) return;
        double bx = x + Math.cos(angle) * (CANNON_LEN + BODY_HALF);
        double by = y + Math.sin(angle) * (CANNON_LEN + BODY_HALF);
        bullets.add(new Bullet(bx, by, angle, this));
        lastFire = System.currentTimeMillis();
        ammo--;
    }

    @Override
    public void draw(Graphics2D g) {
        if (!alive) return;

        if (shieldActive) {
            g.setColor(new Color(100, 200, 255, 150));
            g.setStroke(new BasicStroke(3));
            g.drawOval((int)x - 20, (int)y - 20, 40, 40);
        }

        g.translate(x, y);
        g.rotate(angle);

        g.setColor(color.darker());
        g.fillRect(-BODY_HALF - 2, -BODY_HALF, 5, BODY_HALF * 2);
        g.fillRect( BODY_HALF - 3, -BODY_HALF, 5, BODY_HALF * 2);

        g.setColor(color);
        g.fillRoundRect(-BODY_HALF + 3, -BODY_HALF, (BODY_HALF - 3) * 2, BODY_HALF * 2, 6, 6);

        g.setColor(color.brighter());
        g.fillOval(-5, -5, 10, 10);

        g.setColor(color.darker().darker());
        g.fillRect(0, -2, CANNON_LEN, 4);

        g.rotate(-angle);
        g.translate(-x, -y);
    }
}