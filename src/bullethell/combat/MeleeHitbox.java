package bullethell.combat;

import java.util.function.Predicate;

import bullethell.GameSolid;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.enemies.Enemy;
import bullethell.items.Item;
import bullethell.movement.Path;

public class MeleeHitbox extends Entity implements WeaponEntity {

    private final int lifeSpan;
    private final int maxHits;
    private final int hitDelay;

    private final Item item;

    private int numHits;
    private int age;
    private int lastHit;

public MeleeHitbox(Item sender, Spritesheet spritesheet, java.awt.Rectangle bounds, int lifeSpan, int maxHits) {
        super(spritesheet, Path.DEFAULT_PATH, sender.dmg, 0, 0, true);
        setHitbox(bounds);
        setLayer(10);
        item = sender;
        this.lifeSpan = lifeSpan;
        this.maxHits = maxHits;
        hitDelay = lifeSpan / maxHits;
    }

    @Override
    public final boolean onCollision(GameSolid obj) {
        if (!(obj instanceof Enemy enem && !enem.friendly())) return false;
            if (numHits < maxHits && age - hitDelay >= lastHit && !enem.isInvicible()) {
                enem.registerDMG(item.getCritDMG(enem));
                Player.get().registerDealtDMG(dmg, this);
                numHits++;
                lastHit = age;
                return true;
            }
            return false;
    }

    @Override
    public final boolean readyToKill() {
        return age > lifeSpan;
    }

    @Override
    public void update() {
        if (readyToKill()) {
            kill();
            return;
        }
        Player player = Player.get();
        setRotationAnchor(player.getCenterX(), player.getCenterY());
        if (age == 0) {
            float theta = (float) Math.atan2(player.getCenterX() - Player.cursorX(), player.getCenterY() - Player.cursorY());
            float angle = (float) Math.toDegrees(theta);
            angle = -angle;
            setRotationAnchor(w / 2, h);
            rotate(angle);
        }
        setLocation(player.getCenterX() - w / 2, player.getCenterY() - h);
        age++;
    }

    protected int getLifeSpan() {
        return lifeSpan;
    }

    protected int getAge() {
        return age;
    }

    @Override
    public final void registerDMG(int dmg) { }

    @Override
    public Predicate<Enemy> getCritCondition() {
        return item.critCondition;
    }
}
