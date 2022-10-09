package bullethell.combat.tags;

import java.util.ArrayList;
import java.util.List;

import bullethell.enemies.Enemy;

public class Bleed extends StatusEffect {

    private int age;
    public final int procCount;

    public Bleed(int miliDuration, int dph, int procCount) {
        super(StatusEffectType.BLEED, dph, 0, miliDuration);
        this.procCount = procCount;
    }

    @Override
    public void apply(Enemy enemy) {
        if (age == 0) {
            int totalBleeds = 0;
            for (int i = 0; i < enemy.tags.size(); i++) {
                if (enemy.tags.get(i) instanceof Bleed) {
                    totalBleeds++;
                }
            }
            if (totalBleeds >= procCount) {
                enemy.registerDMG(dmg, type);
                
                List<Tag> toRemove = new ArrayList<>();
                int numDone = 0;
                for (Tag tag : enemy.tags) {
                    if (tag instanceof Bleed && tag != this) {
                        toRemove.add(tag);
                        numDone++;
                        if (numDone >= procCount) {
                            break;
                        }
                    }
                }
                enemy.tags.removeAll(toRemove);
                
                totalBleeds -= procCount;
            }
        }
        age++;
    }

    @Override
    public boolean active() {
        return age < duration;
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public boolean canStack() {
        return true;
    }

    @Override
    public TagActivationType getActivationType() {
        return TagActivationType.EVERY_TICK;
    }
    
}
