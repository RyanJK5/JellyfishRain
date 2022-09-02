package bullethell.items;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import bullethell.Player;
import bullethell.Trigger;

public final class ItemDrop extends Trigger {

    private static final List<ItemDrop> drops = new ArrayList<>();

    private static final int RANGE = 75;

    private final Item item;

    public ItemDrop(ItemID id, int count, int x, int y) {
        super(id.getSprite(), new Trigger.Type[] {Trigger.TARGET_IN_RANGE});
        this.item = ItemID.getItem(id).clone(count);
        setLocation(x, y);
        setRange(RANGE);
        setTarget(Player.get());

        for (ItemDrop obj : drops) {
            if (Point.distance(getCenterX(), getCenterY(), obj.getCenterX(), obj.getCenterY()) <= RANGE &&
              item.canStack && obj.item.canStack && item.equals(obj.item)) {
                obj.item.addFrom(0, item);
                permakill();
                return;
            }
        }
        drops.add(this);
    }

    @Override
    public void toGhost() {
        super.toGhost();
        drops.remove(this);
    }

    @Override
    protected void activate() {
        Player.get().getInventory().addItem(item);
        permakill();
    }

    @Override
    public void paint(Graphics g) {
        item.setLocation(x, y);
        item.paint(g);
    }
}