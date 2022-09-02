package bullethell.items.weapons;

import java.awt.image.BufferedImage;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Projectile;
import bullethell.Spritesheet;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.Recipe;
import bullethell.movement.AngledPath;
import bullethell.movement.Path;

public class ExampleMagicWeapon extends Item {

    @Override
    protected void setValues() {
        id = ItemID.EXAMPLE_MAGIC_WEAPON;
        equipType = EquipType.WEAPON;

        name = "Example Magic Weapon";

        dmg = 75;
        manaCost = 100;
        fireTime = 50 / Globals.TIMER_DELAY;
    }

    @Override
    public void addRecipes() {
        new Recipe(new ItemID[] {ItemID.MAGIC_DUST}, id, new int[] {20}, 1);
    }

    @Override
    public void onUse() {
        Player player = Player.get();
        BufferedImage projSprite = Globals.getImage("TriangleBullet");
        
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
    }

}
