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

public final class ExampleStaff extends Weapon {
    
    public static final int DEFAULT_DMG = 10;
    public static final int DEFAULT_FIRE_TIME = 100 / Globals.TIMER_DELAY;
    public static final float DEFAULT_SHOT_SPEED = 20;
    public static final int DEFAULT_MANA_COST = 0;

    @Override
    public void setValues() {
        id = ItemID.EXAMPLE_STAFF;

        name = "Example Staff";

        dmg = 10;
        fireTime = 100 / Globals.TIMER_DELAY;
        manaCost = 0;
    }

    @Override
    protected void setEnchantmentParams() {
        enchantPool = EnchantmentPool.MAGIC_RANGED_WEAPON;
        allowedEffects = new StatusEffectType[] { StatusEffectType.POISON, StatusEffectType.FREEZE, StatusEffectType.BLEED };
    }

    @Override
    public void onUse() { 
        Player player = Player.get();
        BufferedImage projSprite = Globals.getImage("FriendlyBullet");
        
        int centerX = player.getCenterX() - projSprite.getWidth(null) / 2;
        int centerY = player.getCenterY() - projSprite.getHeight(null) / 2;

        double angle = Globals.pointToCursorAngle(centerX, centerY);

        Projectile proj = new WeaponProjectile(this, Spritesheet.getSpriteSheet(projSprite), new AngledPath(angle), false, 0, 20);
        proj.rotate((float) -angle + 180);
        proj.setRange(range);
        proj.setLocation(centerX, centerY);
        proj.setFriendly(true);
    }
}