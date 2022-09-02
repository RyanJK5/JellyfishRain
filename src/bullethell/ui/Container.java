package bullethell.ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bullethell.GameObject;
import bullethell.Player;
import bullethell.items.Item;

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
        
        boolean stackable = item != null && item.canStack;
        if (!stackable) {
            stackable = stackable();
        }
        boolean cursorStackable = player.getCursorSlot() == null || player.getCursorSlot().canStack;

        if (item != null) {
            if (taking && stackable) {
                if (player.getCursorSlot() != null && cursorStackable && player.getCursorSlot().equals(item)) {
                    player.getCursorSlot().addFrom(1, item);
                } else if (player.getCursorSlot() == null) {
                    player.select(item.take(1));
                }
            } else if (player.getCursorSlot() == null) {
                if (stackable) {
                    player.select(item.take(0));
                } else {
                    player.select(item);
                    item = null;
                }
            } else if (itemClass.isInstance(player.getCursorSlot())) {
                if (stackable && cursorStackable && item.equals(player.getCursorSlot())) {
                    item.addFrom(0, player.getCursorSlot());
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
                item = (T) player.getCursorSlot().take(0);
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
        return false;
    }

    private class ItemDrawing extends GameObject {
				
        Item item;
        
        ItemDrawing(Item item, float scaleFactor) {
            super(null, true);
            BufferedImage scaledImage = new BufferedImage((int) (item.getWidth() * scaleFactor), (int) (item.getHeight() * scaleFactor), 
              BufferedImage.TYPE_INT_ARGB);
            scaledImage.getGraphics().drawImage(item.getSprite(), 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
            setSprite(scaledImage);
            this.item = item;
        }
        
        @Override
        public void paint(Graphics g) {
            BufferedImage temp = item.getSprite();
            item.setSprite(getSprite());
            item.setLocation(x, y);
            item.paint(g);
            item.setSprite(temp);

        }
    }
}
