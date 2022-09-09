package bullethell.items.abilities;

import bullethell.Player;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public class AdrenalineAbility extends Item {

    @Override
    protected void setValues() {
        id = ItemID.ADRENALINE_ABILITY;
        equipType = EquipType.ABILITY;
        
        name = "Adrenaline Ability";
        description = "Adrenaline charges quicker, but the damage bonus is only applied when activated. \n" +
          "Adrenaline will deplete while the ability is active.";
    }

    @Override
    public void onUse() {
        Player.get().useAdren();
    }
}
