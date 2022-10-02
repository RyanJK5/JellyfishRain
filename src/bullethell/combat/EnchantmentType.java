package bullethell.combat;

import bullethell.Globals;

public enum EnchantmentType {
    EFFECT_DAMAGE_BOOST(true), 
    INFLICT_EFFECT(true),
    VICTIMS_EXPLODE(false);

    public final boolean statusEffectBased;

    private EnchantmentType(boolean statusEffectBased) {
        this.statusEffectBased = statusEffectBased;
    }

    public static EnchantmentType getType(int id) {
        return values()[id];
    }

    public static int getID(EnchantmentType type) {
        return Globals.indexOf(values(), type);
    }
}
