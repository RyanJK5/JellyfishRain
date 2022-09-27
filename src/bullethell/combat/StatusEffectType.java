package bullethell.combat;

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
