package bullethell.items.abilities;

import bullethell.Player;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public final class HealAbility extends Item {

    @Override
    protected void setValues() {
        id = ItemID.HEAL_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Heal Ability";
        description = "Allows the wearer to heal";

        playerModifiers.pHP = 100;
    }

    @Override
    public void paint(java.awt.Graphics g) {
        super.paint(g);
        g.drawString(Integer.toString(Player.get().getHealNum()), x, y + h);
        if (Player.cursorOver(getBounds())) {
            drawInfo(g);
        }
    }
}
