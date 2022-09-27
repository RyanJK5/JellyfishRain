package bullethell.combat;

import bullethell.Globals;

public enum EnchantmentType {
    EFFECT_DAMAGE_BOOST, INFLICT_EFFECT;

    public static int getID(EnchantmentType type) {
        return Globals.indexOf(values(), type);
    }
}
