package bullethell.scenes;

import bullethell.GameSolid;
import bullethell.GameState;
import bullethell.Globals;
import bullethell.Trigger;

public final class EnchantmentForge implements Scene {

    private static final EnchantmentForge ENCHANTMENT_FORGE = new EnchantmentForge();

    private GameSolid forge;
    private Trigger startTrigger;

    public static EnchantmentForge get() {
        return ENCHANTMENT_FORGE;
    }

    private EnchantmentForge() { }

    @Override
    public GameState getState() {
        return GameState.DEFAULT;
    }

    @Override
    public void start(int x, int y) {
        forge = new GameSolid(Globals.getImage("EnchantForge"));
        forge.setLocation(x, y);

        startTrigger = new Trigger(null, new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER, Trigger.TARGET_IN_RANGE}) {

            @Override
            protected void activate() {
                System.out.println("heyo");   
            }
        };
        forge.addTrigger(startTrigger);
    }

    @Override
    public void end() {
        
    }

    @Override
    public boolean isActive() {
        return false;
    }

}
