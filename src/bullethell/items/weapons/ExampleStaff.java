package bullethell.items.weapons;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Projectile;
import bullethell.Spritesheet;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.movement.AngledPath;
import bullethell.movement.Path;

public final class ExampleStaff extends Item {
    
    public static final int DEFAULT_DMG = 10;
    public static final int DEFAULT_FIRE_TIME = 100 / Globals.TIMER_DELAY;
    public static final float DEFAULT_SHOT_SPEED = 20;
    public static final int DEFAULT_MANA_COST = 0;

    @Override
    public void setValues() {
        id = ItemID.EXAMPLE_STAFF;
        equipType = EquipType.WEAPON;

        name = "Example Staff";

        dmg = 10;
        fireTime = 100 / Globals.TIMER_DELAY;
        manaCost = 0;
    }

    @Override
    public void onUse() { 
        try {
            Player player = Player.get();
            BufferedImage projSprite = ImageIO.read(new File("sprites/FriendlyBullet.png"));
            
            if (!(player.getCurrentFire() >= fireTime && player.isAlive())) return;
            int centerX = player.getCenterX() - projSprite.getWidth(null) / 2;
            int centerY = player.getCenterY() - projSprite.getHeight(null) / 2;
        
            centerX = player.getX() + player.getWidth() / 2 - projSprite.getWidth(null) / 2;
            centerY = player.getY() + player.getHeight() / 2 - projSprite.getHeight(null) / 2;
            
            double angle = Math.atan2(centerX - Player.cursorX(), centerY - Player.cursorY());
            angle = Math.toDegrees(angle);
            angle += 180;
            if (angle < 0) angle += 360;
            
            Path path = new AngledPath(angle);

            Projectile proj = new Projectile(Spritesheet.getSpriteSheet(projSprite), path, false, 0, 20, dmg);
            proj.rotate((float) -angle + 180);
            proj.setRange(range);
            proj.setLocation(centerX, centerY);
            proj.setFriendly(true);
            
            player.setCurrentFire(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}