package bullethell.combat.tags;

import bullethell.enemies.Enemy;

public class SlowDown extends StatusEffect {

    private int age;

    public SlowDown(int miliDuration) {
        super(StatusEffectType.SLOW_DOWN, 0, 0, miliDuration);
    }
    
    @Override
    public void apply(Enemy enemy) {
        age++;
        if (active()) {
            enemy.paused = !enemy.paused;
        } else {
            enemy.paused = false;
        }
    }

    @Override
    public boolean active() {
        return age < duration;
    }
    
    @Override
    public boolean canStack() {
        return false;
    }
}
