package bullethell;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Animation {
    private static List<Animation> instances = new ArrayList<>();

    private boolean moving = false;
    private List<BufferedImage> frames = new ArrayList<>();
    private int currentFrame;
    private int startFrame;

    public Animation(BufferedImage[] frames, int startFrame) {
        for (int i = 0; i < frames.length; i++) {
            this.frames.add(frames[i]);
        }
        this.startFrame = startFrame;
        currentFrame = startFrame;

        if (instances.size() == 0) {
            Globals.GLOBAL_TIMER.addActionListener((ActionEvent e) -> {
                for (Animation instance : instances) {
                    if (!instance.moving) continue;
                    if (instance.currentFrame != this.frames.size() - 1)
                        instance.currentFrame++;
                    else
                        instance.currentFrame = 0;
                }
            });
        }
        instances.add(this);
    }

    public void restart() {
        moving = true;
        currentFrame = startFrame;
    }

    public void reset() {
        moving = false;
        currentFrame = startFrame;
    }

    public void start() { 
        moving = true;
    }

    public void stop() {
        moving = false;
    }

    public boolean active() { return moving; }

    public void setToFrame(int frame) { this.currentFrame = frame; }
    public int getNumOfFrames() { return frames.size(); }
    
    public int getFrameIndex() { return currentFrame; }
    public BufferedImage getFrame() { return frames.get(currentFrame); }

    @Override
    public String toString() { 
        return "{" + "moving=" + moving + ", currentFrame=" + currentFrame + "}";
    }
}
