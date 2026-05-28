import javax.swing.*;


/**
 * Entry point – launches the ScreenManager (title → mode select → game).
 */
// this is the main class, running this starts the whole game
public class TankTrouble {
    public static void main(String[] args) {
        // invokeLater makes sure swing stuff runs on the right thread or something
        SwingUtilities.invokeLater(() -> {
        	
            GameMusic.playBackgroundMusic("src/Down Under - Agartha Remix - YourLocalSchizo (128k).wav"); // starts the background music

        	
            JFrame frame = new JFrame("Tank Trouble");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            ScreenManager sm = new ScreenManager(frame);
            frame.add(sm);
            frame.pack();
            frame.setLocationRelativeTo(null); // centers the window on the screen
            frame.setVisible(true);
        });
    }
}



