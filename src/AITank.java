import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

/**
 * Computer-controlled tank with smooth steering, threat evasion, and difficulty-locked self-bounce shooting protection.
 */
public class AITank extends Tank {
    private static final double SPEED = 3.2;
    private static final double TURN = 0.07;
    private static final int DIFFICULTY_EASY = 0;
    private static final int DIFFICULTY_MEDIUM = 1;
    private static final int DIFFICULTY_HARD = 2;
    
    private int difficulty; 
    private int pathfindTimer = 0;
    private int[] pathDir = {0, 0};
    private double targetAngle = 0;
    private double targetX, targetY;
    private boolean hasTarget = false;

    public AITank(double x, double y, Color color, int difficulty) {
        super(x, y, color, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD0);
        this.difficulty = difficulty;
        targetX = x;
        targetY = y;
    }

    @Override
    public void update(Maze maze, java.util.List<Bullet> bullets, java.util.List<HomingMissile> missiles, java.util.List<RCMissile> rcMissiles, java.util.List<Laser> lasers, java.util.List<FragBomb> bombs, Tank enemy) {
        if (!alive) return;

        tickShield();
        tickAimGuide();

        if (++pathfindTimer >= 5) {
            pathfindTimer = 0;
            computeNextTarget(maze, enemy);
        }

        // Base destination direction
        double dx = targetX - x, dy = targetY - y;
        double desired = Math.atan2(dy, dx);

        // GLOBAL DODGE SYSTEM: Avoids all incoming projectiles on medium/hard difficulties
        if (difficulty > DIFFICULTY_EASY) {
            double escapeAngle = findEscapeAngle(bullets, missiles, bombs, lasers);
            if (escapeAngle != Double.MAX_VALUE) {
                double option1 = escapeAngle;
                double option2 = escapeAngle + Math.PI;
                double d1 = Math.abs(option1 - desired);
                while (d1 > Math.PI) d1 -= 2 * Math.PI;
                double d2 = Math.abs(option2 - desired);
                while (d2 > Math.PI) d2 -= 2 * Math.PI;
                double chosenDodge = (Math.abs(d1) < Math.abs(d2)) ? option1 : option2;
                desired = desired * 0.3 + chosenDodge * 0.7;
            }
        }

        // Calculate angular difference
        double diff = desired - angle;
        while (diff > Math.PI) diff -= 2 * Math.PI;
        while (diff < -Math.PI) diff += 2 * Math.PI;

        double turnRate = TURN * (difficulty == DIFFICULTY_HARD ? 1.6 : 1.0);
        angle += Math.min(Math.abs(diff), turnRate) * Math.signum(diff);

        double forwardFactor = Math.max(0, Math.cos(diff));
        move(SPEED * forwardFactor, maze);

        // Fire safely
        tryFire(bullets, missiles, lasers, bombs, maze, enemy);
    }

    private double findEscapeAngle(java.util.List<Bullet> bullets, java.util.List<HomingMissile> missiles, java.util.List<FragBomb> bombs, java.util.List<Laser> lasers) {
        double closestDist = 180.0; 
        double threatAngle = Double.MAX_VALUE;

        for (Bullet b : bullets) {
            if (b.owner == this && !b.hasBounced()) continue;
            double dist = Math.hypot(x - b.x, y - b.y);
            if (dist < closestDist && isHeadingTowardMe(b.x, b.y, b.angle)) {
                closestDist = dist;
                threatAngle = b.angle;
            }
        }

        for (HomingMissile m : missiles) {
            if (m.owner == this) continue;
            double dist = Math.hypot(x - m.x, y - m.y);
            if (dist < closestDist) {
                closestDist = dist;
                threatAngle = m.angle;
            }
        }

        for (FragBomb b : bombs) {
            if (b.owner == this) continue;
            double dist = Math.hypot(x - b.x, y - b.y);
            if (dist < closestDist) {
                closestDist = dist;
                threatAngle = Math.atan2(y - b.y, x - b.x);
            }
        }

        for (Laser l : lasers) {
            if (l.owner == this || !l.isFiring) continue;
            double dist = Math.hypot(x - l.owner.x, y - l.owner.y);
            if (dist < 300) {
                closestDist = dist;
                threatAngle = l.owner.angle;
            }
        }

        if (threatAngle != Double.MAX_VALUE) {
            return threatAngle + Math.PI / 2;
        }
        return Double.MAX_VALUE;
    }

    private boolean isHeadingTowardMe(double tx, double ty, double ta) {
        double bdx = x - tx;
        double bdy = y - ty;
        double angleToTank = Math.atan2(bdy, bdx);
        double angleDiff = Math.abs(angleToTank - ta);
        while (angleDiff > Math.PI) angleDiff = Math.abs(angleDiff - 2 * Math.PI);
        return angleDiff < 0.6;
    }

