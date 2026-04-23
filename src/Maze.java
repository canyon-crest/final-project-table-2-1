import java.util.*;
import java.awt.*;


/**
 * Maze – generates a perfect (spanning-tree) maze with recursive back-tracker
 * and exposes flood-fill distance maps used by the AI.
 *
 * Coordinate convention:
 *   vWalls[col][row]  – vertical wall on the LEFT edge of cell (col, row)
 *   hWalls[col][row]  – horizontal wall on the TOP edge of cell (col, row)
 *   Outer border walls are always true.
 */
public class Maze {


    public static final int GRID  = 10;
    public static final int CELL  = 60;          // pixels per cell
    public static final int PX    = GRID * CELL; // 600 px


    // vWalls[GRID+1][GRID], hWalls[GRID][GRID+1]
    boolean[][] vWalls, hWalls;


    public Maze() {
        generate();
    }


    // ──────────────────────────────────────────────
    //  Generation (recursive back-tracker / DFS)
    // ──────────────────────────────────────────────
    public void generate() {
        vWalls = new boolean[GRID + 1][GRID];
        hWalls = new boolean[GRID][GRID + 1];


        // Fill everything with walls
        for (int i = 0; i <= GRID; i++)
            for (int j = 0; j < GRID; j++) {
                vWalls[i][j] = true;
                hWalls[j][i] = true;
            }


        // DFS carve
        boolean[][] visited = new boolean[GRID][GRID];
        Deque<Point> stack = new ArrayDeque<>();
        Random rng = new Random();


        Point start = new Point(0, 0);
        visited[0][0] = true;
        stack.push(start);
        int total = GRID * GRID, done = 1;


        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};


