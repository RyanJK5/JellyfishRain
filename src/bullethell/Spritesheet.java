package bullethell;

import java.awt.image.BufferedImage;

public class Spritesheet {
    
    private BufferedImage[][] sprites;
    private int spriteWidth, spriteHeight;

    // width = num of sprites across, height = num of sprites down
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
    
    public BufferedImage getSprite(int row, int column) {
        return sprites[column][row];
    }

    public int getSpriteWidth() { return spriteWidth; }
    public int getSpriteHeight() { return spriteHeight; }

    public int getWidth() { return sprites.length; }
    public int getHeight() { return sprites[0].length; }

    public static Spritesheet getSpriteSheet(String fileName) {
        return new Spritesheet(Globals.getImage(fileName), 1, 1);
    }

    public static Spritesheet getSpriteSheet(BufferedImage image) {
        return new Spritesheet(image, 1, 1);
    }
}
