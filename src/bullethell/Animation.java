package bullethell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Animation implements ActionListener {

    private List<BufferedImage> frames = new ArrayList<>();
    private int currentFrame;
    private int startFrame;

    private int frameRate = 0;
    private int timeSinceLastFrame;
    
    public Animation(BufferedImage[] frames, int startFrame) {
        for (int i = 0; i < frames.length; i++) {
            this.frames.add(frames[i]);
        }
        this.startFrame = startFrame;
        currentFrame = startFrame;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (timeSinceLastFrame < frameRate) {
            timeSinceLastFrame++;
            return;
        }
        timeSinceLastFrame = 0;
        
        if (currentFrame != frames.size() - 1) {
            currentFrame++;
        } else {
            currentFrame = 0;
        }
    }

    private void add() {
        if (!Globals.contains(Globals.GLOBAL_TIMER.getActionListeners(), this)) {
            Globals.GLOBAL_TIMER.addActionListener(this);
        }
    }

    public void restart() {
        add();
        currentFrame = startFrame;
    }

    public void reset() {
        Globals.GLOBAL_TIMER.removeActionListener(this);
        currentFrame = startFrame;
    }

    public void start() { 
        add();
    }

    public void stop() {
        Globals.GLOBAL_TIMER.removeActionListener(this);
    }

    public boolean active() { return Globals.contains(Globals.GLOBAL_TIMER.getActionListeners(), this); }

    public void setToFrame(int frame) { this.currentFrame = frame; }
    public int getNumOfFrames() { return frames.size(); }
    
    /**
     * @param frameRate the number of 12 millisecond intervals between each frame update
     */
    public void setFrameRate(int frameRate) {
        if (frameRate < 0) {
            throw new IllegalArgumentException("frameRate must be greater than 0");
        }
        this.frameRate = frameRate;
    }

    public int getFrameIndex() { return currentFrame; }
    public BufferedImage getFrame() { return frames.get(currentFrame); }
    public BufferedImage getFrame(int index) { return frames.get(index); }

    @Override
    public String toString() { 
        return "{" + "moving=" + active() + ", currentFrame=" + currentFrame + "}";
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
