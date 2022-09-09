package bullethell.items.abilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import bullethell.Globals;
import bullethell.Player;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.ItemID;

public final class DashAbility extends Item {

    private static int timeSinceDash = Player.DEFAULT_DASH_DELAY;
    private static int dashLength = 5;

    @Override
    protected void setValues() {
        id = ItemID.DASH_ABILITY;
        equipType = EquipType.ABILITY;

        name = "Dash Ability";
        description = "Allows the wearer to dash";
    }

    static {
        Globals.GLOBAL_TIMER.addActionListener(e -> {
            if (Player.get().isAlive()) {
                timeSinceDash++;
            }
        });
    }

    @Override
    public void onUse() {
        Player player = Player.get();
        if (timeSinceDash < Player.DEFAULT_DASH_DELAY) {
			return;
		}
		timeSinceDash = 0;
		player.dashInvinc = true;

		Globals.GLOBAL_TIMER.addActionListener(new ActionListener() {
			int timesPerformed = 0;

			@Override
			public void actionPerformed(ActionEvent e) {
                if (timesPerformed == dashLength) {
					player.dashInvinc = false;
					Globals.GLOBAL_TIMER.removeActionListener(this);
					return;
				}
				if (player.getLastDirections().size() > 0) {
                    final float speed = player.getSpeed();
					player.setSpeed(50);
					player.move();
					player.setSpeed(speed);
				}
				timesPerformed++;
			}
		});
    }
}
