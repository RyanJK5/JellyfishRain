package bullethell;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class GameObject implements Cloneable {
	
	/**
	 * If a class that extends {@code GameObject} contains fields that reference another {@code GameObject}, that object
	 * must be added to this list for proper serialization to occur.
	 */
	protected BufferedImage sprite;
	
	protected int lastX, lastY;
	protected int x, y, w, h;
	
	protected boolean isAlive = true;
	protected boolean essential = false;
	protected boolean alwaysDraw = false;
	
	protected int layerNumber;
	protected float rotationDeg;
	protected float opacity = 1;
	protected Point rotationAnchor;
	
	private List<Trigger> triggers = new ArrayList<>();

	protected static final List<List<GameObject>> layers = new ArrayList<>();

	protected GameObject() {
		this(new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB), 1, false);
	}

	public GameObject(String filePath) throws IOException {
		this(ImageIO.read(new File(filePath)));
	}

	public GameObject(BufferedImage sprite) {
		this(sprite, 1, false);
	}
	
	public GameObject(BufferedImage sprite, int layerNum) {
		this(sprite, layerNum, false);
	}
	
	public GameObject(BufferedImage sprite, boolean ghost) {
		this(sprite, 0, ghost);
	}

	protected GameObject(GameObject obj) {
		x = obj.x;
		y = obj.y;
        isAlive = obj.isAlive;
        essential = obj.essential;
		setLayer(obj.getLayer());
	}

	private GameObject(BufferedImage sprite, int layerNum, boolean ghost) {
		setSprite(sprite);

		x = 0;
		y = 0;

		if (!ghost) {
			for (int i = 0; i <= layerNum; i++) {
				if (i >= layers.size()) {
					layers.add(new ArrayList<>());
				}
			}
			layers.get(layerNum).add(this);
			layerNumber = layerNum;
		}
	}
	
	public void addTrigger(Trigger trigger) {
		trigger.setSprite(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
		trigger.setLocation(x, y);
		trigger.addCondition((obj) -> isAlive());
		triggers.add(trigger);
	}

	public boolean outOfBounds() {
		return x + w < 0 || 
			   y + h < 0 || 
			   x > Globals.WIDTH || 
			   y > Globals.HEIGHT;
	}
	
	public boolean outOfScreenBounds() {
		return x + w < Player.cameraX() ||
			   y + h < Player.cameraY() ||
			   x > Player.cameraX() + Globals.NATIVE_SCREEN_WIDTH ||
			   y > Player.cameraY() + Globals.NATIVE_SCREEN_HEIGHT;
	}
	
	public static void updateAll(Graphics g) {
		GameSolid.quadtree.clear();
		
		for (int i = 0; i < layers.size(); i++) {
			for (int j = 0; j < layers.get(i).size(); j++) {
				GameObject obj = layers.get(i).get(j);
				if (obj.isAlive()) {
					if (obj instanceof GameSolid solid) {
						GameSolid.quadtree.insert(solid);
					}
					if (!obj.outOfScreenBounds() || obj.alwaysDraw()) {
						obj.update(g);
					}
					g.setFont(Globals.DEFAULT_FONT);
					g.setColor(Globals.DEFAULT_COLOR);
				}
			}
		}
	}

	public void update(Graphics g) {
		if (rotationDeg > 0 || opacity != 1f) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2.rotate(rotationDeg, rotationAnchor != null ? rotationAnchor.x : getCenterX(), rotationAnchor != null ? rotationAnchor.y : getCenterY());
			g2.drawImage(sprite, x, y, null);
			g2.dispose();
			return;
		}
		g.drawImage(sprite, x, y, null);
	}

	public static int objectCount() {
		int result = 0;
		for (int i = 0; i < layers.size(); i++) {
			for (int j = 0; j < layers.get(i).size(); j++) {
				if (layers.get(i).get(j) != null) result++;
			}
		}
		return result;
	}

	public Rectangle toRect() {
		return new Rectangle(x, y, w, h);
	}

	public void kill() { isAlive = false; }
	public final void permakill() {
		kill();
		if (!isEssential()) {
			toGhost();
		}
	}

	public void rotate(float rotationDegrees) {
		if (rotationDegrees < 0) {
			rotationDegrees += 360;
		}
		rotationDeg += Math.toRadians(rotationDegrees);
	}

	public void setRotationAnchor(int x, int y) {
		this.rotationAnchor = new Point(x, y);
	}

	public void setOpacity(float opacity) {
		if (opacity < 0f || opacity > 1f) {
			throw new IllegalArgumentException("opacity must be between 0 and 1");
		}
		this.opacity = opacity;
	}
	
	public boolean isGhost() {
		return layers.stream().anyMatch((list) -> list.contains(this));
	}

	public void toGhost() {
		if (layerNumber < layers.size()) {
			layers.get(layerNumber).remove(this);
		}
	}

	public void unghost() {
		setLayer(layerNumber);
	}

	public void revive() { isAlive = true; }
	
	public boolean isAlive() { return isAlive;}

	public int getLayer() { return layerNumber; }
	
	public int getWidth() { return w; }
	
	public int getHeight() { return h; }
	
	public Rectangle getBounds() { return new Rectangle(x, y, w, h); }
	public int getX() { return x; }
	public int getCenterX() { return x + w / 2;}

	public int getY() { return y; }
	public int getCenterY() { return y + w / 2; }
	
	public boolean isEssential() { return essential; }
	public void setEssential(boolean essential) { this.essential = essential; }
	
	public boolean alwaysDraw() { return alwaysDraw; }
	public void setAlwaysDraw(boolean alwaysDraw) { this.alwaysDraw = alwaysDraw; }

	public void setLayer(int layerNum) { 
		for (int i = layers.size(); i <= layerNum; i++) {
			layers.add(new ArrayList<>());
		}
		for (int i = 0; i < layers.size(); i++) {
			layers.get(i).remove(this);
		}

		layers.get(layerNum).add(this);
		layerNumber = layerNum;
	}
	
	public void setLocation(int x, int y) {
		lastX = this.x;
		lastY = this.y;
		this.x = x;
		this.y = y;
		for (GameObject obj : triggers) {
			if (obj instanceof Trigger trig) {
				trig.setLocation(x, y);
			}
		}
	}

	public final void setLocation(Point p) {
		setLocation(p.x, p.y);
	}

	public void setSprite(BufferedImage newSprite) {
		if (newSprite == null) {
			sprite = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		} else {
			sprite = newSprite;
		}
		w = sprite.getWidth();
		h = sprite.getHeight();
	}
	public BufferedImage getSprite() { return sprite; }

	public Point getLocation() { return new Point(x, y); }

	public GameObject clone() {
		return new GameObject(this);
	}

	public boolean equals(GameObject obj) {
		return obj != null && obj.getWidth() == getWidth() && 
		obj.getHeight() == getHeight() && isEssential() == obj.isEssential();
	}

	@Override
	public String toString() {
		return "{" + this.getClass() + ", x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ", alive=" + isAlive + "}";
	}
}