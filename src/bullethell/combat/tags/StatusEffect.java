package bullethell.combat.tags;

import bullethell.Globals;
import bullethell.enemies.Enemy;

public class StatusEffect implements Tag {
    
    private int age;
    
    public final StatusEffectType type;
    public final int dmg;
    public final int duration;
    public final int hitDelay;

    public StatusEffect(StatusEffectType type, int dph, int miliHitDelay, int miliDuration) {
        this.type = type;
        dmg = dph;
        hitDelay = miliHitDelay / Globals.TIMER_DELAY;
        duration = miliDuration / Globals.TIMER_DELAY;
    }

    public static StatusEffect getStatusEffect(StatusEffectType type) {
        switch (type) {
            case FREEZE:
                return new Freeze(type.defaultMiliDuration);
            case BLEED:
                return new Bleed(type.defaultMiliDuration, type.defaultDPH, 10);
            default:
                return new StatusEffect(type, type.defaultDPH, type.defaultMiliHitDelay, type.defaultMiliDuration);
        }
    }

    public boolean active() {
        return age < duration;
    }

    @Override
    public void apply(Enemy target) {
        if (age % hitDelay == 0) {
            target.registerDMG(dmg, type);
        }
        age++;
    }

    @Override
    public TagActivationType getActivationType() {
        return TagActivationType.EVERY_TICK;
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public boolean canStack() {
        return true;
    }
}
