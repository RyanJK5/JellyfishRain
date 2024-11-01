package bullethell.items;

import java.awt.Graphics;
import java.io.File;

import bullethell.GameObject;
import bullethell.Globals;
import bullethell.Player;
import bullethell.combat.Enchantment;
import bullethell.items.weapons.Weapon;

public abstract class Item extends GameObject {

    public ItemID id;
    public EquipType equipType;

	public String name;
    public String description;

    public Recipe[] recipes;

    public boolean canStack;
    
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
                    if (this instanceof Weapon wep) {
                        g.drawString("    " + Globals.damageFormula(wep.dmg) + " damage", x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 2:
                    if (this instanceof Weapon wep2 && wep2.manaCost != 0) {
                        g.drawString("    " + (int) ((float) wep2.manaCost / Player.get().getMaxMana() * 100) + "% mana", 
                          x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 3:
                    if (this instanceof Weapon wep3) {    
                        g.drawString("    " + wep3.fireTime + " fire time", x, y + index * spacing);
                        break;
                    } else {
                        continue;
                    }
                case 4:
                    if (this instanceof Weapon wep4 && wep4.range > 0) {    
                        g.drawString("    " + wep4.range + " range", x, y + index * spacing);
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
                    if (this instanceof Weapon wep5) {
                        for (Enchantment enchantment : wep5.enchantments) {
                            g.drawString("    " + enchantment, x, y + index * spacing);
                            index++;
                        }
                    }
            }
            index++;
        }
    }

    @Override
    public void toGhost() { }
    @Override
    public void unghost() { }
}
