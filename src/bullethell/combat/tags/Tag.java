package bullethell.combat.tags;

import bullethell.enemies.Enemy;

public interface Tag {

    void apply(Enemy enemy);

    boolean active();

    TagActivationType getActivationType();
}