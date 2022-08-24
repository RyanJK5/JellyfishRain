package bullethell.items;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import bullethell.Player;

public class Ability extends Item {

    private static Ability
        TELEPORT,
        DASH,
        SLOW,
        HEAL
    ;
    
    private final Type type;


    private Ability(java.awt.image.BufferedImage sprite, String name, Type type) {
        super(sprite, name);
        this.type = type;
    }

    private Ability(String spriteName, String name, Type type) throws IOException {
        this(ImageIO.read(new File("Sprites\\" + spriteName + ".png")), name, type);
    }

    public static enum Type {
        TELEPORT, DASH, SLOW, HEAL;
    }
    
    public Type getType() {
        return type;
    }

    public boolean equals(Ability other) {
        return other != null && type == other.type;
    }

    public Ability clone() {
        return new Ability(sprite, name, type);
    }

    @Override
    public void paint(java.awt.Graphics g) {
        super.paint(g);
        if (type == Type.HEAL) {
            g.drawString(Integer.toString(Player.get().getHealNum()), x, y + h);
        }
    }

    public static Ability getAbility(Type type) {
        if (DASH == null) {
            declareAbilities();
        }

        switch (type) {
            case DASH:
                return DASH.clone();
            case SLOW:
                return SLOW.clone();
            case TELEPORT:
                return TELEPORT.clone();
            case HEAL:
                return HEAL.clone();
        }
        return null;
    }

    private static void declareAbilities() {
        try {
            DASH = new Ability("Item", "Dash Charm", Type.DASH);
            SLOW = new Ability("Item", "Slow Charm", Type.SLOW);
            TELEPORT = new Ability("Item", "Teleport Charm", Type.TELEPORT);
            HEAL = new Ability("Item", "Heal Charm", Type.HEAL);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
