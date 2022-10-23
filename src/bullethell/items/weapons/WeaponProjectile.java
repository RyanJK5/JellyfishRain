package bullethell.items.weapons;

import java.util.function.Predicate;

import bullethell.GameSolid;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.combat.Entity;
import bullethell.combat.Projectile;
import bullethell.combat.WeaponEntity;
import bullethell.enemies.Enemy;
import bullethell.movement.Path;

public class WeaponProjectile extends Projectile implements WeaponEntity {

    private final Weapon item;

    public WeaponProjectile(Weapon sender, Spritesheet spritesheet, Path path, boolean immortal, int lifeSpan, float speed) {
        super(spritesheet, path, immortal, lifeSpan, speed, sender.dmg);
        item = sender;
    }

    public WeaponProjectile(Weapon sender, Spritesheet spritesheet, Path path, int speed) {
        super(spritesheet, path, speed, sender.dmg);
        item = sender;
    }

    public WeaponProjectile(Weapon sender, Spritesheet spritesheet, Path path) {
        super(spritesheet, path);
        item = sender;
    }

    public WeaponProjectile(Weapon sender, Path path) {
        super(path);
        item = sender;
    }

    public WeaponProjectile(Weapon sender) {
        super();
        item = sender;
    }

    @Override
    public Predicate<Enemy> getCritCondition() {
        return item::critCondition;
    }

    @Override
    public boolean onCollision(GameSolid obj) {
        if (pierce < 0) {
			permakill();
			return false;
		}

		if (obj instanceof GameSolid && !(obj instanceof Entity)) {
			permakill();
			return false;
		}

        if (!(obj instanceof Enemy)) {
            return false;
        }

		Enemy enemy = (Enemy) obj;
		boolean successful = false;
		for (Entity hit : hits) {
			if (enemy == hit) {
				successful = true;
				break;
			}
		}
		if (!successful && !enemy.isInvicible()) {
			if (pierce != Integer.MAX_VALUE) {
				pierce--;
			}
			enemy.registerDMG(item.getModifiedDMG(enemy));
			if (friendly()) {
				Player.get().registerDealtDMG(dmg, this);
			}
			hits.add(enemy);

			if (pierce < 0) {
				permakill();
				return false;
			}
		}
		return !successful;
    }
}
