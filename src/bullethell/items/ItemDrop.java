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

    public ItemDrop(Item item, int x, int y) {
        super(item.getSprite(), new Trigger.Type[] {Trigger.TARGET_IN_RANGE});
        this.item = item.clone();
        setLocation(x, y);
        setRange(RANGE);
        setTarget(Player.get());

        for (ItemDrop obj : drops) {
            if (Point.distance(getCenterX(), getCenterY(), obj.getCenterX(), obj.getCenterY()) <= RANGE &&
              this.item instanceof StackableItem stack && obj.item instanceof StackableItem oStack &&
              stack.equals(oStack)) {
                oStack.addFrom(0, stack);
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