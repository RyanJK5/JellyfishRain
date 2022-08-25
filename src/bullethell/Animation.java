package bullethell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Animation implements ActionListener {

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

        Globals.GLOBAL_TIMER.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (!moving) {
            return;
        }

        if (currentFrame != frames.size() - 1) {
            currentFrame++;
        } else {
            currentFrame = 0;
        }
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
    public BufferedImage getFrame(int index) { return frames.get(index); }

    @Override
    public String toString() { 
        return "{" + "moving=" + moving + ", currentFrame=" + currentFrame + "}";
    }

    public static Animation[] getAnimations(Spritesheet spritesheet) {
        Animation[] result = new Animation[spritesheet.getHeight()];
        for (int i = 0; i < spritesheet.getHeight(); i++) {
            BufferedImage[] frames = new BufferedImage[spritesheet.getWidth()];
            for (int j = 0; j < spritesheet.getWidth(); j++) {
                frames[j] = spritesheet.getSprite(i, j);
            }
            result[i] = new Animation(frames, 0);
        }
        return result;
    }
}
