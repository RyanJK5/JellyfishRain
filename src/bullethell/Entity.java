package bullethell;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import bullethell.movement.Path;

public non-sealed abstract class Entity extends GameSolid implements ActionListener {

	protected boolean drawIndicator;
    protected Path path;

    protected Animation[] animations;
    protected int currentAnimation;

    protected int dmg;
    protected int maxHP, hp;
    protected float speed;
    protected boolean friendly;
    protected boolean invincible;
    protected boolean ignoreSolids;

    protected Entity(Spritesheet spritesheet, Path path, int dmg, int maxHP, float speed, boolean friendly) {
        super(spritesheet.getSprite(0, 0));

        animations = Animation.getAnimations(spritesheet);

        this.path = path;
        this.dmg = dmg;
        this.maxHP = maxHP;
        hp = maxHP;
        this.speed = speed;
        this.friendly = friendly;

        Globals.GLOBAL_TIMER.addActionListener(this);
    }

    protected Entity(Entity entity) {
        super(entity);
        path = entity.path;
        dmg = entity.dmg;
        maxHP = entity.maxHP;
        hp = maxHP;
        speed = entity.speed;
        friendly = entity.friendly;
        animations = entity.animations;
        
        invincible = entity.invincible;
        drawIndicator = entity.drawIndicator;
        ignoreSolids = entity.ignoreSolids;

        setSprite(animations[0].getFrame());
        setHitbox(new java.awt.Rectangle(x, y, w, h));
    }

    public void move() {
    	Point newCoords = path.move(speed);
    	int newX = x + newCoords.x;
    	int newY = y + newCoords.y;
    	setLocation(newX, newY);
    }

    public abstract boolean onCollision(GameSolid obj);
    public abstract void update();
    public abstract void registerDMG(int dmg);
    public abstract Entity clone();

	@Override
    public final void actionPerformed(ActionEvent e) {
        update();
    }

    @Override
	public void paint(Graphics g) {
        if (drawIndicator) path.drawIndicator(g, getLocation());
		if (rotationDeg > 0 || opacity != 1f) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2.rotate(rotationDeg, getCenterX(), getCenterY());
			g2.drawImage(animations[currentAnimation].getFrame(), x, y, null);
			g2.dispose();
			return;
		}
		g.drawImage(animations[currentAnimation].getFrame(), x, y, null);
	}

    @Override
    @Deprecated
    public BufferedImage getSprite() { return super.getSprite(); }

    public void setAnimation(int index) {
        animations[currentAnimation].reset();
        currentAnimation = index;
        animations[currentAnimation].restart();
    }

    public Animation getAnimation(int index) {
        return animations[index];
    }

    public Animation getCurrentAnimation() {
        return animations[currentAnimation];
    }

    public void setAnimations(Spritesheet spritesheet) {

    }

    @Override
	public void toGhost() {
		super.toGhost();
        Globals.GLOBAL_TIMER.removeActionListener(this);
	}

	@Override
	public void unghost() {
		super.unghost();
        for (ActionListener al: Globals.GLOBAL_TIMER.getActionListeners()) {
            if (this == al) {
                return;
            }
        }
        Globals.GLOBAL_TIMER.addActionListener(this);
	}

    public Path getPath() { return path; }
    public void setPath(Path path) { this.path = path; }

    public int getDMG() { return dmg; }
    public void setDMG(int dmg) { this.dmg = dmg; }

    public int getHP() { return hp; }
    public void setHP(int hp) { this.hp = hp; }
    public void resetHP() { hp = maxHP; }
    public int getMaxHP() { return maxHP; }

    public void setSpeed(float speed) { this.speed = speed; }
    public float getSpeed() { return speed; }

    public boolean friendly() { return friendly; }
    public void setFriendly(boolean friendly) { this.friendly = friendly; }

    public boolean drawsIndicator() { return drawIndicator; }
    public void setDrawIndicator(boolean drawIndicator) { this.drawIndicator = drawIndicator; }

    public boolean isInvicible() { return invincible; }
    public void setInvincible(boolean invincible) { this.invincible = invincible; }

    public void setIgnoreSolids(boolean ignoreSolids) { this.ignoreSolids = ignoreSolids; }

    @Override
    public boolean readyToKill() { return hp <= 0; }

    public boolean equals(Entity ent) {
        return super.equals(ent) && dmg == ent.dmg && maxHP == ent.maxHP;
    }

    public static void removeAll(Predicate<Entity> condition) {
		if (condition == null) {
			condition = obj -> true;
		}

        List<Entity> toRemove = new ArrayList<>();
        for (int i = 0; i < solids.size(); i++) {
            GameSolid solid = solids.get(i);
            if (solid instanceof Entity ent && !ent.isEssential() && condition.test(ent)) {
                toRemove.add(ent);
            }
        }
        toRemove.forEach(obj -> obj.permakill());
    }
}
