import javax.sound.sampled.*;
import java.io.File;

public class GameMusic {
    private static Clip clip;

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

    public static void setVolume(float percent) {
        if (clip == null) return;
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float db = (percent == 0) ? -80f : (float)(Math.log10(percent / 100.0) * 20);
        volume.setValue(Math.max(volume.getMinimum(), Math.min(db, volume.getMaximum())));
    }
}
