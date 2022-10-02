package bullethell.combat;

import bullethell.Globals;

// TODO : add enchantment pools so different types of items can get different types of enchantments
public enum EnchantmentType {
    EFFECT_DAMAGE_BOOST, INFLICT_EFFECT;

    public static int getID(EnchantmentType type) {
        return Globals.indexOf(values(), type);
    }
}