        while (done < total) {
            Point c = stack.peek();
            java.util.List<Point> nbrs = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = c.x + d[0], ny = c.y + d[1];
                if (nx >= 0 && nx < GRID && ny >= 0 && ny < GRID && !visited[nx][ny])
                    nbrs.add(new Point(nx, ny));
            }
            if (!nbrs.isEmpty()) {
                Point next = nbrs.get(rng.nextInt(nbrs.size()));
                removeWall(c, next);
                visited[next.x][next.y] = true;
                stack.push(next);
                done++;
            } else {
                stack.pop();
            }
        }


        // ── Extra wall removal – punch ~25 extra holes to create loops ──
        int toRemove = 25;
        for (int attempt = 0; attempt < toRemove * 15 && toRemove > 0; attempt++) {
            if (rng.nextBoolean()) {
                int col = 1 + rng.nextInt(GRID - 1); // interior vertical walls
                int row = rng.nextInt(GRID);
                if (vWalls[col][row]) { vWalls[col][row] = false; toRemove--; }
            } else {
                int col = rng.nextInt(GRID);
                int row = 1 + rng.nextInt(GRID - 1); // interior horizontal walls
                if (hWalls[col][row]) { hWalls[col][row] = false; toRemove--; }
            }
        }
    }


    private void removeWall(Point a, Point b) {
        if (a.x == b.x) {
            // same column – horizontal wall between rows a.y and b.y
            hWalls[a.x][Math.max(a.y, b.y)] = false;
        } else {
            // same row – vertical wall between cols a.x and b.x
            vWalls[Math.max(a.x, b.x)][a.y] = false;
        }
    }


    // ──────────────────────────────────────────────
    //  Flood-fill (BFS distance map from a pixel pos)
    //  Returns dist[col][row] = BFS distance from seed cell, -1 = unreachable
    // ──────────────────────────────────────────────
    public int[][] floodFill(double px, double py) {
        int[][] dist = new int[GRID][GRID];
        for (int[] row : dist) Arrays.fill(row, -1);


        int sc = (int)(px / CELL), sr = (int)(py / CELL);
        sc = Math.max(0, Math.min(GRID-1, sc));
        sr = Math.max(0, Math.min(GRID-1, sr));


        Deque<Point> q = new ArrayDeque<>();
        dist[sc][sr] = 0;
        q.add(new Point(sc, sr));


        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};


        while (!q.isEmpty()) {
            Point c = q.poll();
            for (int[] d : dirs) {
                int nx = c.x + d[0], ny = c.y + d[1];
                if (nx < 0 || nx >= GRID || ny < 0 || ny >= GRID) continue;
                if (dist[nx][ny] != -1) continue;
                if (!canPass(c.x, c.y, d[0], d[1])) continue;
                dist[nx][ny] = dist[c.x][c.y] + 1;
                q.add(new Point(nx, ny));
            }
        }
        return dist;
    }


    /** True if movement from (col,row) in direction (dc,dr) is unobstructed */
    public boolean canPass(int col, int row, int dc, int dr) {
        if (dc == 1)  return !vWalls[col+1][row];
        if (dc == -1) return !vWalls[col][row];
        if (dr == 1)  return !hWalls[col][row+1];
        if (dr == -1) return !hWalls[col][row];
        return false;
    }


    // ──────────────────────────────────────────────
    //  Wall-hit helpers used by Tank & Bullet
    // ──────────────────────────────────────────────


    /**
     * Returns true if a circle (px,py,radius) overlaps any wall segment.
     * Used for tank body collision.
     */
    public boolean circleHitsWall(double px, double py, double radius) {
        // boundary
        if (px - radius < 0 || px + radius > PX || py - radius < 0 || py + radius > PX)
            return true;


        int col = (int)(px / CELL), row = (int)(py / CELL);
        col = Math.max(0, Math.min(GRID-1, col));
        row = Math.max(0, Math.min(GRID-1, row));


        double lx = px - col * CELL;   // position within cell (0..CELL)
        double ly = py - row * CELL;


        // Check the four walls of current cell
        if (vWalls[col][row]   && lx < radius)         return true;
        if (vWalls[col+1][row] && lx > CELL - radius)  return true;
        if (hWalls[col][row]   && ly < radius)         return true;
        if (hWalls[col][row+1] && ly > CELL - radius)  return true;


        return false;
    }


    /**
     * Returns 'x', 'y', or 0 indicating which axis a bullet moving from
     * (ox,oy) to (nx,ny) should bounce off.
     */
    public char bulletBounce(double ox, double oy, double nx, double ny, double dvx, double dvy) {
        int col = (int)(ox / CELL), row = (int)(oy / CELL);
        col = Math.max(0, Math.min(GRID-1, col));
        row = Math.max(0, Math.min(GRID-1, row));


        double lx = nx - col * CELL;
        double ly = ny - row * CELL;


        final double MARGIN = 4.0;
        boolean bounceX = false, bounceY = false;


        if (dvx < 0 && vWalls[col][row]   && lx < MARGIN)         bounceX = true;
        if (dvx > 0 && vWalls[col+1][row] && lx > CELL - MARGIN)  bounceX = true;
        if (dvy < 0 && hWalls[col][row]   && ly < MARGIN)         bounceY = true;
        if (dvy > 0 && hWalls[col][row+1] && ly > CELL - MARGIN)  bounceY = true;


        // Boundary bounce
        if (nx <= MARGIN || nx >= PX - MARGIN) bounceX = true;
        if (ny <= MARGIN || ny >= PX - MARGIN) bounceY = true;


        if (bounceX && bounceY) return 'b'; // corner
        if (bounceX) return 'x';
        if (bounceY) return 'y';
        return 0;
    }


    // ──────────────────────────────────────────────
    //  Rendering
    // ──────────────────────────────────────────────
    public void draw(Graphics2D g) {
        g.setColor(new Color(50, 50, 60));
        g.fillRect(0, 0, PX, PX);


        g.setColor(new Color(200, 200, 220));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


        for (int i = 0; i <= GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                if (vWalls[i][j])
                    g.drawLine(i*CELL, j*CELL, i*CELL, (j+1)*CELL);
                if (hWalls[j][i])
                    g.drawLine(j*CELL, i*CELL, (j+1)*CELL, i*CELL);
            }
        }
    }
}



