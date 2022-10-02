package bullethell.combat;

import bullethell.enemies.Enemy;

public class Enchantment {
    
    public final EnchantmentType eType;
    public final StatusEffectType sType;

    public final float mDmg;

    public Enchantment(float mDmg, StatusEffectType type) {
        eType = EnchantmentType.EFFECT_DAMAGE_BOOST;
        sType = type;
    
        this.mDmg = mDmg;
    }

    public Enchantment(StatusEffectType type) {
        eType = EnchantmentType.INFLICT_EFFECT;
        sType = type;

        mDmg = 0;
    }

    /**
     * A constructor with all arguments. Some parameters may be ignored based on {@code eType}.
     */
    public Enchantment(EnchantmentType eType, StatusEffectType sType, float mDmg) {
        this.eType = eType;
        this.sType = sType;
        switch (eType) {
            case EFFECT_DAMAGE_BOOST:
                this.mDmg = mDmg;
                break;
            default:
                this.mDmg = 0;
                break;
        }
    }

    public boolean test(Enemy enemy) {
        return enemy.activeEffects.stream().anyMatch(effect -> effect.type == sType);
    }

    @Override
    public String toString() {
        switch (eType) {
            case EFFECT_DAMAGE_BOOST:
                return "+" + Math.round(mDmg * 100) + "% damage to " + sType.pastTense() + " enemies";
            case INFLICT_EFFECT:
                return sType.futureTense() + " on enemies";
            default:
                return "";
        }
    }
}
