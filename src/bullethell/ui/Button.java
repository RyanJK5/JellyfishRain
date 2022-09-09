package bullethell.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Trigger;

public abstract class Button extends Trigger {

    private Supplier<Boolean> altCondition;

    public Button(String backSprite, int width, int height) {
        super(null, 101, new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER, Trigger.LEFT_CLICK});
        BufferedImage sprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        sprite.getGraphics().drawImage(Globals.getImage(backSprite), 0, 0, width, height, null);
        setSprite(sprite);
    }

    public Button(BufferedImage sprite) {
        super(sprite, 101, new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER, Trigger.LEFT_CLICK});
    }

    public void setAltCondition(Supplier<Boolean> altCondition) {
        this.altCondition = altCondition;
    }
    public Supplier<Boolean> getAltCondition() { 
        return altCondition;
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        
        if (altCondition != null && altCondition.get()) {
            g.setColor(new Color(0, 0, 0, 0.5f));
            g.fillRect(x, y, w, h);
        }
        else if (getBounds().contains(Player.cursorX(), Player.cursorY())) {
            g.setColor(new Color(1, 1, 1, 0.5f));
            g.fillRect(x, y, w, h);
        }
    }
}
