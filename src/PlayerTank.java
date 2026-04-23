import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;


/**
 * Human-controlled tank.
 */
public class PlayerTank extends Tank {


    private boolean uP, dP, lP, rP, firePressed;
    private static final double SPEED = 3.2;
    private static final double TURN  = 0.07;


    public PlayerTank(double x, double y, Color color,
                      int up, int down, int left, int right, int fire) {
        super(x, y, color, up, down, left, right, fire);
    }


    public void handleInput(int k, boolean pressed) {
        if (k == upKey)    uP         = pressed;
        if (k == downKey)  dP         = pressed;
        if (k == leftKey)  lP         = pressed;
        if (k == rightKey) rP         = pressed;
        if (k == fireKey)  firePressed = pressed;
    }


    @Override
    public void update(Maze maze, java.util.List<Bullet> bullets, java.util.List<HomingMissile> missiles, java.util.List<RCMissile> rcMissiles, java.util.List<Laser> lasers, java.util.List<FragBomb> bombs, Tank enemy) {
        if (!alive) return;
        tickShield();
        tickAimGuide();

        // CHECK FOR MOVEMENT LOCK
        boolean isFiringLaser = false;
        for (Laser l : lasers) {
            if (l.owner == this && l.isFiring) {
                isFiringLaser = true;
                break;
            }
        }

        if (!isFiringLaser) {
            // Normal movement only if NOT firing
            double speed = (uP ? SPEED : 0) + (dP ? -SPEED : 0);
            angle += (lP ? -TURN : 0) + (rP ? TURN : 0);
            move(speed, maze);
        }

        if (firePressed && canFire()) {
            if (showAimGuide) {
                if (lasers.isEmpty()) {
                    lasers.add(new Laser(this));
                } else {
                    lasers.get(0).startFire();
                }
                lastFire = System.currentTimeMillis();
            } else {
                fireStandardBullet(bullets);
            }
        }
    }


}



