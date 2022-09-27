package bullethell.combat;

import bullethell.Globals;

public enum StatusEffectType {
    POISON(3, 500, 250);

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

    public static StatusEffectType getEffect(int id) {
        return values()[id];
    }

    public String futureTense() {
        switch (this) {
            case POISON:
                return "Inflicts poison";
            default:
                return "Inflicts ?";
        }
    }

    public String pastTense() {
        switch (this) {
            case POISON:
                return "poisoned";
            default:
                return "?ed";
        }
    }
}
