package bullethell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public sealed class GameSolid extends GameObject permits Entity {

	protected static final Quadtree<GameSolid> quadtree = new Quadtree<>(new Rectangle(Globals.WIDTH, Globals.HEIGHT));
	protected static final List<GameSolid> solids = new ArrayList<>();

	protected static final boolean showHitboxes = true;
	private Shape hitbox;

	public GameSolid(BufferedImage sprite) {
		this(sprite, 1);
	}
	
	public GameSolid(BufferedImage sprite, int layerNum) {
		super(sprite, layerNum);
		solids.add(this);
		hitbox = new Rectangle(x, y, w, h);
	}
	
	public GameSolid(BufferedImage sprite, boolean ghost) {
		super(sprite, ghost);
		solids.add(this);
		hitbox = new Rectangle(x, y, w, h);
	}

	protected GameSolid(GameSolid solid) {
		super(solid);
        solids.add(this);
		setHitbox(solid.getHitbox());
	}

	@Override
	public void toGhost() {
		super.toGhost();
		solids.remove(this);
	}

	@Override
	public void unghost() {
		super.unghost();
		if (!solids.contains(this)) {
			solids.add(this);
		}
	}

	public boolean readyToKill() { return false; }

	public Shape getHitbox() { return hitbox; }

	public final boolean collidedWith(GameSolid obj) {
		Area area1 = new Area(hitbox);
        Area area2 = new Area(obj.hitbox);
        area1.intersect(area2);
        return !(area1.equals(new Area()));
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (showHitboxes) paintHitbox(g);
	}

	protected final void paintHitbox(Graphics g) {
		g.setColor(Color.RED);
		((Graphics2D) g).draw(hitbox);
		g.setColor(Globals.DEFAULT_COLOR);
	}

	public void setHitbox(Shape hitbox) {
		this.hitbox = hitbox;
	}

	@Override
	public void rotate(float rotationDegrees) {
		super.rotate(rotationDegrees);
		Area area = new Area(hitbox);
		AffineTransform rotation = AffineTransform.getRotateInstance(
			Math.toRadians(rotationDegrees), getCenterX(), getCenterY());
		setHitbox(area.createTransformedArea(rotation));
	}

	@Override
	public void setLocation(int x, int y) {
		AffineTransform at = AffineTransform.getTranslateInstance(x - this.x, y - this.y);
		Area area = new Area(hitbox);
		setHitbox(area.createTransformedArea(at));
		super.setLocation(x, y);
	}
	
	public static void quadtreeCheck() {
		List<GameSolid> returnObjects = new ArrayList<>();

		for (int i = 0; i < solids.size(); i++) {
			GameSolid obj = solids.get(i);
			if (!obj.isAlive()) {
				continue;
			}
			returnObjects.clear();
			quadtree.retrieve(returnObjects, obj);

			for (int j = 0; j < returnObjects.size(); j++) {
				GameSolid oObj = returnObjects.get(j);
				
				if (!oObj.isAlive() || obj == oObj) {
					continue;
				}

				if (obj instanceof Player ent && oObj.getClass() == GameSolid.class) {
					ent.setHitbox(new Rectangle(ent.x, ent.y, ent.w, ent.h));
					if (ent.collidedWith(oObj)) {
						ent.onCollision(oObj);
					}
					ent.setHitbox(new Rectangle(
					  ent.x + ent.w / 5, ent.y + (int) (ent.h / 2 - (1f/6f) * ent.h), 
					  (int) (ent.w * 0.6f), (int) ((1f/3f) * ent.h)));
				}
				else if (obj.collidedWith(oObj)) {
					if (obj instanceof Entity ent) {
						ent.onCollision(oObj);
					}
				} 
			}
		}
	}

	public static int objectCount() {
		return solids.size();
	}
}