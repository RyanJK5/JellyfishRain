package bullethell;

import javax.sound.sampled.FloatControl;

public enum AudioType {
    MUSIC, SOUND_EFFECT, AMBIENT;

    private float volume = 1;

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        resetAudioVolumes();
    }

    private static void resetAudioVolumes() {
        for (Audio audio : Audio.values()) {
            FloatControl gainControl = (FloatControl) audio.getClip().getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(audio.volume * audio.type.volume));
        }
    }
}