    private void computeNextTarget(Maze maze, Tank enemy) {
        int[][] dist = maze.floodFill(enemy.x, enemy.y);
        int myCol = Math.max(0, Math.min(Maze.GRID-1, (int)(x / Maze.CELL)));
        int myRow = Math.max(0, Math.min(Maze.GRID-1, (int)(y / Maze.CELL)));
        int bestDist = dist[myCol][myRow];
        if (bestDist <= 1) {
            targetX = x;
            targetY = y;
            return;
        }

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        int bestCol = myCol, bestRow = myRow;

        for (int[] d : dirs) {
            int nc = myCol + d[0], nr = myRow + d[1];
            if (nc < 0 || nc >= Maze.GRID || nr < 0 || nr >= Maze.GRID) continue;
            if (!maze.canPass(myCol, myRow, d[0], d[1])) continue;
            if (dist[nc][nr] >= 0 && dist[nc][nr] < bestDist) {
                bestDist = dist[nc][nr];
                bestCol = nc;
                bestRow = nr;
            }
        }

        if (difficulty == DIFFICULTY_EASY && new Random().nextInt(4) == 0) {
            java.util.List<int[]> opts = new ArrayList<>();
            for (int[] d : dirs) {
                int nc = myCol + d[0], nr = myRow + d[1];
                if (nc >= 0 && nc < Maze.GRID && nr >= 0 && nr < Maze.GRID && maze.canPass(myCol, myRow, d[0], d[1]) && dist[nc][nr] >= 0)
                    opts.add(new int[]{nc, nr});
            }
            if (!opts.isEmpty()) {
                int[] choice = opts.get(new Random().nextInt(opts.size()));
                bestCol = choice[0];
                bestRow = choice[1];
            }
        }
        targetX = bestCol * Maze.CELL + Maze.CELL / 2.0;
        targetY = bestRow * Maze.CELL + Maze.CELL / 2.0;
    }

    private void tryFire(java.util.List<Bullet> bullets, java.util.List<HomingMissile> missiles, java.util.List<Laser> lasers, java.util.List<FragBomb> bombs, Maze maze, Tank enemy) {
        if (!canFire()) return;

        double dx = enemy.x - x, dy = enemy.y - y;
        double distanceToEnemy = Math.hypot(dx, dy);
        if (distanceToEnemy > 500) return;

        double angleToEnemy = Math.atan2(dy, dx);
        double diff = Math.abs(angleToEnemy - angle);
        while (diff > Math.PI) diff = Math.abs(diff - 2 * Math.PI);

        double aimTolerance = (difficulty == DIFFICULTY_HARD) ? 0.12 : (difficulty == DIFFICULTY_MEDIUM) ? 0.22 : 0.35;

        if (diff < aimTolerance) {
            // RESTRICTED TO HARD MODE: Only Hard AI runs self-bounce protection loops
            if (difficulty == DIFFICULTY_HARD) {
                boolean clearLineOfSight = true;
                double losX = x;
                double losY = y;
                double losSteps = distanceToEnemy / 4.0;
                double stepX = (dx / losSteps);
                double stepY = (dy / losSteps);

                for (int i = 0; i < (int)losSteps; i++) {
                    double nextLosX = losX + stepX;
                    double nextLosY = losY + stepY;
                    if (maze.bulletBounce(losX, losY, nextLosX, nextLosY, stepX, stepY) != 0) {
                        clearLineOfSight = false;
                        break;
                    }
                    losX = nextLosX;
                    losY = nextLosY;
                }

                // If line of sight is blocked by a wall, check if a blind shot is suicidal
                if (!clearLineOfSight) {
                    double simX = x;
                    double simY = y;
                    double simAngle = angle;
                    double simSpeed = 5.5; 
                    int simSteps = 45;     
                    int substeps = 8;      

                    for (int step = 0; step < simSteps; step++) {
                        double vx = simSpeed * Math.cos(simAngle);
                        double vy = simSpeed * Math.sin(simAngle);
                        double nextSimX = simX + vx / substeps;
                        double nextSimY = simY + vy / substeps;

                        char bounce = maze.bulletBounce(simX, simY, nextSimX, nextSimY, vx, vy);
                        if (bounce == 'x' || bounce == 'b') simAngle = Math.PI - simAngle;
                        if (bounce == 'y' || bounce == 'b') simAngle = -simAngle;

                        if (bounce == 0) {
                            simX = nextSimX;
                            simY = nextSimY;
                        }

                        if (step > 15) { 
                            double distToSelf = Math.hypot(simX - x, simY - y);
                            if (distToSelf < 20.0) { 
                                return; // Hard AI aborts to save itself
                            }
                        }
                    }
                }
            }

            // Easy, Medium, and clean Hard tanks fire normally here
            fireStandardBullet(bullets);
        }
    }
}
