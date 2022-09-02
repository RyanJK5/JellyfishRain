package bullethell.items.abilities;

import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public final class DashAbility extends Item {

    @Override
    protected void setValues() {
        id = ItemID.DASH_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Dash Ability";
        description = "Allows the wearer to dash";
    }
}
