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

public final class ExampleScepter extends Item {
    
	private double maxSpread;
    private int shotNum;

    @Override
    protected void setValues() {
        id = ItemID.EXAMPLE_SCEPTER;
        equipType = EquipType.WEAPON;

        name = "Example Scepter";

        dmg = 10;
        fireTime = 100 / Globals.TIMER_DELAY;
        manaCost = 0;

        maxSpread = 90;
        shotNum = 3;
    }

    public void onUse() {
        Player player = Player.get();
        if (!(player.getCurrentFire() >= fireTime && player.isAlive())) return;
        try {
            BufferedImage projSprite = ImageIO.read(new File("sprites/FriendlyBullet.png"));
            int centerX = player.getCenterX() - projSprite.getWidth(null) / 2;
            int centerY = player.getCenterY() - projSprite.getHeight(null) / 2;
            
            centerX = player.getX() + player.getWidth() / 2 - projSprite.getWidth(null) / 2;
            centerY = player.getY() + player.getHeight() / 2 - projSprite.getHeight(null) / 2;
            
            double angle = Math.atan2(centerX - Player.cursorX(), centerY - Player.cursorY());
            angle = Math.toDegrees(angle);
            angle += 180;
            if (angle < 0) angle += 360;

            for (double i = -maxSpread / 2; i <= maxSpread / 2; i += maxSpread / (shotNum - 1)) {
                Path path = new AngledPath(angle + i);
                Projectile proj = new Projectile(Spritesheet.getSpriteSheet(projSprite), path, false, 0, 10, dmg);
                proj.setLocation(centerX, centerY);
                proj.setFriendly(true);
                proj.setRange(range);
            }
            
            player.setCurrentFire(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
