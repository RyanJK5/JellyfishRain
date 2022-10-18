package bullethell.combat.tags;

import java.awt.Color;
import java.awt.image.BufferedImage;

import bullethell.Globals;

public enum StatusEffectType {
    POISON(3, 500, 250),
    FREEZE(0, 1000, 0),
    SLOW_DOWN(0, 1000, 0),
    BLEED(200, 1000, 0);

    public final int defaultDPH;
    public final int defaultMiliDuration;
    public final int defaultMiliHitDelay;

    private static final BufferedImage iconSheet = Globals.getImage("StatusEffectIcons");

    private StatusEffectType(int defaultDPH, int defaultMiliDuration, int defaultMiliHitDelay) {
        this.defaultDPH = defaultDPH;
        this.defaultMiliDuration = defaultMiliDuration;
        this.defaultMiliHitDelay = defaultMiliHitDelay;
    }

    public static int getID(StatusEffectType type) {
        return Globals.indexOf(values(), type);
    }

    public static StatusEffectType getType(int id) {
        return values()[id];
    }

    public String futureTense() {
        switch (this) {
            case POISON:
                return "Inflicts poison on";
            case FREEZE:
                return "Freezes";
            case SLOW_DOWN:
                return "Slows down";
            case BLEED:
                return "Inflicts bleed on";
        }
        return "Inflicts ?";
    }

    public String presentTense() {
        switch (this) {
            case POISON:
                return "Poison";
            case FREEZE:
                return "Frozen";
            case SLOW_DOWN:
                return "Slowness";
            case BLEED:
                return "Bleed";
        }
        return "?";
    }

    public String pastTense() {
        switch (this) {
            case POISON:
                return "poisoned";
            case FREEZE:
                return "frozen";
            case SLOW_DOWN:
                return "slowed down";
            case BLEED:
                return "bleeding";
        }
        return "?ed";
    }

    public Color getColor() {
        switch (this) {
            case POISON:
                return new Color(4, 112, 0);
            case FREEZE:
                return new Color(33, 174, 255);
            case SLOW_DOWN:
                return new Color(149, 208, 252);
            case BLEED:
                return new Color(150, 12, 35);
        }
        return Globals.DEFAULT_COLOR;
    }

    public BufferedImage getIcon() {
        return iconSheet.getSubimage(Globals.indexOf(values(), this) * iconSheet.getWidth() / values().length, 0, 
          iconSheet.getWidth() / values().length, iconSheet.getHeight());
    }
}
