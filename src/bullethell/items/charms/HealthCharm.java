package bullethell.items.charms;

import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.Recipe;

public class HealthCharm extends Item {

    @Override
    protected void setValues() {
        id = ItemID.HEALTH_CHARM;
        equipType = EquipType.ACCESSORY;

        name = "Health Charm";
        description = "+100 HP";

        playerModifiers.pHP = 100;
    }

    @Override
    public void addRecipes() {
        new Recipe(new ItemID[] {ItemID.MAGIC_DUST}, id, new int[] {5}, 1);
    }
}
