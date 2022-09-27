package bullethell.combat;

import java.util.function.Predicate;

import bullethell.enemies.Enemy;

public interface WeaponEntity {
    Predicate<Enemy> getCritCondition();
}
