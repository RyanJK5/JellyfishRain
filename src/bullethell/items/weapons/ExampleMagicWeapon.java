package bullethell.items.weapons;

import java.awt.image.BufferedImage;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.combat.Projectile;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.Recipe;
import bullethell.movement.AngledPath;

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
        
        int centerX = player.getCenterX() - projSprite.getWidth() / 2;
        int centerY = player.getCenterY() - projSprite.getHeight() / 2;
        double angle = Globals.pointToCursorAngle(centerX, centerY);

        Projectile proj = new WeaponProjectile(this, Spritesheet.getSpriteSheet(projSprite), new AngledPath(angle), false,
          0, 20);
        proj.rotate((float) -angle + 180);
        proj.setRange(range);
        proj.setLocation(centerX, centerY);
        proj.setFriendly(true);
        
        player.setCurrentFire(0);
    }

}
