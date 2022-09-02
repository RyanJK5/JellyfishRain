package bullethell.enemies;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import bullethell.Spritesheet;
import bullethell.scenes.ErnestoBoss;

public class JellyFishBoss extends Enemy {
    
    @Override
    protected void setValues() {
        name = "Ernesto";
        maxHP = 8500;
        hp = 8500;
        dmg = 100;
        speed = 20;
        setHitbox(new Rectangle(0,0,0,0));
    }

    @Override
    protected void createLootTable() { }

    @Override
    public void update() {
        super.update();
        if (readyToKill()) {
            ErnestoBoss.get().end();
        }
    }
    
    public int timesPerformed = 0;
    public BufferedImage origSprite = sprite;
    public boolean switchAlpha = false;
    private float alpha = 0.2f;
    
    public boolean fade(float alphaDecr) {
        if (timesPerformed % (1 / alphaDecr) == 0) {
            switchAlpha = !switchAlpha;
        }
        if (switchAlpha) {
            alphaDecr = -alphaDecr;
        }
        if (alpha - alphaDecr >= 0 && alpha - alphaDecr <= 1) {
            alpha -= alphaDecr;
        }
        BufferedImage newSprite = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newSprite.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(origSprite, 0, 0, null);
        setAnimations(Spritesheet.getSpriteSheet(newSprite));
        timesPerformed++;
        return timesPerformed % (1 / Math.abs(alphaDecr) * 2) == 0;
    }
}

