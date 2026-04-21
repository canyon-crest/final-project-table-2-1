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
        this.hasShotgun = false
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
