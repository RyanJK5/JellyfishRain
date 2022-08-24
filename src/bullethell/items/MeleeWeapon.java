package bullethell.items;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.Enemy;
import bullethell.Entity;
import bullethell.GameSolid;
import bullethell.Globals;
import bullethell.Player;

public class MeleeWeapon extends Weapon {

	public static final int DEFAULT_DMG = 40;
    public static final int DEFAULT_FIRE_TIME = 300 / Globals.TIMER_DELAY;
    public static final double DEFAULT_COVERAGE_ANGLE = 90;

    private double coverage = DEFAULT_COVERAGE_ANGLE;
    private AtkBox atkBox;
    
    public MeleeWeapon(String name) throws IOException {
        this(name, DEFAULT_DMG, DEFAULT_FIRE_TIME);
    }

    public MeleeWeapon(String name, int dmg, int fireTime) throws IOException {
        this(ImageIO.read(new File("Sprites/Item.png")), name, dmg, DEFAULT_FIRE_TIME);
    }

    public MeleeWeapon(BufferedImage sprite, String name) {
        this(sprite, name, DEFAULT_DMG, DEFAULT_FIRE_TIME);
    }

    public MeleeWeapon(BufferedImage sprite, String name, int dmg, int fireTime) {
        super(sprite, name, dmg, fireTime, 0, 0);
    }
    
    public AtkBox getAtkBox() { return atkBox; }

    @Override
    public void attack() {
        Player player = Player.get();
        if (!(player.getCurrentFire() >= getFireTime() && player.isAlive())) return;
        try {
            (new AtkBox(ImageIO.read(new File("Sprites/Slash.png")), new Rectangle(140, 0, 43, 162))).update();
            player.setCurrentFire(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MeleeWeapon clone() {
        MeleeWeapon obj = new MeleeWeapon(sprite, name, getDefaultWepDMG(), getDefaultFireTime());
        obj.coverage = coverage;
		obj.setLocation(getLocation());
		if (!isAlive()) {
            obj.kill();
        }
		obj.setEssential(isEssential());
        return obj;
    }

    @Override
    public void updateData() {
        List<String> data = new ArrayList<>();
        data.add(getName());
        data.add("   " + getWepDMG() + " damage");
        if (getManaCost() > 0) {
            data.add("   " + getManaCost() + " mana");
        } else {
            data.add("   " + "Restores mana");
        }
        data.add("   " + getFireTime() + " fire rate");
        data.add("   " + getShotSpeed() + " shot speed");
        setData(data.toArray(new String[0]));
    }

    public final class AtkBox extends Entity {
		
        private final int lifeSpan = 15;
        private final int maxHits = 2;
        private final int hitDelay = lifeSpan / maxHits;        

        private int age = 1;
        private int numHits = 0;
        private int lastHit = 0;

        public AtkBox(BufferedImage sprite, Rectangle bounds) {
            super(sprite, null, getWepDMG(), 0, 0, true);
            setHitbox(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
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
                Player.get().registerDealtDMG(dmg);
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