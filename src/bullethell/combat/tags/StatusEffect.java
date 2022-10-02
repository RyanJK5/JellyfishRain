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

    public StatusEffect(StatusEffectType type) {
        this.type = type;
        dmg = type.defaultDPH;
        hitDelay = type.defaultMiliHitDelay / Globals.TIMER_DELAY;
        duration = type.defaultMiliDuration / Globals.TIMER_DELAY;
        
    }

    public boolean active() {
        return age < duration;
    }

    @Override
    public void apply(Enemy target) {
        if (age % hitDelay == 0) {
            target.registerDMG(dmg);
        }
        age++;
    }

    @Override
    public TagActivationType getActivationType() {
        return TagActivationType.EVERY_TICK;
    }
}