package bullethell.items.abilities;

import bullethell.Globals;
import bullethell.Player;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public final class TeleportAbility extends Item {
    
    private static int timeSinceTP = Player.DEFAULT_TP_COOLDOWN;

    @Override
    protected void setValues() {
        id = ItemID.TP_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Teleport Ability";
        description = "Allows the wearer to teleport";
    }

    static {
        Globals.GLOBAL_TIMER.addActionListener(e -> {
            if (Player.get().isAlive()) {
                timeSinceTP++;
            }
        });
    }

    @Override
    public void onUse() {
        if (timeSinceTP > Player.DEFAULT_TP_COOLDOWN) {
			Player player = Player.get();
            Player.get().setLocation(Player.cursorX() - player.getWidth() / 2, Player.cursorY() - player.getHeight() / 2);
			timeSinceTP = 0;
		}
    }
}
