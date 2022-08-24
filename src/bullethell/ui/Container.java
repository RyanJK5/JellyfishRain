package bullethell.ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bullethell.GameObject;
import bullethell.Globals;
import bullethell.Player;
import bullethell.items.Ability;
import bullethell.items.Item;
import bullethell.items.StackableItem;

public class Container<T extends Item> extends GameObject {
    
	public static final List<Container<?>> containers = new ArrayList<>();

    private final Class<T> itemClass;
    private T item;

    @SuppressWarnings("unchecked")
    public Container(BufferedImage sprite, T item) {
        super(sprite, 99);
        
        Objects.requireNonNull(item);
        this.item = item;
        this.itemClass = (Class<T>) item.getClass();
        containers.add(this);
    }

    /**
     * @param itemClass The generic type for this container as a class variable.
     */
    public Container(BufferedImage sprite, Class<T> itemClass) {
        super(sprite, 99);
        this.itemClass = itemClass;
        item = null;
        containers.add(this);   
    }

    /**
     * @param itemClass The generic type for this container as a class variable.
     */
    public Container(BufferedImage sprite, Class<T> itemClass, boolean ghost) {
        super(sprite, ghost);
        this.itemClass = itemClass;
        item = null;
        if (!ghost) {
            containers.add(this);
        }
    }

    public T getItem() { return item; }

    public void setItem(T item) {
        this.item = item;
    }

    public static boolean overContainer(int x, int y) {
       
        return containers.stream().noneMatch((cont) -> 
          new Rectangle(cont.x, cont.y, cont.w, cont.h).contains(x, y));
    }

    public Class<T> getItemClass() { return itemClass; }

    @SuppressWarnings("unchecked")
    public boolean moveItem(boolean taking, Inventory<? super T> inventory) {
        Player player = Player.get();
        
        boolean stackable = item != null && item instanceof StackableItem;
        if (!stackable) {
            stackable = stackable();
        }
        boolean cursorStackable = player.getCursorSlot() instanceof StackableItem || player.getCursorSlot() == null;

        if (item != null) {
            if (taking && stackable) {
                if (player.getCursorSlot() != null && cursorStackable) {
                    ((StackableItem) player.getCursorSlot()).addFrom(1, (StackableItem) item);
                } else if (player.getCursorSlot() == null) {
                    player.select(((StackableItem) item).take(1));
                }
            } else if (player.getCursorSlot() == null) {
                if (stackable) {
                    player.select(((StackableItem) item).take(0));
                } else {
                    player.select(item);
                    item = null;
                }
            } else if (itemClass.isInstance(player.getCursorSlot())) {
                if (stackable && cursorStackable && item.equals(player.getCursorSlot())) {
                    ((StackableItem) item).addFrom(0, (StackableItem) player.getCursorSlot());
                } else if (inventory == null) {
                    T temp = (T) player.getCursorSlot();
                    player.select(item);
                    item = temp;
                } else {
                    inventory.addItem((T) player.getCursorSlot());
                    player.select(null);
                }
            }
        } else if (!taking && player.getCursorSlot() != null && itemClass.isInstance(player.getCursorSlot())) {
            if (cursorStackable) {
                item = (T) ((StackableItem) player.getCursorSlot()).take(0);
            } else {
                item = (T) player.getCursorSlot();
                player.select(null);
            }
        }

        if (item != null && !item.isAlive()) {
            item = null;
        }
        if (player.getCursorSlot() != null && !player.getCursorSlot().isAlive()) {
            player.select(null);
        }

        return true;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (item != null) {
            if (item.isAlive()) {
                final float scaleFactor = (float) w / (float) item.getWidth();
                ItemDrawing drawing = new ItemDrawing(item, scaleFactor);
                drawing.setLocation((int) (x + w / 2 - scaleFactor * (item.getWidth() / 2)),
                    (int) (y + h / 2 - scaleFactor * (item.getHeight() / 2)));
                drawing.paint(g);
            } else {
                item = null;
            }
        }
    }

    public boolean equals (Container<T> obj) {
        return super.equals(obj) && ((item == null && obj.getItem() == null) || item.equals(obj.getItem()));
    }

    public final boolean stackable() {
        Class<? super T> stackableClass = itemClass;
        while (stackableClass != Object.class) {
            stackableClass = stackableClass.getSuperclass();
            if (stackableClass.equals(StackableItem.class)) {
                return true;
            }
        }
        return false;
    }

    private class ItemDrawing extends GameObject {
				
        Item item;
        
        ItemDrawing(Item item, float scaleFactor) {
            super(item.scaledSprite(scaleFactor), true);
            this.item = item;
        }
        
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (item instanceof StackableItem stack) {
                g.drawString(Integer.toString(stack.getCount()), x, y + h);
            }
            else if (item.equals(Ability.getAbility(Ability.Type.HEAL))) {
                g.drawString(Integer.toString(Player.get().getHealNum()), x, y + h);
            }

            if (Player.cursorX() >= x && Player.cursorX() <= x + w &&
            Player.cursorY() >= y && Player.cursorY() <= y + h &&
            Player.get().getCursorSlot() == null) {
                for (int i = 0; i < item.getData().length; i++) {
                    if (item.getData()[i] != null) {
                        g.drawString(item.getData()[i].toString(), Player.cursorX() + 10, 
                          Player.cursorY() + 20 + i * Globals.DEFAULT_FONT.getSize());
                    }
                }
            }
        }
    }
}
