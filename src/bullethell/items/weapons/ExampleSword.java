package bullethell.items.weapons;

import java.awt.Rectangle;

import bullethell.Entity;
import bullethell.GameSolid;
import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.enemies.Enemy;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.Recipe;

public final class ExampleSword extends Item {

    private AtkBox atkBox;
    private float coverage;

    public AtkBox getAtkBox() { return atkBox; }

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
        (new AtkBox(Spritesheet.getSpriteSheet("Slash"), new Rectangle(140, 0, 43, 162))).update();
        player.setCurrentFire(0);
    }

    private int getWepDMG() { return dmg; }

    public final class AtkBox extends Entity {
		
        private final int lifeSpan = 15;
        private final int maxHits = 2;
        private final int hitDelay = lifeSpan / maxHits;        

        private int age = 1;
        private int numHits = 0;
        private int lastHit = 0;

        public AtkBox(Spritesheet sprite, Rectangle bounds) {
            super(sprite, null, getWepDMG(), 0, 0, true);
            setHitbox(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
            setLayer(99);
        }

        private double theta = 0;
        @Override
        public void update() {
            Player player = Player.get();
            if (readyToKill()) permakill();

            if (age == 1) {
                theta = Math.atan2(player.getCenterX() - Player.cursorX(), player.getCenterY() - Player.cursorY());
                double angle = Math.toDegrees(theta);
                angle = -angle;
                rotate((float) (angle - coverage / 2));
            }
            double angle = Math.toDegrees(theta);

            angle = -angle;
            rotate((float) (coverage / lifeSpan));

            setLocation(player.getCenterX() - w / 2, player.getCenterY() - h / 2);

            age += 1;
        }

        @Override
        public boolean readyToKill() {
            return age > lifeSpan;
        }

        @Override
        public boolean onCollision(GameSolid obj) {
            if (!(obj instanceof Enemy enem && !enem.friendly())) return false;
            if (numHits < maxHits && age - hitDelay >= lastHit && !enem.isInvicible()) {
                enem.registerDMG(dmg);
                Player.get().registerDealtDMG(dmg, this);
                numHits++;
                lastHit = age;
                return true;
            }
            return false;
        }

        @Override
        public void registerDMG(int dmg) { }
        @Override
        public Entity clone() { return null; }
    }
}