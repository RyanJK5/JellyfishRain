package bullethell.combat.tags;

import bullethell.enemies.Enemy;

public class Freeze extends StatusEffect {

    private int age;
    private final UnfreezeTag tag = new UnfreezeTag();

    public Freeze(int miliDuration) {
        super(StatusEffectType.FREEZE, 0, 0, miliDuration);
    }

    @Override
    public void apply(Enemy enemy) {
        if (age == 0) {
            enemy.paused = true;
            enemy.addTag(tag);
        }
        age++;
        if (!active()) {
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

    private class UnfreezeTag implements Tag {
        
        @Override
        public void apply(Enemy enemy) {
            enemy.paused = false;
        }

        @Override
        public boolean active() {
            return true;
        }

        @Override
        public TagActivationType getActivationType() {
            return TagActivationType.ON_HIT;
        }

        @Override
        public boolean oneTime() {
            return true;
        }

        @Override
        public boolean canStack() {
            return false;
        }
    }
}
