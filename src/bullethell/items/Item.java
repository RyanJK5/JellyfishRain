package bullethell.items;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import bullethell.GameObject;
import bullethell.Globals;
import bullethell.Player;

public class Item extends GameObject {
	protected String name;
    private String[] data;

    public Item(String name) throws IOException {
        this(ImageIO.read(new File("sprites/Item.png")), name);
    }

    public Item(BufferedImage sprite, String name) {
        super(sprite, true);
        Objects.requireNonNull(name);
        this.name = name;
        setData(new String[] {name});
    }

    public void updateData() {
        setData(new String[] {name});
    }

    public BufferedImage scaledSprite(float scale) {
        BufferedImage newSprite = new BufferedImage((int) (w * scale), (int) (h * scale), BufferedImage.TYPE_INT_ARGB);
        newSprite.getGraphics().drawImage(sprite, 0, 0, (int) (w * scale), (int) (h * scale), null);
        return newSprite;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (Player.cursorX() >= x && Player.cursorX() <= x + w &&
        Player.cursorY() >= y && Player.cursorY() <= y + h &&
        Player.get().getCursorSlot() == null) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] != null) {
                    g.drawString(data[i].toString(), Player.cursorX() + 10, Player.cursorY() + 20 + i * Globals.DEFAULT_FONT.getSize());
                }
            }
        }
    }

    public String[] getData() { return data;}
    public void setData(String[] data) { this.data = data; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        updateData();
    }

    public boolean equals(Item o) {
        if (o == null) return false;
        for (int i = 0; i < data.length; i++) {
            if (!o.getData()[i].equals(getData()[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Item clone() {
        Item obj = new Item(sprite, name);
		obj.setLocation(getLocation());
		obj.setData(getData());
        if (!isAlive()) {
            obj.kill();
        }
		obj.setEssential(isEssential());
        return obj;
    }

    @Override
    public void toGhost() { }

    @Override
    public void unghost() { }
}
