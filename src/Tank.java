import java.awt.*;
import java.util.*;


/**
 * Base Tank class shared by PlayerTank and AITank.
 *
 * Holds position, rotation, health, active power-up, and drawing logic.
 */
public abstract class Tank {


    // ── Position & motion ──
    public double x, y;
    public double angle = 0;
    public boolean alive = true;


    // ── Appearance ──
    public Color color;


    // ── Dimensions ──
    public static final int BODY_HALF   = 10;   // half body width / height (~3/4 original)
    public static final int COLLISION_R = 11;   // circle radius for wall checks
    public static final int CANNON_LEN  = 14;


    // ── Control keys (used by PlayerTank and RCMissile) ──
    public int upKey, downKey, leftKey, rightKey, fireKey;


    // ── Firing ──
    protected long lastFire = 0;
    protected static final long FIRE_COOLDOWN = 400;  // ms


    // ── Ammo ──
    public int ammo = 10;


    // ── Shield ──
    public boolean shieldActive = false;
    private int shieldTimer = 0;
    private static final int SHIELD_DURATION = 300; // frames


    // ── Aim guide powerup ──
    public boolean showAimGuide = false;
    public int aimGuideStacks  = 0;  // cumulative X-crate pickups this round
    private int aimGuideTimer  = 0;
    private static final int AIM_GUIDE_DURATION = 600; // frames (~10 s)


    public Tank(double x, double y, Color color,
                int up, int down, int left, int right, int fire) {
        this.x = x; this.y = y; this.color = color;
        this.upKey = up; this.downKey = down;
        this.leftKey = left; this.rightKey = right;
        this.fireKey = fire;
    }


    // ── Bounds ──
    public Rectangle getBounds() {
        return new Rectangle((int)x - BODY_HALF, (int)y - BODY_HALF,
                             BODY_HALF * 2, BODY_HALF * 2);
    }


    // ── Power-up activation ──
    public void applyPowerUp(PowerUp.Type type) {
        if (type == PowerUp.Type.SHIELD) {
            shieldActive = true;
            shieldTimer  = SHIELD_DURATION;
        } else if (type == PowerUp.Type.AMMO) {
            ammo += 5 + new Random().nextInt(6); // gives 5-10 extra ammo
        } else if (type == PowerUp.Type.AIM_GUIDE) {
            aimGuideStacks++; // each pickup stacks – more bounces shown
            showAimGuide  = true;
            aimGuideTimer = AIM_GUIDE_DURATION;
        }
    }


    // ── Shield tick ──
    protected void tickShield() {
        if (shieldActive) {
            if (--shieldTimer <= 0) shieldActive = false;
        }
    }


    // ── Aim guide tick ──
    protected void tickAimGuide() {
        if (showAimGuide) {
            if (--aimGuideTimer <= 0) showAimGuide = false;
        }
    }


    /** Called when this tank is hit (by a bullet/missile/laser). Returns true if tank dies. */
    /** Called when this tank is hit. Returns true if tank dies. */
    public boolean hit() {
        if (shieldActive) {
            shieldActive = false; // THE FIX: Shield breaks after one hit
            return false;        // Tank survives this hit
        }
        alive = false;           // No shield? Tank dies
        return true;
    }



    // ── Abstract update ──
    public abstract void update(Maze maze,
                                java.util.List<Bullet>       bullets,
                                java.util.List<HomingMissile> missiles,
                                java.util.List<RCMissile>     rcMissiles,
                                java.util.List<Laser>         lasers,
                                java.util.List<FragBomb>      bombs,
                                Tank enemy);


    // ── Movement helper ──
    protected void move(double speed, Maze maze) {
        double nx = x + Math.cos(angle) * speed;
        double ny = y + Math.sin(angle) * speed;
        if (!maze.circleHitsWall(nx, ny, COLLISION_R)) {
            x = nx; y = ny;
        } else {
            // Try sliding on X only
            nx = x + Math.cos(angle) * speed;
            if (!maze.circleHitsWall(nx, y, COLLISION_R)) x = nx;
            // Try sliding on Y only
            ny = y + Math.sin(angle) * speed;
            if (!maze.circleHitsWall(x, ny, COLLISION_R)) y = ny;
        }
    }


    // ── Fire helpers ──
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


    // ── Drawing ──
    public void draw(Graphics2D g) {
        if (!alive) return;


     // Inside Tank.draw()
        if (shieldActive) {
            g.setColor(new Color(100, 200, 255, 150)); // Solid blue
            g.setStroke(new BasicStroke(3));
            g.drawOval((int)x - 20, (int)y - 20, 40, 40);
        }



        // Tank body
        g.translate(x, y);
        g.rotate(angle);


        // Tracks
        g.setColor(color.darker());
        g.fillRect(-BODY_HALF - 2, -BODY_HALF,     5, BODY_HALF * 2);
        g.fillRect( BODY_HALF - 3, -BODY_HALF,     5, BODY_HALF * 2);


        // Hull
        g.setColor(color);
        g.fillRoundRect(-BODY_HALF + 3, -BODY_HALF, (BODY_HALF - 3) * 2, BODY_HALF * 2, 6, 6);


        // Turret
        g.setColor(color.brighter());
        g.fillOval(-5, -5, 10, 10);


        // Cannon
        g.setColor(color.darker().darker());
        g.fillRect(0, -2, CANNON_LEN, 4);


        g.rotate(-angle);
        g.translate(-x, -y);
    }
}



