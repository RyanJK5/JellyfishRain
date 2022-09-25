package bullethell;

import bullethell.enemies.Enemy;

public class StatusEffect {
    
    private int age;
    public final int dmg;
    public final int duration;
    public final int hitDelay;

    public StatusEffect(int dph, int miliHitDelay, int miliDuration) {
        dmg = dph;
        hitDelay = miliHitDelay / Globals.TIMER_DELAY;
        duration = miliDuration / Globals.TIMER_DELAY;
    }

    public boolean active() {
        return age < duration;
    }

    public void update(Enemy target) {
        if (age % hitDelay == 0) {
            target.registerDMG(dmg);
        }
        age++;
    }
}
