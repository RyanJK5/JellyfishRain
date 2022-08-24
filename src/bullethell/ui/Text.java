package bullethell.ui;

import java.awt.Graphics2D;

import bullethell.GameObject;
import bullethell.Globals;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;

public class Text extends GameObject {

	private String str;
    private Font font;
    private Color color;

    public Text(String str) {
        this(str, Globals.DEFAULT_FONT);
    }

    public Text(String str, Font font) {
        this(str, font, Globals.DEFAULT_COLOR);
    }

    public Text(String str, Color color) {
        this(str, Globals.DEFAULT_FONT, color);
    }

    public Text(String str, Font font, Color color) {
        super(null, 98);
        this.str = str;
        this.font = font;
        this.color = color;
    }

    public void setFont(Font font) { this.font = font; }
    public void setText(String str) { this.str = str; }
    public void setColor(Color color) { this.color = color; }

    @Override
    public void paint(Graphics g) {
        g.setColor(color);
        g.setFont(font);
        if (rotationDeg > 0) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.rotate(rotationDeg, getCenterX(), getCenterY());
			g2.drawString(str, x, y);
			g2.dispose();
			return;
		}
		g.drawString(str, x, y);
    }
}
