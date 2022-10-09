package bullethell.combat;

import bullethell.combat.tags.StatusEffect;
import bullethell.combat.tags.StatusEffectType;
import bullethell.enemies.Enemy;

public class Enchantment {
    
    public final EnchantmentType eType;
    public final StatusEffectType sType;

    public final float floatArg;
    public final int intArg;

    /**
     * Corresponds to {@code EFFECT_DAMAGE_BOOST}
     */
    public Enchantment(float mDmg, StatusEffectType type) {
        eType = EnchantmentType.EFFECT_DAMAGE_BOOST;
        sType = type;
    
        floatArg = mDmg;
        intArg = 0;
    }

    /**
     * Corresponds to {@code INFLICT_EFFECT}
     */
    public Enchantment(StatusEffectType type) {
        eType = EnchantmentType.INFLICT_EFFECT;
        sType = type;

        floatArg = 0;
        intArg = 0;
    }

    /**
     * Corresponds to {@code VICTIMS_EXPLODE}
     */
    public Enchantment(int explosionRadius) {
        eType = EnchantmentType.VICTIMS_EXPLODE;
        sType = null;

        floatArg = 0;
        intArg = explosionRadius;
    }

    /**
     * A constructor with all arguments. Some parameters may be ignored based on {@code eType}.
     */
    public Enchantment(EnchantmentType eType, StatusEffectType sType, float floatArg, int intArg) {
        this.eType = eType;
        switch (eType) {
            case EFFECT_DAMAGE_BOOST:
                this.sType = sType;
                this.floatArg = floatArg;
                this.intArg = 0;
                break;
            case INFLICT_EFFECT:
                this.sType = sType;
                this.floatArg = floatArg;
                this.intArg = intArg;
                break;
            case VICTIMS_EXPLODE:
                this.sType = sType;
                if (intArg < 0) {
                    this.intArg = 200;
                } else {
                    this.intArg = intArg;
                }
                this.floatArg = 0;
                break;
            default:
                this.sType = null;
                this.floatArg = 0;
                this.intArg = 0;
                break;
        }
    }

    public boolean test(Enemy enemy) {
        return enemy.tags.stream().anyMatch(tag -> tag instanceof StatusEffect effect &&  effect.type == sType);
    }

    @Override
    public String toString() {
        switch (eType) {
            case EFFECT_DAMAGE_BOOST:
                return "+" + Math.round(floatArg * 100) + "% damage to " + sType.pastTense() + " enemies";
            case INFLICT_EFFECT:
                return sType.futureTense() + " enemies";
            case VICTIMS_EXPLODE:
                return "Enemies explode on death";
        }
        return "";
    }
}
