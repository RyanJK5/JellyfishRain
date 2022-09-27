package bullethell.items.weapons;

import java.awt.Rectangle;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.combat.MeleeHitbox;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.Recipe;

public final class ExampleSword extends Item {

    private ExampleSwordHitbox atkBox;
    private float coverage;

    public ExampleSwordHitbox getAtkBox() { return atkBox; }

    @Override
    protected void setValues() {
        id = ItemID.EXAMPLE_SWORD;
        equipType = EquipType.WEAPON;

        name = "Example Sword";
        
        dmg = 40;
        fireTime = 300 / Globals.TIMER_DELAY;
        coverage = 90;
    }

    @Override
    protected void addRecipes() {
        new Recipe(new ItemID[] {ItemID.METAL}, id, new int[] {8}, 1);
    }

    @Override
    public void onUse() {
        Player player = Player.get();
        if (!(player.getCurrentFire() >= fireTime && player.isAlive())) {
            return;
        }
        new ExampleSwordHitbox();
    }

    public final class ExampleSwordHitbox extends MeleeHitbox {
		
        public ExampleSwordHitbox() {
            super(ExampleSword.this, Spritesheet.getSpriteSheet("Slash"), new Rectangle(0, 0, 43, 162), 
            15, 2);
        }

        @Override
        public void update() {
            super.update();

            Player player = Player.get();
            setRotationAnchor(player.getCenterX(), player.getCenterY());
            
            if (getAge() == 1) {
                rotate((float) (-coverage / 2));
            }
            rotate((float) (coverage / getLifeSpan()));
        }
    }
}