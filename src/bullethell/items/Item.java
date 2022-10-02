package bullethell.items;

import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import bullethell.GameObject;
import bullethell.Globals;
import bullethell.Player;
import bullethell.combat.Enchantment;
import bullethell.combat.StatusEffect;
import bullethell.enemies.Enemy;

public abstract class Item extends GameObject {

    public ItemID id;
    public EquipType equipType;

	public String name;
    public String description;

    public Recipe[] recipes;
    public List<Enchantment> enchantments;

    public boolean canStack;

    public Predicate<Enemy> critCondition;
    public float critMultiplier;
    public int dmg;
    public int manaCost;
    public int fireTime;
    public int range;

    public WeaponModifiers weaponModifiers;
    public PlayerModifiers playerModifiers;

    public int count;

    protected Item() {
        super(null, true);
        setSprite(new File("sprites\\items\\" + getClass().getSimpleName() + ".png").exists() ? 
          Globals.getImage("items\\" + getClass().getSimpleName()) : 
        Globals.getImage("items\\Default"));
        
        equipType = EquipType.NONE;
        name = "Unnamed";
        description = "";
        recipes = new Recipe[0];
        enchantments = new ArrayList<>();
        critCondition = e -> false;
        critMultiplier = 1.5f;
        weaponModifiers = new WeaponModifiers();
        playerModifiers = new PlayerModifiers();
        count = 0;

        setValues();
    }

    static {
        for (ItemID id : ItemID.values()) {
            id.getItem().addRecipes();
        }
    }

    protected abstract void setValues();
    protected void addRecipes() { }

    public void addEnchantment(Enchantment enchant) {
        enchantments.add(enchant);
    }

    public void onUse() { }

    public Item clone(int count) {
        Item item = ItemID.getItem(id); 
        if (canStack) {
            item.count = count;
        }
        return item;
    }

    public final boolean equals(Item item) {
        return item != null && id == item.id;
    }
    
    public final Item take(int num) {
        if (num < 0) {
            throw new IllegalArgumentException("num must be >0");
        }

        if (num == 0 || num > count) {
            int origCount = count;
            count = 0;
            kill();
            return clone(origCount);
        }

        count -= num;
        if (count <= 0) {
            kill();
        }
        return clone(num);
    }

    public final void add(int num) {
        count += num;
    }

    public final void addFrom(int num, Item item) {
        int count = num == 0 ? item.count : num;
        add(count);
        item.take(count);
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        if (count > 1) {
            g.drawString(Integer.toString(count), x, y + h);
        }
        
        if (Player.cursorOver(getBounds())) {
            drawInfo(g);
        }
    }

    public void drawInfo(Graphics g) {
        if (Player.get().getCursorSlot() != null) {
            return;
        }

        int index = 0;
        final int spacing = 20;
        final int x = Player.cursorX() + 10;
        final int y = Player.cursorY() + 25;

        for (int i = 0; i < 7; i++) {
            switch (i) {
                case 0:
                    g.drawString(name, x, y);
                    break;
                case 1:
                    if (equipType == EquipType.WEAPON) {
                        g.drawString("    " + Globals.damageFormula(dmg) + " damage", x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 2:
                    if (equipType == EquipType.WEAPON && manaCost != 0) {
                        g.drawString("    " + (int) ((float) manaCost / Player.get().getMaxMana() * 100) + "% mana", 
                          x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 3:
                    if (equipType == EquipType.WEAPON) {    
                        g.drawString("    " + fireTime + " fire time", x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 4:
                    if (equipType == EquipType.WEAPON && range > 0) {    
                        g.drawString("    " + range + " range", x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 5:
                    if (description.length() > 0) {
                        g.drawString("    " + description, x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 6:
                    g.setColor(new java.awt.Color(0, 153, 151));
                    for (Enchantment enchantment : enchantments) {
                        g.drawString("    " + enchantment, x, y + index * spacing);
                        index++;
                    }
            }
            index++;
        }
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
                        finalmDmg += enchantment.mDmg;
                    }
                    break;
                case INFLICT_EFFECT:
                    enemy.addStatusEffect(new StatusEffect(enchantment.sType));
                    break;
            }
        }
        return (int) (dmg * finalmDmg);
    }

    @Override
    public void toGhost() { }
    @Override
    public void unghost() { }
}
