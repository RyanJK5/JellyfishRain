package bullethell;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class Spritesheet {
    
    private BufferedImage[][] sprites;
    private int spriteWidth, spriteHeight;

    /**
     *  @param width num of sprites across
     *  @param height num of sprites down
     */
     public Spritesheet(BufferedImage spritesheet, int width, int height) {
        sprites = new BufferedImage[width][height];
        spriteWidth = spritesheet.getWidth() / width;
        spriteHeight = spritesheet.getHeight() / height;
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                sprites[i][j] = spritesheet.getSubimage(i * spriteWidth, j * spriteHeight, spriteWidth, spriteHeight);
            } 
        }
    }

    /**
     * @param bounds each {@code Dimension} in this array should represent the size of the sprite at that position in the spritesheet 
     * (i.e. {@code bounds[2][3]} would equate to frame 4 of row 3). 
     */
    public Spritesheet(BufferedImage spritesheet, Dimension[]... bounds) {
        if (bounds.length == 0) {
            bounds = new Dimension[][] {
                { new Dimension(spritesheet.getWidth(), spritesheet.getHeight()) }
            };
        }
        sprites = new BufferedImage[bounds.length][bounds[0].length];

        int xPos = 0;
        int yPos = 0;
        for (int i = 0; i < bounds.length; i++) {
            int largestHeight = 0;
            for (int j = 0; j < bounds[i].length; j++) {
                if (bounds[i][j].height > largestHeight) {
                    largestHeight = bounds[i][j].height;
                }
                try {
                    sprites[i][j] = spritesheet.getSubimage(xPos, yPos, bounds[i][j].width, bounds[i][j].height);
                } catch (java.awt.image.RasterFormatException e) {
                    throw new IllegalArgumentException("in bounds[" + i + "][" + j + "], current x position (" + xPos +  ") + " + 
                    bounds[i][j].width + " is out of bounds (" + spritesheet.getWidth() + ")");
                }
                xPos += bounds[i][j].width;
            }
            xPos = 0;
            yPos += largestHeight;
        }
    }

    public Spritesheet(String fileName, Dimension[]... bounds) {
        this(Globals.getImage(fileName), bounds);
    }
    
    public BufferedImage getSprite(int row, int column) {
        return sprites[row][column];
    }

    public int getSpriteWidth() { return spriteWidth; }
    public int getSpriteHeight() { return spriteHeight; }

    public int getWidth() { return sprites[0].length; }
    public int getHeight() { return sprites.length; }

    public static Spritesheet getSpriteSheet(String fileName) {
        return new Spritesheet(Globals.getImage(fileName), 1, 1);
    }

    public static Spritesheet getSpriteSheet(BufferedImage image) {
        return new Spritesheet(image, 1, 1);
    }
}
