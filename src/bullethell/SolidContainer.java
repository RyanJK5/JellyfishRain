package bullethell;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import bullethell.items.Item;
import bullethell.ui.Container;
import bullethell.ui.Inventory;

public class SolidContainer<T extends Item> extends Container<T> {
    
	public static final int DEFAULT_RANGE = 200;
    private int range;
    private final GameSolid shadowSolid;

    public SolidContainer(BufferedImage sprite, T item) {
        this(sprite, item, DEFAULT_RANGE);
    }

    public SolidContainer(BufferedImage sprite, T item, int range) {
        super(sprite, item);
        this.range = range;   
        shadowSolid = new GameSolid(sprite);
        setLayer(1);
    }

    public SolidContainer(BufferedImage sprite, Class<T> itemClass) {
        this(sprite, itemClass, DEFAULT_RANGE);
    }

    public SolidContainer(BufferedImage sprite, Class<T> itemClass, int range) {
        super(sprite, itemClass);
        this.range = range;
        shadowSolid = new GameSolid(sprite);
        setLayer(1);
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        shadowSolid.setLocation(x, y);
    }

    @Override
    public void kill() {
        super.kill();
        shadowSolid.kill();
    }

    @Override
    public void revive() {
        super.revive();
        shadowSolid.revive();
    }

    /**
     * {@code moveItem(boolean)} should be called instead of this method.
     */
    @Override
    @Deprecated
    public boolean moveItem(boolean taking, Inventory<? super T> inventory) {
        return moveItem(taking);
    }

    @Override
	public void toGhost() {
		super.toGhost();
        shadowSolid.toGhost();
	}

	@Override
	public void unghost() {
		super.unghost();
        shadowSolid.unghost();
	}

    public boolean moveItem(boolean taking) {
        if (inRange(Player.get())) {
            super.moveItem(taking, null);
        }
        return inRange(Player.get());
    }

    private boolean inRange(GameObject obj) {
        return Point.distance(getCenterX(), getCenterY(), obj.getCenterX(), obj.getCenterY()) <= range;
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        if (GameSolid.showHitboxes) shadowSolid.paintHitbox(g);
    }

    public void setHitbox(Shape hitbox) {
        shadowSolid.setHitbox(hitbox);
    }

}
