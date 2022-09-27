package bullethell.enemies;

import bullethell.movement.Path;

public final class Dummy extends Enemy {

    @Override
    protected void setValues() {
        maxHP = Integer.MAX_VALUE;
        hp = maxHP;
        path = Path.DEFAULT_PATH;
        name = "Dummy";
    }

    @Override
    protected void createLootTable() { }
}
