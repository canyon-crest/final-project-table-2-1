import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;


/**
 * Computer-controlled tank.
 *
 * Behaviour:
 *  - Flood-fill from the player's cell to build a distance map.
 *  - Navigate toward the player along the steepest descent.
 *  - Fire when facing roughly toward the player (line-of-sight or ricochet angle).
 *  - Uses power-ups when collected.
 */
public class AITank extends Tank {


    private static final double SPEED = 2.6;
    private static final double TURN  = 0.05;
    private static final int    DIFFICULTY_EASY   = 0;
    private static final int    DIFFICULTY_MEDIUM  = 1;
    private static final int    DIFFICULTY_HARD    = 2;


    private int difficulty;          // 0 easy, 1 medium, 2 hard
    private int pathfindTimer = 0;
    private int[] pathDir = {0, 0};  // current move direction in grid cells
    private double targetAngle = 0;


    // Target world-position centre of the next cell to move toward
    private double targetX, targetY;
    private boolean hasTarget = false;


    public AITank(double x, double y, Color color, int difficulty) {
        // AI uses dummy keys (never read from keyboard)
        super(x, y, color,
              KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD2,
              KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6,
              KeyEvent.VK_NUMPAD0);
        this.difficulty = difficulty;
        targetX = x; targetY = y;
    }


    @Override
    public void update(Maze maze,
                       java.util.List<Bullet>       bullets,
                       java.util.List<HomingMissile> missiles,
                       java.util.List<RCMissile>     rcMissiles,
                       java.util.List<Laser>         lasers,
                       java.util.List<FragBomb>      bombs,
                       Tank enemy) {
        if (!alive) return;
        tickShield();
        tickAimGuide();


        // Re-compute flood fill every 20 frames
        if (++pathfindTimer >= 20) {
            pathfindTimer = 0;
            computeNextTarget(maze, enemy);
        }


        // Steer toward targetX, targetY
        double dx = targetX - x, dy = targetY - y;
        double dist = Math.hypot(dx, dy);
        if (dist > 6) {
            double desired = Math.atan2(dy, dx);
            double diff = desired - angle;
            while (diff >  Math.PI) diff -= 2 * Math.PI;
            while (diff < -Math.PI) diff += 2 * Math.PI;


            double turnRate = TURN * (difficulty == DIFFICULTY_HARD ? 1.5 : 1.0);
            if (Math.abs(diff) < 0.15) {
                move(SPEED, maze);
            } else {
                angle += Math.min(Math.abs(diff), turnRate) * Math.signum(diff);
                // Small forward nudge while turning so AI doesn't stall
                move(SPEED * 0.3, maze);
            }
        }


        // Shoot if roughly aligned with enemy
        tryFire(bullets, missiles, lasers, bombs, maze, enemy);
    }


    private void computeNextTarget(Maze maze, Tank enemy) {
        int[][] dist = maze.floodFill(enemy.x, enemy.y);
        int myCol = Math.max(0, Math.min(Maze.GRID-1, (int)(x / Maze.CELL)));
        int myRow = Math.max(0, Math.min(Maze.GRID-1, (int)(y / Maze.CELL)));


        int bestDist = dist[myCol][myRow];
        if (bestDist <= 1) {
            // We're already adjacent – hold position
            targetX = x; targetY = y;
            return;
        }


        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        int bestCol = myCol, bestRow = myRow;
        for (int[] d : dirs) {
            int nc = myCol + d[0], nr = myRow + d[1];
            if (nc < 0 || nc >= Maze.GRID || nr < 0 || nr >= Maze.GRID) continue;
            if (!maze.canPass(myCol, myRow, d[0], d[1]))  continue;
            if (dist[nc][nr] >= 0 && dist[nc][nr] < bestDist) {
                bestDist = dist[nc][nr];
                bestCol  = nc; bestRow = nr;
            }
        }


        // Add slight randomness on easy difficulty
        if (difficulty == DIFFICULTY_EASY && new Random().nextInt(4) == 0) {
            // Pick a random passable neighbour instead
            java.util.List<int[]> opts = new ArrayList<>();
            for (int[] d : dirs) {
                int nc = myCol + d[0], nr = myRow + d[1];
                if (nc >= 0 && nc < Maze.GRID && nr >= 0 && nr < Maze.GRID
                        && maze.canPass(myCol, myRow, d[0], d[1]) && dist[nc][nr] >= 0)
                    opts.add(new int[]{nc, nr});
            }
            if (!opts.isEmpty()) {
                int[] choice = opts.get(new Random().nextInt(opts.size()));
                bestCol = choice[0]; bestRow = choice[1];
            }
        }


        targetX = bestCol * Maze.CELL + Maze.CELL / 2.0;
        targetY = bestRow * Maze.CELL + Maze.CELL / 2.0;
    }


    private void tryFire(java.util.List<Bullet>       bullets,
                         java.util.List<HomingMissile> missiles,
                         java.util.List<Laser>         lasers,
                         java.util.List<FragBomb>      bombs,
                         Maze maze, Tank enemy) {
        if (!canFire()) return;


        double dx = enemy.x - x, dy = enemy.y - y;
        double angleToEnemy = Math.atan2(dy, dx);
        double diff = Math.abs(angleToEnemy - angle);
        while (diff > Math.PI) diff = Math.abs(diff - 2 * Math.PI);


        double aimTolerance = (difficulty == DIFFICULTY_HARD)  ? 0.18
                            : (difficulty == DIFFICULTY_MEDIUM) ? 0.28
                            :                                     0.40;


        if (diff < aimTolerance) {
            fireStandardBullet(bullets);
        }
    }
}



