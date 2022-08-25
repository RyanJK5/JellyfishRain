package bullethell.items;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import bullethell.Player;
import bullethell.Projectile;
import bullethell.Spritesheet;
import bullethell.movement.AngledPath;
import bullethell.movement.Path;

public class MultiShotWeapon extends Weapon {
    
	private double maxSpread;
    private int shotNum;

    public MultiShotWeapon(BufferedImage sprite, String name, int dmg, int fireSpeed, double maxSpread, int shotNum, 
      int range, int manaCost) {
        super(sprite, name, dmg, fireSpeed, range, manaCost);
        this.maxSpread = maxSpread;
        if (shotNum <= 0) throw new IllegalArgumentException("shotNum must be greater than 0");
        if (maxSpread <= 0) throw new IllegalArgumentException("maxSpread must be greater than 0");
        this.shotNum = shotNum;
    }

    public MultiShotWeapon(String name, int dmg, int fireSpeed, double maxSpread, int shotNum, int range, int manaCost) 
      throws IOException {
        this(ImageIO.read(new File("Sprites/Item.png")), name, dmg, fireSpeed, maxSpread, shotNum, range, manaCost);
    }

    public MultiShotWeapon(BufferedImage sprite, String name, double maxSpread, int shotNum, int range) {
        this(sprite, name, DEFAULT_DMG, DEFAULT_FIRE_TIME, maxSpread, shotNum, range, 0);
    }

    public MultiShotWeapon(String name, double maxSpread, int shotNum) throws IOException {
        this(name, DEFAULT_DMG, DEFAULT_FIRE_TIME, maxSpread, shotNum, 0, 0);
    }

    @Override
    public void attack() {
        Player player = Player.get();
        if (!(player.getCurrentFire() >= getFireTime() && player.isAlive())) return;
        try {
            BufferedImage projSprite = ImageIO.read(new File("Sprites/FriendlyBullet.png"));
            int centerX = player.getCenterX() - projSprite.getWidth(null) / 2;
            int centerY = player.getCenterY() - projSprite.getHeight(null) / 2;
            
            centerX = player.getX() + player.getWidth() / 2 - projSprite.getWidth(null) / 2;
            centerY = player.getY() + player.getHeight() / 2 - projSprite.getHeight(null) / 2;
            
            double angle = Math.atan2(centerX - Player.cursorX(), centerY - Player.cursorY());
            angle = Math.toDegrees(angle);
            angle += 180;
            if (angle < 0) angle += 360;

            for (double i = -maxSpread / 2; i <= maxSpread / 2; i += maxSpread / (shotNum - 1)) {
                Path path = new AngledPath(angle + i);
                Projectile proj = new Projectile(Spritesheet.getSpriteSheet(projSprite), path, false, 0, getShotSpeed(), getWepDMG());
                proj.setLocation(centerX, centerY);
                proj.setFriendly(true);
                proj.setRange(getRange());
            }
            
            player.setCurrentFire(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MultiShotWeapon clone() {
        MultiShotWeapon obj = new MultiShotWeapon(sprite, name, getDefaultWepDMG(), getDefaultFireTime(), maxSpread, shotNum,
          getDefaultRange(), getManaCost());
		obj.setLocation(getLocation());
		if (!isAlive()) {
            obj.kill();
        }
		obj.setEssential(isEssential());
        return obj;
    }
}
