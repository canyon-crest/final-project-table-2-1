import java.awt.*;
import java.util.*;

public class Laser {
    public Tank owner;
    private java.util.List<double[]> segments = new ArrayList<>();
    private static final int MAX_BOUNCES = 150;
    
    public boolean isFiring = false;
    private long fireStartTime = 0;
    private double tracerProgress = 0; 
    private boolean finished = false;

    public Laser(Tank owner) {
        this.owner = owner;
    }

    public void startFire() {
        if (!isFiring) {
            isFiring = true;
            fireStartTime = System.currentTimeMillis();
        }
    }

    public boolean update(Maze maze) {
        double lx = owner.x + Math.cos(owner.angle) * (owner.CANNON_LEN + owner.BODY_HALF);
        double ly = owner.y + Math.sin(owner.angle) * (owner.CANNON_LEN + owner.BODY_HALF);
        calculatePath(lx, ly, owner.angle, maze);

        if (isFiring) {
            long elapsed = System.currentTimeMillis() - fireStartTime;
            if (elapsed > 1000) { 
                tracerProgress += 0.1; // Lightning fast tracer
                if (tracerProgress >= 1.2) finished = true; 
            }
        }
        return finished;
    }

    public void draw(Graphics2D g) {
        long elapsed = isFiring ? System.currentTimeMillis() - fireStartTime : 0;
        
        // 1. Calculate the Charge Color (Yellow -> Deep Red)
        Color currentLineColor;
        if (!isFiring) {
            currentLineColor = Color.YELLOW;
        } else if (elapsed < 1000) {
            float ratio = (float) elapsed / 1000f;
            int r = 255;
            int gVal = (int) (255 * (1 - ratio)); // Green fades out
            currentLineColor = new Color(r, gVal, 0);
        } else {
            currentLineColor = Color.RED;
        }

        for (int i = 0; i < segments.size(); i++) {
            double[] s = segments.get(i);
            double segIndexRatio = (double)i / segments.size();

            // 2. Draw the segments
            if (isFiring && elapsed > 1000 && segIndexRatio <= tracerProgress) {
                g.setColor(Color.RED);
                g.setStroke(new BasicStroke(5f)); // Solid lethal part
            } else {
                g.setColor(currentLineColor);
                float[] dash = {10.0f, 10.0f};
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0));
            }
            g.drawLine((int)s[0], (int)s[1], (int)s[2], (int)s[3]);
        }
    }

    private void calculatePath(double ox, double oy, double angle, Maze maze) {
        segments.clear();
        double dx = Math.cos(angle), dy = Math.sin(angle);
        double curX = ox, curY = oy;
        int bounces = 0;
        for (int i = 0; i < 2000 && bounces < MAX_BOUNCES; i++) {
            double nx = curX + dx, ny = curY + dy;
            char b = maze.bulletBounce(curX, curY, nx, ny, dx, dy);
            if (b != 0) {
                segments.add(new double[]{ox, oy, nx, ny});
                if (b == 'x' || b == 'b') dx = -dx;
                if (b == 'y' || b == 'b') dy = -dy;
                ox = nx; oy = ny;
                bounces++;
            }
            curX = nx; curY = ny;
        }
        segments.add(new double[]{ox, oy, curX, curY});
    }

    public boolean hits(Tank t) {
        if (!isFiring || System.currentTimeMillis() - fireStartTime < 1000) return false;
        Rectangle r = t.getBounds();
        for (int i = 0; i < segments.size() * tracerProgress; i++) {
            if (i >= segments.size()) break;
            double[] s = segments.get(i);
            if (lineIntersectsRect(s[0], s[1], s[2], s[3], r)) return true;
        }
        return false;
    }

    private boolean lineIntersectsRect(double x1, double y1, double x2, double y2, Rectangle r) {
        double dist = Math.hypot(x2 - x1, y2 - y1);
        for (int i = 0; i <= dist; i += 5) {
            double ratio = i / dist;
            if (r.contains(x1 + (x2 - x1) * ratio, y1 + (y2 - y1) * ratio)) return true;
        }
        return false;
    }
}
