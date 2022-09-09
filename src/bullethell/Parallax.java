package bullethell;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Parallax extends GameObject {
    
    private static final List<Parallax> parallaxes = new ArrayList<>();

    public Parallax(String fileName) {
        super(createSprite(Globals.getImage(fileName)));
        setLayer(0);
        setEssential(true);
        setLocation(-Globals.SCREEN_WIDTH / 2, -Globals.SCREEN_HEIGHT / 2);
        parallaxes.add(this);
    }

    private static BufferedImage createSprite(BufferedImage original) {
        BufferedImage finalBg = new BufferedImage(Globals.SCREEN_WIDTH + Globals.WIDTH, Globals.SCREEN_HEIGHT + Globals.HEIGHT, 
          BufferedImage.TYPE_INT_ARGB)
        ;
        finalBg.getGraphics().drawImage(original, 0, 0, finalBg.getWidth(), finalBg.getHeight(), null);
        return finalBg;
    }

    private static int lastXMove, lastYMove;
    private static int xMovesSinceLast, yMovesSinceLast;
    public static void updateParallaxes(int dx, int dy, boolean corrective) {
        int xChange = (int) (dx / 2f > 0 ? dx / 2f + 0.5f : dx / 2f - 0.5f);
        int yChange = (int) (dy / 2f > 0 ? dy / 2f + 0.5f : dy / 2f - 0.5f);
        
        if (!corrective) {
            lastXMove = xChange;
            lastYMove = yChange;
            xMovesSinceLast = 0;
            yMovesSinceLast = 0;
        } else {
            xMovesSinceLast += Math.abs(xChange);
            yMovesSinceLast += Math.abs(yChange);
            if (xMovesSinceLast > Math.abs(lastXMove)) {
                xChange = 0;
            }
            if (yMovesSinceLast > Math.abs(lastYMove)) {
                yChange = 0;
            }
        }

        if (xChange == 0 && yChange == 0) {
            return;
        }

        for (Parallax p : parallaxes) {
            int newX = p.x + xChange;
            int newY = p.y + yChange;
            p.setLocation(newX, newY);
        }
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
    }
}
