import javax.sound.sampled.*;
import java.io.File;

public class GameMusic {
    public static void main(String[] args) {
        // This calls the method with your exact filename
        playBackgroundMusic("Down Under - Agartha Remix - YourLocalSchizo (128k).wav");
    }

    public static void playBackgroundMusic(String filePath) {
        try {
            File musicPath = new File(filePath);
            
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                
                // Loops the song forever
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                
                System.out.println("Music is playing!");
            } else {
                // If it fails, this will tell you exactly where Java is looking
                System.out.println("Can't find the file: " + filePath);
                System.out.println("Put your .wav file in this folder: " + System.getProperty("user.dir"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
