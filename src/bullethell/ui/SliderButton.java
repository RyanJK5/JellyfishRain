package bullethell.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

import bullethell.Globals;
import bullethell.Player;

public class SliderButton extends Button implements MouseMotionListener {

    private static final BufferedImage SLIDER_SPRITE = Globals.getImage("UISlider");
    private static final BufferedImage SLIDER_KNOB = SLIDER_SPRITE.getSubimage(SLIDER_SPRITE.getWidth() - 29, 0, 29, 
      SLIDER_SPRITE.getHeight() / 2);
    private static final BufferedImage OFF_SLIDER = SLIDER_SPRITE.getSubimage(0, 0, 
      SLIDER_SPRITE.getWidth() - 29, SLIDER_SPRITE.getHeight() / 2);
    private static final BufferedImage ON_SLIDER = SLIDER_SPRITE.getSubimage(0, SLIDER_SPRITE.getHeight() / 2, 
      SLIDER_SPRITE.getWidth() - 29, SLIDER_SPRITE.getHeight() / 2);

    private Field floatRef;
    private Object objRef;
    private Consumer<Float> methodRef;

    private final Rectangle sliderBox;
    private float value = 0f;

    public SliderButton(String backSprite, int width, int height) {
        super(backSprite, width, height);
        sliderBox = new Rectangle(0, 0, SLIDER_KNOB.getWidth(), SLIDER_KNOB.getHeight());
		Globals.frame.addMouseMotionListener(this);
    }

    public SliderButton(BufferedImage sprite) {
        super(sprite);
        sliderBox = new Rectangle(0, 0, SLIDER_KNOB.getWidth(), SLIDER_KNOB.getHeight());
		Globals.frame.addMouseMotionListener(this);
    }

    /**
     * @param floatRef Reference to a float that will be altered based on the state of this switch.
     * @param obj The object used to access {@code floatRef}, or null if it is a static field.
     */
    public SliderButton(BufferedImage sprite, Field floatRef, Object obj) {
        this(sprite);
        try {
            if (!(floatRef.get(obj) instanceof Float)) {
                throw new IllegalArgumentException("Field must be a float");
            }
            this.floatRef = floatRef;
            objRef = obj;
            value = floatRef.getFloat(objRef);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public SliderButton(BufferedImage sprite, Consumer<Float> setMethod, Supplier<Float> getMethod) {
        this(sprite);
        this.methodRef = setMethod;
        value = getMethod.get();
    }

    @Override
    protected void activate() {
        tryMove();
    }

    @Override
    public void kill() {
        super.kill();
        Globals.frame.removeMouseMotionListener(this);
        sliderBox.setLocation(0, 0);
    }

    @Override
    public void revive() {
        super.revive();
        Globals.frame.addMouseMotionListener(this);
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        sliderBox.setLocation(sliderBox.x + x + w - 40, y + h / 2 - ON_SLIDER.getHeight() / 2);
        sliderBox.setLocation((int) (sliderBox.x - (1f - value) * ON_SLIDER.getWidth()), sliderBox.y);
    }

    @Override
    public void paint(Graphics g) {
        if (rotationDeg > 0) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.rotate(rotationDeg, getCenterX(), getCenterY());
			g2.drawImage(sprite, x, y, null);
			g2.dispose();
			return;
		}
		g.drawImage(sprite, x, y, null);
        
        String value = Integer.toString((int) (this.value * 100 + 0.5f));
        g.setColor(Globals.DEFAULT_COLOR);
        g.drawString(value + "%", x + w - OFF_SLIDER.getWidth() - 80, y + h / 2 + 10);

        if (getAltCondition() != null && getAltCondition().get()) {
            g.setColor(new Color(0, 0, 0, 0.5f));
            g.fillRect(x, y, w, h);
        }
        else if (getBounds().contains(Player.cursorX(), Player.cursorY())) {
            g.setColor(new Color(1, 1, 1, 0.5f));
            g.fillRect(x, y, w, h);
        }

        g.drawImage(OFF_SLIDER, x + w - OFF_SLIDER.getWidth() - 20, y + h / 2 - ON_SLIDER.getHeight() / 2, null);
        if (this.value > 0) {
            g.drawImage(ON_SLIDER, x + w - ON_SLIDER.getWidth() - 20, y + h / 2 - ON_SLIDER.getHeight() / 2,
              (int) (sliderBox.getCenterX() - (x + w - ON_SLIDER.getWidth() - 20)), sliderBox.height, null);
        }
        g.drawImage(SLIDER_KNOB, sliderBox.x, sliderBox.y, null);
    }

    public void mouseDragged(MouseEvent e) {
        tryMove();
    }

    private void tryMove() {
        if (new Rectangle(x + w - OFF_SLIDER.getWidth() - 20, y + h / 2 - ON_SLIDER.getHeight() / 2, OFF_SLIDER.getWidth(), OFF_SLIDER.getHeight())
          .contains(Player.cursorX(), Player.cursorY())) {
            sliderBox.setLocation(Player.cursorX() - sliderBox.width / 2, sliderBox.y);
            value = (float) ((sliderBox.getCenterX() - (x + w - OFF_SLIDER.getWidth() - 20)) / OFF_SLIDER.getWidth());
            if (value < 0) {
                value = 0;
            }
            if (value > 1) {
                value = 1;
            }

            if (floatRef != null) {
                try {
                    floatRef.setFloat(objRef, value);
                } catch (IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
            if (methodRef != null) {
                methodRef.accept(value);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) { }
}
