package bullethell.items.weapons;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

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

    public Predicate<Enemy> critCondition;
    public float critMultiplier;
    public int dmg;
    public int manaCost;
    public int fireTime;
    public int range;

    protected Weapon() {
        super();
        equipType = EquipType.WEAPON;
        enchantments = new ArrayList<>();
        critCondition = e -> false;
        critMultiplier = 1.5f;
        allowedEffects = new StatusEffectType[0];
        setEnchantmentParams();
    }

    protected abstract void setEnchantmentParams();

    public void addEnchantment(Enchantment enchant) {
        enchantments.add(enchant);
    }

    public int getModifiedDMG(Enemy enemy) {
        float finalmDmg = 1;
        if (critCondition.test(enemy)) {
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
                    enemy.addTag(new StatusEffect(enchantment.sType));
                    break;
                case VICTIMS_EXPLODE:
                    enemy.addTag(new DeathExplosion(enchantment.intArg));
                    break;
            }
        }
        return (int) (dmg * finalmDmg);
    }
}
