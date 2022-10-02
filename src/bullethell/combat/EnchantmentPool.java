package bullethell.combat;

import static bullethell.combat.EnchantmentType.*;

public enum EnchantmentPool {
    MELEE_WEAPON(EFFECT_DAMAGE_BOOST, INFLICT_EFFECT, VICTIMS_EXPLODE),
    MANA_WEAPON(EFFECT_DAMAGE_BOOST, INFLICT_EFFECT),
    RANGED_WEAPON(EFFECT_DAMAGE_BOOST, INFLICT_EFFECT),
    MAGIC_RANGED_WEAPON(EFFECT_DAMAGE_BOOST, INFLICT_EFFECT),
    MAGIC_MELEE_WEAPON(EFFECT_DAMAGE_BOOST, INFLICT_EFFECT);

    private final EnchantmentType[] types;

    private EnchantmentPool(EnchantmentType... types) {
        this.types = types;
    }

    public EnchantmentType[] getTypes() {
        return types;
    }

    public EnchantmentType getType(int index) {
        return types[index];
    }
}
