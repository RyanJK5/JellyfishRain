package bullethell.combat;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import bullethell.GameSolid;
import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.items.weapons.ExampleSword;
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

	public Projectile(Spritesheet spritesheet, Path path, boolean immortal, int lifeSpan, float speed, int dmg) {
		super(spritesheet, path, dmg, 0, speed, false);
		this.lifeSpan = lifeSpan;
		this.immortal = immortal;
		this.constDMG = dmg;

		setHitbox(new Polygon(new int[] {x, x + w / 2, x + w, x + w / 2},
		  new int[] {y + h / 2, y, y + h / 2, y + h},
		  4));
		getCurrentAnimation().start();
	}

	public Projectile(Spritesheet spritesheet, Path path, int speed, int dmg) {
		this(spritesheet, path, true, 0, speed, dmg);
	}
	
	public Projectile(Spritesheet spritesheet, Path path) {
		this(spritesheet, path, true, 0, DEFAULT_SPEED, DEFAULT_DMG);
	}
	
	public Projectile(Path path) {
		this(new Spritesheet(Globals.getImage("Bullet"), 1, 1), path, true, 0, DEFAULT_SPEED, DEFAULT_DMG);
	}

	public Projectile() {
		this(Path.DEFAULT_PATH);
	}

	private Projectile(Projectile proj) {
		super(proj);
		lifeSpan = proj.lifeSpan;
		immortal = proj.immortal;
		constDMG = proj.dmg;

		setHitbox(new Polygon(new int[] {x, x + w / 2, x + w, x + w / 2},
		  new int[] {y + h / 2, y, y + h / 2, y + h},
		  4));
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
		  (obj instanceof ExampleSword.ExampleSwordHitbox)) {
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
				Player.get().registerDealtDMG(dmg, this);
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
	public void update(Graphics g) {
		if (drawIndicator && (age < indicatorLifespan || indicatorLifespan == 0)) {
			g.setColor(new Color(128,128,128,50));
			if (range > 0) {
				getPath().drawIndicator(g, new Point(startX + w / 2, startY + h / 2), range);	
			} else {
				getPath().drawIndicator(g, new Point(startX + w / 2, startY + h / 2));
			}
		}
		if ((indicatorProjDelay == 0) || (age >= indicatorProjDelay)) {
			if (rotationDeg > 0 || opacity != 1f) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				g2.rotate(rotationDeg, rotationAnchor != null ? rotationAnchor.x : getCenterX(), rotationAnchor != null ? rotationAnchor.y : getCenterY());
				g2.drawImage(animations[currentAnimation].getFrame(), x, y, null);
				g2.dispose();
				return;
			}
			g.drawImage(animations[currentAnimation].getFrame(), x, y, null);
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
		return new Projectile(this);
	}

	public boolean equals(Projectile obj) {
		return obj != null && obj.getWidth() == getWidth() && 
		obj.getHeight() == getHeight() && isEssential() == obj.isEssential() && constDMG == obj.constDMG && maxHP == obj.maxHP;
	}

	@Override
	public void registerDMG(int dmg) {
		
	}
}