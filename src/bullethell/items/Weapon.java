package bullethell.items;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Projectile;
import bullethell.movement.AngledPath;
import bullethell.movement.Path;

public class Weapon extends Item {
    
    public static final int DEFAULT_DMG = 10;
    public static final int DEFAULT_FIRE_TIME = 100 / Globals.TIMER_DELAY;
    public static final float DEFAULT_SHOT_SPEED = 20;
    public static final int DEFAULT_MANA_COST = 0;

    private BufferedImage projSprite;

    private int dmg, defaultDMG;
    private int manaCost, defaultManaCost;
    private int fireTime, defaultFireTime;
    private int range, defaultRange;
    private float shotSpeed, defaultShotSpeed;

    public Weapon(String name) throws IOException {
        this(name, DEFAULT_DMG, DEFAULT_FIRE_TIME, 0);
    }

    public Weapon(String name, int dmg, int fireTime, int range) throws IOException {
        this(ImageIO.read(new File("Sprites/Item.png")), name, dmg, DEFAULT_FIRE_TIME, 0, 
            DEFAULT_MANA_COST);
    }

    public Weapon(BufferedImage sprite, String name)  {
        this(sprite, name, DEFAULT_DMG, DEFAULT_FIRE_TIME, 0, DEFAULT_MANA_COST);
    }

    public Weapon(BufferedImage sprite, String name, int dmg, int fireTime, int range, int manaCost) {
        super(sprite, name);
        this.dmg = dmg;
        this.fireTime = fireTime;
        this.range = range;
        this.shotSpeed = DEFAULT_SHOT_SPEED;
        this.manaCost = manaCost;

        defaultDMG = dmg;
        defaultFireTime = fireTime;
        defaultRange = range;
        defaultShotSpeed = shotSpeed;
        defaultManaCost = manaCost;
        try {
            projSprite = ImageIO.read(new File("Sprites/FriendlyBullet.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateData();
    }

    public Weapon(BufferedImage sprite, BufferedImage projSprite, String name, int dmg, int fireTime, int range, int manaCost) {
        this(sprite, name, dmg, fireTime, range, manaCost);
        this.projSprite = projSprite;
        updateData();
    }

    public void attack() { 
        Player player = Player.get();
        if (!(player.getCurrentFire() >= getFireTime() && player.isAlive())) return;
        int centerX = player.getCenterX() - projSprite.getWidth(null) / 2;
        int centerY = player.getCenterY() - projSprite.getHeight(null) / 2;
        
        centerX = player.getX() + player.getWidth() / 2 - projSprite.getWidth(null) / 2;
        centerY = player.getY() + player.getHeight() / 2 - projSprite.getHeight(null) / 2;
        
        double angle = Math.atan2(centerX - Player.cursorX(), centerY - Player.cursorY());
        angle = Math.toDegrees(angle);
        angle += 180;
        if (angle < 0) angle += 360;
        
        Path path = new AngledPath(angle);

        Projectile proj = new Projectile(projSprite, path, false, 0, shotSpeed, dmg);
        proj.rotate((float) -angle + 180);
        proj.setRange(range);
        proj.setLocation(centerX, centerY);
        proj.setFriendly(true);
        
        player.setCurrentFire(0);
    }

    public void resetStats() {
        dmg = defaultDMG;
        fireTime = defaultFireTime;
        range = defaultRange;
        shotSpeed = defaultShotSpeed;
    }

    @Override
    public void updateData() {
        List<String> data = new ArrayList<>();
        data.add(getName());
        data.add("   " + getWepDMG() + " damage");
        if (manaCost > 0) {
            data.add("   " + getManaCost() + " mana");
        }
        data.add("   " + getFireTime() + " fire rate");
        data.add("   " + getShotSpeed() + " shot speed");
        data.add("   " + (getRange() == 0 ? "Infinite range" : getRange() + " range"));

        setData(data.toArray(new String[0]));
    }

    public int getWepDMG() { return dmg; }
    public void setWepDMG(int dmg) { this.dmg = dmg; }

    public int getFireTime() { return fireTime; }
    public void setFireTime(int fireTime) { this.fireTime = fireTime; }

    public int getRange() { return range; } 
    public void setRange(int range) { this.range = range; }

    public float getShotSpeed() { return shotSpeed; }
    public void setShotSpeed(float shotSpeed) { this.shotSpeed = shotSpeed; }

    public int getManaCost() { return manaCost; }
    public void setManaCost(int manaCost) { this.manaCost = manaCost; }

    public int getDefaultWepDMG() { return defaultDMG; }
    public int getDefaultFireTime() { return defaultFireTime; }
    public int getDefaultRange() { return defaultRange; } 
    public float getDefaultShotSpeed() { return defaultShotSpeed; }
    public int getDefaultManaCost() { return defaultManaCost; }

    @Override
    public Weapon clone() {
        Weapon obj = new Weapon(sprite, projSprite, name, defaultDMG, defaultFireTime, defaultRange, manaCost);
		obj.setLocation(getLocation());
		if (!isAlive()) {
            obj.kill();
        }
		obj.setEssential(isEssential());
        return obj;
    }

    @Override
    public boolean equals(Item item) {
        return item instanceof Weapon wep && defaultDMG == wep.defaultDMG && defaultFireTime == wep.defaultFireTime &&
          defaultManaCost == wep.defaultManaCost && defaultRange == wep.defaultRange && defaultShotSpeed == wep.defaultShotSpeed;
    }
}