import java.util.ArrayList;
import java.util.Random;


public class Everything {
	
	
	public class World {
	    private PlayerTank player;
	    private ArrayList<AITank> bots;
	    private ArrayList<Bullet> bullets;
	    private ArrayList<Wall> walls;
	    private ArrayList<PowerUp> powerUps;
	    private Random random = new Random();

	    public World() {
	        this.player = new PlayerTank(100, 100);
	        this.bots = new ArrayList<>();
	        this.bullets = new ArrayList<>();
	        this.walls = new ArrayList<>();
	        this.powerUps = new ArrayList<>();
	    }

	    /**
	     * Periodically spawns a random power-up at a random location.
	     */
	    public void spawnPowerUps() {
	        int x = random.nextInt(800); 
	        int y = random.nextInt(600);
	        
	        
	        int type = random.nextInt(3);
	        if (type == 0) {
	            powerUps.add(new SpeedBoostPowerUp(x, y, 500));
	        } else if (type == 1) {
	            powerUps.add(new ShieldPowerUp(x, y));
	        } else {
	            powerUps.add(new RapidFirePowerUp(x, y, 300, 5));
	        }
	    }

	    /**
	     * Checks if the player has touched any active power-ups.
	     */
	    public void checkCollisions() {
	        for (int i = powerUps.size() - 1; i >= 0; i--) {
	            PowerUp p = powerUps.get(i);
	            
	            if (Math.hypot(player.x - p.x, player.y - p.y) < 30) {
	                p.activate(player); 
	                powerUps.remove(i); 
	            }
	        }
	    }

	    public void update() {
	        player.update();
	        checkCollisions();
	    }
	}

	public class Tank {
		
	    protected int x;
	    protected int y;
	    protected int bullets;
	    protected int speed;
	    protected int score;
	    protected double direction; 
	
	    public boolean hasHomingLaser;
	    public boolean hasHomingMissile;
	    public boolean hasShotgun;
	    public boolean hasShield;
	    public boolean hasRapidFire;
	
	    public Tank(int x, int y) {
	        this.x = x;
	        this.y = y;
	        this.bullets = 20; 
	        this.speed = 2;  
	        this.score = 0;
	        this.direction = 0.0;
	        
	        this.hasHomingLaser = false;
	        this.hasHomingMissile = false;
	        this.hasShotgun = false;
	        this.hasShield = false;
	        this.hasRapidFire = false;
	    }
	
	    public void shoot() {
	        if (bullets > 0) {
	            bullets--;
	        }
	    }
	
	    public void speedUp() {
	        this.speed += 1;
	    }
	
	    public boolean isAlive() {
	        return true; 
	    }
	}
	
	public class Bullet {
	
		private int x;
	    private int y;
	    private double direction;
	    private int speed;
	    private int bounceCount;
	    private Tank owner;
	
	    public Bullet(int x, int y, double direction, int speed, Tank owner) {
	        this.x = x;
	        this.y = y;
	        this.direction = direction;
	        this.speed = speed;
	        this.owner = owner;
	        this.bounceCount = 0;
	    }
	
	    public void move() {
	        x += (int) (Math.cos(direction) * speed);
	        y += (int) (Math.sin(direction) * speed);
	    }
	
	    public void bounce() {
	        bounceCount++;
	        
	    }
	
	    public void dissipate() {
	    }
	
	    public boolean checkCollision() {
	        return false; 
	    }
	
	    public void draw() {
	    }
	    
	}
	
	public abstract class PowerUp {
	    protected int x;
	    protected int y;
	    protected String name;

	    public PowerUp(int x, int y, String name) {
	        this.x = x;
	        this.y = y;
	        this.name = name;
	    }

	    public void draw() {
	    }

	    public abstract void activate(Tank t);
	}
	
	public class SpeedBoostPowerUp extends PowerUp {
	    private int duration;

	    public SpeedBoostPowerUp(int x, int y, int duration) {
	        super(x, y, "Speed Boost");
	        this.duration = duration;
	    }

	    @Override
	    public void activate(Tank t) {
	        t.speedUp();
	    }
	}

	
	public class HomingMissilePowerUp extends PowerUp {
	    public HomingMissilePowerUp(int x, int y) {
	        super(x, y, "Homing Missile");
	    }

	    @Override
	    public void activate(Tank t) {
	        t.hasHomingMissile = true;
	    }
	}

	public class ShieldPowerUp extends PowerUp {
	    public ShieldPowerUp(int x, int y) {
	        super(x, y, "Shield");
	    }

	    @Override
	    public void activate(Tank t) {
	        t.hasShield = true;
	    }
	}

	public class RapidFirePowerUp extends PowerUp {
	    private int duration;
	    private int fireRate;

	    public RapidFirePowerUp(int x, int y, int duration, int fireRate) {
	        super(x, y, "Rapid Fire");
	        this.duration = duration;
	        this.fireRate = fireRate;
	    }

	    @Override
	    public void activate(Tank t) {
	        t.hasRapidFire = true;
	    }
	}

	
	
	
}


