package bullethell.items.weapons;

import java.awt.Dimension;
import java.awt.Rectangle;

import bullethell.Globals;
import bullethell.Spritesheet;
import bullethell.combat.MeleeHitbox;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public class TripleKnife extends Item {
    
    private static final int RESET_DELAY = 500 / Globals.TIMER_DELAY;
    
    private static final Spritesheet spritesheet = new Spritesheet(Globals.getImage("DefaultSlash"), new Dimension[][] {
        new Dimension[] {new Dimension(127, 100)},
        new Dimension[] {new Dimension(127, 100)},
        new Dimension[] {new Dimension(77, 100)}
    });

    private int strikeNum;
    private int timeSinceLastAttack;

    @Override
    protected void setValues() {
        id = ItemID.TRIPLE_KNIFE;
        equipType = EquipType.WEAPON;
        name = "Triple Knife";
        description = "Three rapid attacks, followed by a cooldown";
        dmg = 20;
        fireTime = 15;

        critCondition = e -> strikeNum == 2;
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
