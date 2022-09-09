package bullethell.items.abilities;

import bullethell.Player;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public final class HealAbility extends Item {

    public static int maxHealNum = 3;
    public static int healNum = maxHealNum;

    @Override
    protected void setValues() {
        id = ItemID.HEAL_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Heal Ability";
        description = "Allows the wearer to heal";

        playerModifiers.pHP = 100;
    }

    @Override
    public void update(java.awt.Graphics g) {
        super.update(g);
        g.drawString(Integer.toString(healNum), x, y + h);
        if (Player.cursorOver(getBounds())) {
            drawInfo(g);
        }
    }

    @Override
    public void onUse() {
        if (healNum > 0) {
			Player player = Player.get();
            player.setHP(player.getHP() + Player.DEFAULT_HEAL_AMOUNT);
			if (player.getHP() > player.getMaxHP() || player.getHP() < 0) {
				player.setHP(player.getMaxHP());
			}
			healNum--;
		} 
    }
}
