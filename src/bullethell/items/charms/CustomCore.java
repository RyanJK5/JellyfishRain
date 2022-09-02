package bullethell.items.charms;

import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.PlayerModifiers;
import bullethell.items.WeaponModifiers;

public class CustomCore extends Item {

    public CustomCore() {
        this(new WeaponModifiers(), new PlayerModifiers());
    }

    public CustomCore(WeaponModifiers wModifiers, PlayerModifiers pModifiers) {
        super();
        weaponModifiers = wModifiers;
        playerModifiers = pModifiers;
    }

    @Override
    protected void setValues() {
        id = ItemID.CUSTOM_CORE;
        equipType = EquipType.CORE;     
    }
    
}
