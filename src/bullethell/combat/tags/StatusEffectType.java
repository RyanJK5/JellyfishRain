package bullethell.combat.tags;

import bullethell.Globals;
import java.awt.Color;

public enum StatusEffectType {
    POISON(3, 500, 250),
    FREEZE(0, 10000, 0);

    public final int defaultDPH;
    public final int defaultMiliDuration;
    public final int defaultMiliHitDelay;

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
                return "Inflicts poison";
            case FREEZE:
                return "Freezes";
        }
        return "Inflicts ?";
    }

    public String presentTense() {
        switch (this) {
            case POISON:
                return "Poison";
            case FREEZE:
                return "Frozen";
        }
        return "?";
    }

    public String pastTense() {
        switch (this) {
            case POISON:
                return "poisoned";
            case FREEZE:
                return "frozen";
        }
        return "?ed";
    }

    public Color getColor() {
        switch (this) {
            case POISON:
                return new Color(4, 112, 0);
            case FREEZE:
                return new Color(33, 174, 255);
        }
        return Globals.DEFAULT_COLOR;
    }
}
