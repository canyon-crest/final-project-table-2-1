import java.awt.Graphics2D;

// every object that gets drawn on screen has to implement this interface
public interface Drawable {
    void draw(Graphics2D g); // each class does the drawing itself
}
