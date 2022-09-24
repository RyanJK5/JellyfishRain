package bullethell.items.weapons;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.MeleeHitbox;

public class TripleKnife extends Item {
    
    BufferedImage slashSprite = Globals.getImage("DefaultSlash");
    private int strikeNum;
    private int timeSinceLastAttack;
    private static final int RESET_DELAY = 500 / Globals.TIMER_DELAY;

    @Override
    protected void setValues() {
        id = ItemID.TRIPLE_KNIFE;
        equipType = EquipType.WEAPON;
        name = "Triple Knife";
        description = "Three rapid attacks, followed by a cooldown";
        dmg = 20;
        fireTime = 15;
    }

    public void update(java.awt.Graphics g) {
        super.update(g);
        timeSinceLastAttack++;
        if (timeSinceLastAttack > RESET_DELAY) {
            strikeNum = 0;
        }
    }

    @Override
    public void onUse() {
        fireTime = 15;
        timeSinceLastAttack = 0;
        Spritesheet sheet = new Spritesheet(slashSprite, new Dimension[][] {
            new Dimension[] {new Dimension(127, 100)},
            new Dimension[] {new Dimension(127, 100)},
            new Dimension[] {new Dimension(77, 100)}
        });
        Rectangle bounds = new Rectangle(0, 0, sheet.getSprite(0, strikeNum).getWidth(), sheet.getSprite(0, strikeNum).getHeight());
        MeleeHitbox hitbox = new MeleeHitbox(sheet, bounds, dmg, 10, 1);
        hitbox.getCurrentAnimation().setToFrame(strikeNum);
        hitbox.update();
        
        strikeNum++;
        switch (strikeNum) { 
            case 1:
                fireTime = 15;
                break;
            case 2:
                dmg = 30;
                break;
            case 3:
                dmg = 20;
                strikeNum = 0;
                fireTime = 30;
                break;
        }
        Player.get().setCurrentFire(0);
    }
}
