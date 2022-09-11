package bullethell.enemies;

import java.awt.Rectangle;
import java.awt.Dimension;

import bullethell.Globals;
import bullethell.Spritesheet;
import bullethell.scenes.ErnestoBoss;

public class JellyFishBoss extends Enemy {
    
    @Override
    protected void setValues() {
        id = EnemyID.JELLY_FISH_BOSS;
        name = "Ernesto";
        maxHP = 8500;
        hp = 8500;
        dmg = 100;
        speed = 20;
        bossEnemy = true;
        setHitbox(new Rectangle(0, 0, 0, 0));
        
        Dimension[][] dimensions = new Dimension[11][2];
        for (int i = 0; i < dimensions.length; i++) {
            for (int j = 0; j < dimensions[0].length; j++) {
                dimensions[i][j] = new Dimension(308, j == 0 ? 293 : 277);
            }
        }

        setAnimations(new Spritesheet(Globals.getImage("enemies\\JellyFishBoss"), dimensions));
        for (int i = 1; i < 11; i++) {
            getAnimation(0).removeFrame(i);
        }
        getAnimation(1).setFrameRate(5);
        getCurrentAnimation().start();
        
        setLayer(3);
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
    public boolean switchAlpha = false;
    
    public boolean fade(float alphaDecr) {
        if (timesPerformed % (1 / alphaDecr) == 0) {
            switchAlpha = !switchAlpha;
        }
        if (switchAlpha) {
            alphaDecr = -alphaDecr;
        }
        if (opacity - alphaDecr >= 0 && opacity - alphaDecr <= 1) {
            opacity -= alphaDecr;
        }
        timesPerformed++;
        return timesPerformed % (1 / Math.abs(alphaDecr) * 2) == 0;
    }
}

