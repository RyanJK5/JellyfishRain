package bullethell.items.abilities;

import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public final class TeleportAbility extends Item {
    
    @Override
    protected void setValues() {
        id = ItemID.TP_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Teleport Ability";
        description = "Allows the wearer to teleport";
    }
}
