package bullethell.items;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class StackableItem extends Item {

	private int count;
    private int capacity;

    public StackableItem(BufferedImage sprite, String name) {
        this(sprite, name, 0);
    }

    public StackableItem(BufferedImage sprite, String name, int capacity) {
        super(sprite, name);
        this.capacity = capacity;
    }

    public StackableItem(StackableItem old, int count) {
        this(old.sprite, old.name, old.capacity);
        this.count = count;
    }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawString(Integer.toString(count), x, y + h);
    }

    public StackableItem take(int num) {
        if (num < 0) {
            throw new IllegalArgumentException("num must be >0");
        }

        if (num == 0 || num > count) {
            int origCount = count;
            count = 0;
            kill();
            return new StackableItem(this, origCount);
        }

        count -= num;
        if (count <= 0) {
            kill();
        }
        return new StackableItem(this, num);
    }

    public void add(int num) {
        count += num;
    }

    public boolean addFrom(int num, StackableItem item) {
        int count = num == 0 ? item.getCount() : num;
        
        add(count);
        item.take(count);
        return true;
    }

    @Override
    public StackableItem clone() {
        StackableItem obj = new StackableItem(sprite, name, capacity);
		obj.setLocation(getLocation());
		if (!isAlive()) {
            obj.kill();
        }
		obj.setEssential(isEssential());
        obj.add(getCount());
        
        return obj;
    }
}
