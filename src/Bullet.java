
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
	

