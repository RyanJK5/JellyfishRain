package bullethell.ui;

import java.awt.Color;

public class TextBubble extends Text {
    
    private static boolean negative = false;

    private Color color;
    private float rotDeg;
    private int timesPerformed;

    public TextBubble(String str, float rotDeg, Color color) {
        super(str, color);
        this.color = color;
        this.rotDeg = negative ? -rotDeg : rotDeg;
        negative = !negative;
    }

    @Override
    public void update(java.awt.Graphics g) {
        super.update(g);
        rotate(rotDeg);
        if (timesPerformed >= 20) {
            if (color.getAlpha() - 10 > 0) {
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() - 10);
                setColor(color);
            } else {
                permakill();
                return;
            }
        }
        timesPerformed++;
    }
}
