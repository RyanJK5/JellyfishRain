package bullethell.enemies;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import bullethell.Globals;
import bullethell.Spritesheet;

public enum EnemyID {
    DUMMY(Dummy.class),
    PIXIE(Pixie.class),
    JELLY_FISH_BOSS(JellyFishBoss.class);

    private Class<? extends Enemy> clazz;

    private EnemyID(Class<? extends Enemy> clazz) {
        this.clazz = clazz;
    }

    public Enemy getEnemy() {
        return getEnemy(this);
    }

    public static int getID(Enemy enemy) {
        return Globals.indexOf(values(), id -> id == enemy.id);
    }

    public static EnemyID getID(int intID) {
        return values()[intID];
    }

    public static Enemy getEnemy(int intID) {
        return values()[intID].getEnemy();
    }

    public static Enemy getEnemy(EnemyID id) {
        try {
            return id.clazz.getDeclaredConstructor(new Class<?>[0]).newInstance(new Object[0]);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Spritesheet getSpritesheet() {
        java.awt.Dimension dimensions = getEnemy().getSpritesheetDimensions();
        return new File("sprites\\enemies\\" + getClass().getSimpleName() + ".png").exists() ? 
          new Spritesheet(Globals.getImage("enemies\\" + getEnemy().getClass().getSimpleName()), dimensions.width, dimensions.height) : 
          new Spritesheet(Globals.getImage("enemies\\Default"), 1, 1)
        ;
    }
}
