package bullethell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.items.MeleeWeapon;
import bullethell.movement.Path;

public class Projectile extends Entity {
	
	public static final int DEFAULT_SPEED = 20;
	public static final int DEFAULT_DMG = 50;
	
	private int startX, startY;

	protected boolean immortal;
	protected boolean undeletable = false;
	
	protected int indicatorLifespan = 0;
	protected int indicatorProjDelay = 0;
	
	protected int lifeSpan;
	protected int age = 1; 
	protected double distanceTraveled = 0;
	
	protected int constDMG;
	protected int range = 0;
	protected int pierce = 0;
	protected List<Entity> hits = new ArrayList<>();

	protected boolean indicatorPortion = false;

	public Projectile(BufferedImage sprite, Path path, boolean immortal, int lifeSpan, float speed, int dmg) {
		super(sprite, path, dmg, 0, speed, false);
		this.lifeSpan = lifeSpan;
		this.immortal = immortal;
		this.constDMG = dmg;
		this.dmg = 0;

		setHitbox(new Polygon(new int[] {x, x + w / 2, x + w, x + w / 2},
		  new int[] {y + h / 2, y, y + h / 2, y + h},
		  4));
	}

	public Projectile(BufferedImage sprite, Path path, int speed, int dmg) {
		this(sprite, path, true, 0, speed, dmg);
	}
	
	public Projectile(BufferedImage sprite, Path path) {
		this(sprite, path, true, 0, DEFAULT_SPEED, DEFAULT_DMG);
	}
	
	public Projectile(Path path) throws IOException {
		this(ImageIO.read(new File("Sprites/Bullet.png")), path, true, 0, DEFAULT_SPEED, DEFAULT_DMG);
	}
	
	public Projectile() throws IOException {
		this(ImageIO.read(new File("Sprites/Bullet.png")), Path.DEFAULT_PATH, true, 0, DEFAULT_SPEED, DEFAULT_DMG);
	}

	@Override
	public void move() {
		Point newCoords = path.move(speed);
    	int newX = x + newCoords.x;
    	int newY = y + newCoords.y;
		distanceTraveled += Point.distance(newX, newY, x, y);
		setLocation(newX, newY);
	}

	@Override
	public void update() {
		age++;
		if ((indicatorProjDelay != 0 && age < indicatorProjDelay) || !isAlive()) {
			return;
		} else {
			dmg = constDMG;
		}
		move();
		if (readyToKill()) {
			permakill();
		}
	}
	
	@Override
	public boolean readyToKill() {
		return !undeletable && (!immortal && outOfScreenBounds()) || 
			   (age >= lifeSpan && lifeSpan > 0) ||
			   (outOfScreenBounds() && outOfBounds()) ||
			   (distanceTraveled >= range && range > 0);
	}

	@Override
	public boolean onCollision(GameSolid obj) {
		if (pierce < 0) {
			permakill();
			return false;
		}
		
		if ((indicatorProjDelay != 0 && age < indicatorProjDelay) ||
		  (obj instanceof Projectile) ||
		  (obj instanceof MeleeWeapon.AtkBox)) {
			return false;
		}

		if (obj instanceof GameSolid && !(obj instanceof Entity)) {
			permakill();
			return false;
		}

		Entity entity = (Entity) obj;
		boolean successful = false;
		for (Entity hit : hits) {
			if (entity == hit) {
				successful = true;
				break;
			}
		}
		if (friendly() != entity.friendly() && !successful && !entity.isInvicible()) {
			if (pierce != Integer.MAX_VALUE) {
				pierce--;
			}
			entity.registerDMG(dmg);
			if (friendly()) {
				Player.get().registerDealtDMG(dmg);
			}
			hits.add(entity);

			if (pierce < 0) {
				permakill();
				return false;
			}
		}
		return !successful;
	}
	
	@Override
	public void paint(Graphics g) {
		if (drawIndicator && (age < indicatorLifespan || indicatorLifespan == 0)) {
			g.setColor(new Color(128,128,128,50));
			if (range > 0) {
				getPath().drawIndicator(g, new Point(startX + w / 2, startY + h / 2), range);	
			} else {
				getPath().drawIndicator(g, new Point(startX + w / 2, startY + h / 2));
			}
		}
		if ((indicatorProjDelay == 0) || (age >= indicatorProjDelay)) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.rotate(rotationDeg, getCenterX(), getCenterY());
			g2.drawImage(sprite, x, y, null);
			g2.dispose();
		}
	}

	@Override
	public void setLocation(int x, int y) {
		if (this.x == 0 && this.y == 0) {
			startX = x;
			startY = y;	
		}
		AffineTransform at = AffineTransform.getTranslateInstance(x - this.x, y - this.y);
		Area area = new Area(getHitbox());
		area.transform(at);
		setHitbox(area);
		this.x = x;
		this.y = y;
	}
	
	public int getLifeSpan() { return lifeSpan; }
	public void setLifeSpan(int lifeSpan) { this.lifeSpan = lifeSpan; }

	public double distanceTraveled() { return distanceTraveled; } 

	public int getRange() { return range; }
	public void setRange(int range) { this.range = range; }

	public void setImmortal(boolean immortal) { this.immortal = immortal; }

	public void setIndicatorLifespan(int lifeSpan) { this.indicatorLifespan = lifeSpan; }
	public int getIndicatorLifespan() { return indicatorLifespan; }

	public void setIndicatorDelay(int indicatorProjDelay) { 
		this.indicatorProjDelay = indicatorProjDelay;
		indicatorPortion = true;
	}
	public int getIndicatorDelay() { return indicatorProjDelay; }

	public void setPierce(int pierce) { this.pierce = pierce; }
	public int getPierce() { return pierce; }

	public Projectile clone() {
		return new Projectile(sprite, path, immortal, lifeSpan, speed, constDMG);
	}

	public boolean equals(Projectile obj) {
		return obj != null && obj.getWidth() == getWidth() && 
		obj.getHeight() == getHeight() && isEssential() == obj.isEssential() && constDMG == obj.constDMG && maxHP == obj.maxHP;
	}

	@Override
	public void registerDMG(int dmg) {
		
	}
}