package bullethell;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum Audio {
    
    JELLY_SONG(AudioType.MUSIC, "AtTheSpeedOfLight"),
    SWITCH(AudioType.SOUND_EFFECT, "Switch");

    public final AudioType type;
    public final File srcFile;
    public float volume = 2f;
    private Clip clip;

    private Audio(AudioType type, String fileName) {
        this.type = type;
        srcFile = new File("audio\\" + fileName + ".wav");
        try {
            clip = AudioSystem.getClip();   
            clip.open(AudioSystem.getAudioInputStream(srcFile));
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public Clip getClip() {
        return clip;
    }

    public void setVolume(float volume) {
        if (volume < 0) {
            throw new IllegalArgumentException("volume + (" + volume + ") must be greater than or equal to 0");
        }
    }
}
