import javax.swing.*;


/**
 * Entry point – launches the ScreenManager (title → mode select → game).
 */
public class TankTrouble {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        	
            GameMusic.playBackgroundMusic("Down Under - Agartha Remix - YourLocalSchizo (128k).wav");

        	
            JFrame frame = new JFrame("Tank Trouble");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            ScreenManager sm = new ScreenManager(frame);
            frame.add(sm);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}



