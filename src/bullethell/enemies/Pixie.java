package bullethell.enemies;

import bullethell.Globals;
import bullethell.Player;
import bullethell.items.ItemID;
import bullethell.items.ItemLoot;
import bullethell.movement.SeekingPath;

public final class Pixie extends Enemy {

    @Override
    public void setValues() {
        id = EnemyID.PIXIE;
        name = "Pixie";
        ignoreSolids = true;
        maxHP = 50;
        hp = 50;
        dmg = 50;
        speed = Globals.rand.nextInt(8, 12);
        setPath(new SeekingPath(this, Player.get()));
    }

    @Override
    protected void createLootTable() {
        lootTable = new ItemLoot[] {new ItemLoot(ItemID.MAGIC_DUST, 1, 5, 6)};
    }
    
}
