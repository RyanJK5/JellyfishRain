package bullethell.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import bullethell.Globals;

public class ToggleButton extends Button {
    
    private static final BufferedImage SWITCH = Globals.getImage("UISwitch");
    private static final BufferedImage OFF_SWITCH = SWITCH.getSubimage(0, 0, SWITCH.getWidth(), SWITCH.getHeight() / 2);
    private static final BufferedImage ON_SWITCH = SWITCH.getSubimage(0, SWITCH.getHeight() / 2, SWITCH.getWidth(), SWITCH.getHeight() / 2);

    private boolean active;
    private Field booleanRef;
    private Object objRef;

    public ToggleButton(String backSprite, int width, int height) {
        super(backSprite, width, height);
    }

    public ToggleButton(BufferedImage sprite) {
        super(sprite);
    }

    /**
     * @param booleanRef Reference to a boolean that will be altered based on the state of this switch.
     * @param obj The object used to access {@code booleanRef}, or null if it is a static field.
     */
    public ToggleButton(BufferedImage sprite, Field booleanRef, Object obj) {
        this(sprite);
        try {
            if (!(booleanRef.get(obj) instanceof Boolean)) {
                throw new IllegalArgumentException("Field must be a boolean");
            }
            this.booleanRef = booleanRef;
            objRef = obj;
            active = booleanRef.getBoolean(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        g.drawImage(active ? ON_SWITCH : OFF_SWITCH, x + w - ON_SWITCH.getWidth() - 20, y + h / 2 - ON_SWITCH.getHeight() / 2, null);
    }

    /**
     * Any subclass that overrides this method should invoke {@code super.activate()} before executing any other code.
     */
    @Override
    protected void activate() {
        active = !active;
        if (booleanRef != null) {
            try {
                booleanRef.setBoolean(objRef, active);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isActive() {
        return active;
    }
}
