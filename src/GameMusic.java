import javax.sound.sampled.*;
import java.io.File;

// handles loading and playing the background music
public class GameMusic {
    private static Clip clip;

    // loads a wav file and loops it forever until the game closes
    public static void playBackgroundMusic(String filePath) {
        try {
            File musicPath = new File(filePath);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                System.out.println("Music is playing!");
            } else {
                System.out.println("Can't find the file: " + filePath);
                System.out.println("Put your .wav file in this folder: " + System.getProperty("user.dir"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // converts a 0-100 percent to decibels because thats how java audio works, kinda weird
    public static void setVolume(float percent) {
        if (clip == null) return;
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float db = (percent == 0) ? -80f : (float)(Math.log10(percent / 100.0) * 20);
        volume.setValue(Math.max(volume.getMinimum(), Math.min(db, volume.getMaximum())));
    }
}
