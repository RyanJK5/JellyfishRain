package bullethell.items.abilities;

import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public class FocusAbility extends Item {
    
    @Override
    protected void setValues() {
        id = ItemID.FOCUS_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Focus Ability";
        description = "Allows the wearer to focus";
    }
}
