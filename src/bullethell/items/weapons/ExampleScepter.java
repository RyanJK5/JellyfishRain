package bullethell.items.weapons;

import java.awt.image.BufferedImage;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.combat.EnchantmentPool;
import bullethell.combat.Projectile;
import bullethell.combat.tags.StatusEffectType;
import bullethell.items.ItemID;
import bullethell.movement.AngledPath;

public final class ExampleScepter extends Weapon {
    
	private double maxSpread;
    private int shotNum;

    @Override
    protected void setEnchantmentParams() {
        enchantPool = EnchantmentPool.MAGIC_RANGED_WEAPON;
        allowedEffects = new StatusEffectType[] { StatusEffectType.POISON };
    }

    @Override
    protected void setValues() {
        id = ItemID.EXAMPLE_SCEPTER;

        name = "Example Scepter";

        dmg = 10;
        fireTime = 100 / Globals.TIMER_DELAY;
        manaCost = 0;

        maxSpread = 90;
        shotNum = 3;
    }

    public void onUse() {
        Player player = Player.get();
        BufferedImage projSprite = Globals.getImage("FriendlyBullet");
        int centerX = player.getCenterX() - projSprite.getWidth(null) / 2;
        int centerY = player.getCenterY() - projSprite.getHeight(null) / 2;
    
        double angle = Globals.pointToCursorAngle(centerX, centerY);

        for (double i = -maxSpread / 2; i <= maxSpread / 2; i += maxSpread / (shotNum - 1)) {
            Projectile proj = new WeaponProjectile(this, Spritesheet.getSpriteSheet(projSprite), 
              new AngledPath(angle + i), false, 0, 10);
            proj.setLocation(centerX, centerY);
            proj.setFriendly(true);
            proj.setRange(range);
        }
    }
}
