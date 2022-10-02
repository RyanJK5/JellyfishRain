package bullethell.combat.tags;

import java.awt.image.BufferedImage;

import bullethell.GameSolid;
import bullethell.Globals;
import bullethell.Spritesheet;
import bullethell.combat.Entity;
import bullethell.enemies.Enemy;
import bullethell.movement.Path;

public class DeathExplosion implements Tag {
    
    public final int radius;
    private final Spritesheet explosionSprite;

    public DeathExplosion(int radius) {
        this.radius = radius;
        BufferedImage explosionImage = new BufferedImage(radius, radius, BufferedImage.TYPE_INT_ARGB);
        explosionImage.getGraphics().drawImage(
            Globals.getImage("Explosion"), 0, 0, explosionImage.getWidth(), explosionImage.getHeight(), null);
        explosionSprite = Spritesheet.getSpriteSheet(explosionImage);
    }

    @Override
    public void apply(Enemy enemy) {
        Entity dmgBox = new Entity(explosionSprite, Path.DEFAULT_PATH, 100, 0, 0, true) {
            int age;
            static final int MAX_AGE = 100 / Globals.TIMER_DELAY;

            @Override
            public boolean onCollision(GameSolid obj) { 
                if (!(obj instanceof Enemy)) {
                    return false;
                }
                ((Enemy) obj).registerDMG(dmg);
                return true;
            } 

            @Override
            public void update() {
                age++;
                if (age >= MAX_AGE) {
                    kill();
                }
            }

            @Override
            public void registerDMG(int dmg) { }
            
            @Override
            public boolean readyToKill() { return false; }
        };
        dmgBox.setLocation(enemy.getCenterX() - dmgBox.getWidth() / 2, enemy.getCenterY() - dmgBox.getHeight() / 2);
    }

    @Override
    public boolean active() {
        return true;
    }

    @Override
    public TagActivationType getActivationType() {
        return TagActivationType.ON_DEATH;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DeathExplosion;
    }
}
