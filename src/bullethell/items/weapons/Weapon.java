package bullethell.items.weapons;

import java.util.ArrayList;
import java.util.List;

import bullethell.combat.Enchantment;
import bullethell.combat.EnchantmentPool;
import bullethell.combat.tags.DeathExplosion;
import bullethell.combat.tags.StatusEffect;
import bullethell.combat.tags.StatusEffectType;
import bullethell.enemies.Enemy;
import bullethell.items.EquipType;
import bullethell.items.Item;

public abstract class Weapon extends Item {

    public List<Enchantment> enchantments;
    public EnchantmentPool enchantPool;
    public StatusEffectType[] allowedEffects;

    public float critMultiplier;
    public int dmg;
    public int manaCost;
    public int fireTime;
    public int range;

    protected Weapon() {
        super();
        equipType = EquipType.WEAPON;
        enchantments = new ArrayList<>();
        critMultiplier = 1.5f;
        allowedEffects = new StatusEffectType[0];
        setEnchantmentParams();
    }

    protected abstract void setEnchantmentParams();

    public boolean critCondition(Enemy enemy) {
        return false;
    }

    public void addEnchantment(Enchantment enchant) {
        enchantments.add(enchant);
    }

    public int getModifiedDMG(Enemy enemy) {
        float finalmDmg = 1;
        if (critCondition(enemy)) {
            finalmDmg += critMultiplier;
        }
        for (Enchantment enchantment : enchantments) {
            switch (enchantment.eType) {
                case EFFECT_DAMAGE_BOOST:
                    if (enchantment.test(enemy)) {
                        finalmDmg += enchantment.floatArg;
                    }
                    break;
                case INFLICT_EFFECT:
                    enemy.addTag(StatusEffect.getStatusEffect(enchantment.sType));
                    break;
                case VICTIMS_EXPLODE:
                    enemy.addTag(new DeathExplosion(enchantment.intArg));
                    break;
            }
        }
        return (int) (dmg * finalmDmg);
    }
}
