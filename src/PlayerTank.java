import java.awt.*;
import java.util.*;


/**
 * Human-controlled tank.
 */
// this is the tank the actual player controls with keyboard input
public class PlayerTank extends Tank {


    // these booleans track wich keys are currently being held down
    private boolean uP, dP, lP, rP, firePressed;
    private static final double SPEED = 3.2;
    private static final double TURN  = 0.07;


    public PlayerTank(double x, double y, Color color,
                      int up, int down, int left, int right, int fire) {
        super(x, y, color, up, down, left, right, fire);
    }


    // this gets called every time a key is pressed or released to update the movement flags
    public void handleInput(int k, boolean pressed) {
        if (k == upKey)    uP         = pressed;
        if (k == downKey)  dP         = pressed;
        if (k == leftKey)  lP         = pressed;
        if (k == rightKey) rP         = pressed;
        if (k == fireKey)  firePressed = pressed;
    }


    @Override
    public void update (Maze maze, ArrayList<Bullet> bullets, ArrayList<Laser> lasers, Tank enemy) {
        if (!alive) return;
        tickShield();
        tickAimGuide();

        boolean isFiringLaser = false;
        for (Laser l : lasers) {
            if (l.owner == this && l.isFiring) {
                isFiringLaser = true;
                break;
            }
        }

        if (!isFiringLaser) {
            // only let the tank move if its not in the middle of shooting a laser
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



