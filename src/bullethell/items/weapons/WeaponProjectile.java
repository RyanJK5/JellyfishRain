package bullethell.items.weapons;

import java.util.function.Predicate;

import bullethell.Spritesheet;
import bullethell.combat.Projectile;
import bullethell.combat.WeaponEntity;
import bullethell.enemies.Enemy;
import bullethell.items.Item;
import bullethell.movement.Path;

public class WeaponProjectile extends Projectile implements WeaponEntity {

    private final Item item;

    public WeaponProjectile(Item sender, Spritesheet spritesheet, Path path, boolean immortal, int lifeSpan, float speed) {
        super(spritesheet, path, immortal, lifeSpan, speed, sender.dmg);
        item = sender;
    }

    public WeaponProjectile(Item sender, Spritesheet spritesheet, Path path, int speed) {
        super(spritesheet, path, speed, sender.dmg);
        item = sender;
    }

    public WeaponProjectile(Item sender, Spritesheet spritesheet, Path path) {
        super(spritesheet, path);
        item = sender;
    }

    public WeaponProjectile(Item sender, Path path) {
        super(path);
        item = sender;
    }

    public WeaponProjectile(Item sender) {
        super();
        item = sender;
    }

    @Override
    public Predicate<Enemy> getCritCondition() {
        return item.critCondition;
    }
}
