package bullethell;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

import bullethell.movement.Path;

public abstract class AnimatedEntity extends Entity {
	
    protected Animation[] animations;
    protected Animation currentAnimation;

    public AnimatedEntity(Spritesheet spritesheet, Path path, int dmg, int maxHP, float speed, boolean friendly) {
        super(spritesheet.getSprite(0, 0), path, dmg, maxHP, speed, friendly);
        
        animations = new Animation[spritesheet.getHeight()];
        for (int i = 0; i < spritesheet.getHeight(); i++) {
            BufferedImage[] frames = new BufferedImage[spritesheet.getWidth()];
            for (int j = 0; j < spritesheet.getWidth(); j++) {
                frames[j] = spritesheet.getSprite(i, j);
            }
            animations[i] = new Animation(frames, 0);
        }
        currentAnimation = animations[0];
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(currentAnimation.getFrame(), x, y, null);
    }

    protected void setAnimation(int index) {
        currentAnimation.reset();
        currentAnimation = animations[index];
        currentAnimation.restart();
    }

    protected Animation getAnimation(int index) {
        return animations[index];
    }
}
