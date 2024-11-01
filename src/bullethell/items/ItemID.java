package bullethell.items;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import bullethell.Globals;
import bullethell.items.abilities.*;
import bullethell.items.charms.*;
import bullethell.items.weapons.*;

public enum ItemID {
    EXAMPLE_SCEPTER(ExampleScepter.class),
    EXAMPLE_STAFF(ExampleStaff.class),
    EXAMPLE_SWORD(ExampleSword.class),
    EXAMPLE_MAGIC_WEAPON(ExampleMagicWeapon.class),
    DASH_ABILITY(DashAbility.class),
    HEAL_ABILITY(HealAbility.class),
    TP_ABILITY(TeleportAbility.class),
    FOCUS_ABILITY(FocusAbility.class),
    HEALTH_CHARM(HealthCharm.class),
    MAGIC_DUST(MagicDust.class),
    METAL(Metal.class),
    STRANGE_SIGIL(StrangeSigil.class),
    CUSTOM_CORE(CustomCore.class),
    ADRENALINE_ABILITY(AdrenalineAbility.class),
    TRIPLE_KNIFE(TripleKnife.class);

    private final Class<? extends Item> itemClass;

    private ItemID(Class<? extends Item> itemClass) {
        this.itemClass = itemClass;
    }

    public Item getItem() {
        return getItem(this);
    }

    public static int getID(Item item) {
        return Globals.indexOf(values(), id -> id == item.id);
    }

    public static ItemID getID(int intID) {
        return values()[intID];
    }

    public static Item getItem(int intID) {
        return values()[intID].getItem();
    }

    public static Item getItem(ItemID id) {
        try {
            return id.itemClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BufferedImage getSprite() {
        return new File("sprites\\items\\" + getClass().getSimpleName() + ".png").exists() ? 
          Globals.getImage("items\\" + getItem().getClass().getSimpleName()) : 
          Globals.getImage("items\\Default")
        ;
    }
}
