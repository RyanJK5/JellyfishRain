package bullethell.items.weapons;

import java.awt.Dimension;
import java.awt.Rectangle;

import bullethell.Globals;
import bullethell.Spritesheet;
import bullethell.combat.EnchantmentPool;
import bullethell.combat.MeleeHitbox;
import bullethell.combat.tags.StatusEffectType;
import bullethell.enemies.Enemy;
import bullethell.items.ItemID;

public class TripleKnife extends Weapon {
    
    private static final int RESET_DELAY = 500 / Globals.TIMER_DELAY;
    
    private static final Spritesheet spritesheet = new Spritesheet(Globals.getImage("DefaultSlash"), new Dimension[][] {
        new Dimension[] {new Dimension(127, 100)},
        new Dimension[] {new Dimension(127, 100)},
        new Dimension[] {new Dimension(77, 100)}
    });

    private int strikeNum;
    private int timeSinceLastAttack;

    @Override
    protected void setEnchantmentParams() {
        enchantPool = EnchantmentPool.MELEE_WEAPON;
        allowedEffects = new StatusEffectType[] { StatusEffectType.POISON };
    }

    @Override
    protected void setValues() {
        id = ItemID.TRIPLE_KNIFE;
        name = "Triple Knife";
        description = "Three rapid attacks, followed by a cooldown";
        dmg = 20;
        fireTime = 15;

        critMultiplier = 50f / dmg;
    }

    public void update(java.awt.Graphics g) {
        super.update(g);
        timeSinceLastAttack++;
        if (timeSinceLastAttack >= RESET_DELAY) {
            strikeNum = 0;
        }
    }

    @Override
    public boolean critCondition(Enemy enemy) {
        return strikeNum == 2;
    }

    @Override
    public void onUse() {
        fireTime = 15;
        timeSinceLastAttack = 0;
        Rectangle bounds = new Rectangle(0, 0, spritesheet.getSprite(0, strikeNum).getWidth(), spritesheet.getSprite(0, strikeNum).getHeight());
        MeleeHitbox hitbox = new MeleeHitbox(this, spritesheet, bounds, 10, 1);
        hitbox.getCurrentAnimation().setToFrame(strikeNum);
        hitbox.update();
        
        strikeNum++;
        switch (strikeNum) { 
            case 1:
                fireTime = 15;
                break;
            case 3:
                strikeNum = 0;
                fireTime = 30;
                break;
        }
    }
}
