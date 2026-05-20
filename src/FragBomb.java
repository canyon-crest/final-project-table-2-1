import java.awt.*;
import java.util.List;

public class FragBomb implements Drawable {

    public double x, y;
    public Tank owner;
    private int blinkTimer    = 0;
    private boolean exploded  = false;
    private int explosionLife = 0;

    public FragBomb(double x, double y, Tank owner) {
        this.x = x; this.y = y; this.owner = owner;
    }

    public boolean isExploded() { return exploded; }

    public void detonate(List<Bullet> bullets) {
        if (exploded) return;
        exploded = true;
        explosionLife = 20;
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            bullets.add(new Bullet(x, y, angle, owner));
        }
    }

    public boolean update() {
        blinkTimer++;
        if (explosionLife > 0) explosionLife--;
        return exploded && explosionLife <= 0;
    }

    @Override
    public void draw(Graphics2D g) {
        if (exploded) {
            int r = (20 - explosionLife) * 4;
            g.setColor(new Color(255, 120, 0, Math.max(0, explosionLife * 12)));
            g.fillOval((int)x - r, (int)y - r, r * 2, r * 2);
            return;
        }

        Color c = (blinkTimer / 8) % 2 == 0 ? Color.ORANGE : Color.RED;
        g.setColor(c);
        g.fillOval((int)x - 7, (int)y - 7, 14, 14);
        g.setColor(Color.BLACK);
        g.drawOval((int)x - 7, (int)y - 7, 14, 14);
        g.drawString("!", (int)x - 3, (int)y + 4);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x - 7, (int)y - 7, 14, 14);
    }
}