package bullethell.items.abilities;

import bullethell.Player;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public class FocusAbility extends Item {

    private boolean slowActivated = false;
    private boolean activating = false;
    
    @Override
    protected void setValues() {
        id = ItemID.FOCUS_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Focus Ability";
        description = "Allows the wearer to focus";
    }
    
    @Override
    public void update(java.awt.Graphics g) {
        super.update(g);
        if (!Player.get().isAlive() && slowActivated) {
            slowActivated = false;
        }
    }

    @Override
    public void onUse() {
        Player player = Player.get();
        activating = !activating;
        if (activating && !slowActivated) {
            player.playerMods.mSpeed += -1.3f;
            slowActivated = true;
        } else if (!activating && slowActivated) {
            player.playerMods.mSpeed += 1.3f;
            slowActivated = false;
        }
        player.registerModifiers();
    }
}
