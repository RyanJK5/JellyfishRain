package bullethell.enemies;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import bullethell.Globals;
import bullethell.Player;

public class EnemyGroup implements ActionListener {
    
    private static final List<EnemyGroup> groups = new ArrayList<>();
    
    public static final int DEFAULT_DETECTION_RADIUS = 200;
    private static final int DEFAULT_DAMAGE_COOLDOWN = 5000 / Globals.TIMER_DELAY;

    private final List<Enemy> enemies;
 
    public final int groupID;
    private static int highestGroupID;

    private boolean active;
    private int timeSinceHit = DEFAULT_DAMAGE_COOLDOWN;
    
    public EnemyGroup(Enemy... enemies) {
        this.enemies = new ArrayList<>();
        for (Enemy enemy : enemies) {
            this.enemies.add(enemy);
        }

        groupID = highestGroupID;
        highestGroupID++;

        for (Enemy enemy : enemies) {
            enemy.groupID = groupID;
        }
        groups.add(this);
        Globals.GLOBAL_TIMER.addActionListener(this);
    }

    public static Area getAreaFromEnemy(Enemy enemy, int radius) {
        return new Area(new Ellipse2D.Float(0, 0, radius * 2, radius * 2));
    }

    public static boolean anyGroupAlive() {
        for (EnemyGroup group : groups) {
            if (group.allAlive()) {
                return true;
            }
        }
        return false;
    }

    public static EnemyGroup getGroup(int groupID) {
        return groups.get(groupID);
    }

    public static Enemy[] getEnemies(int groupID) {
        return groups.get(groupID).enemies.toArray(new Enemy[0]);
    }

    public boolean allAlive() {
        return enemies.stream().allMatch(e -> e.isAlive());
    }

    public boolean anyDetectPlayer() {
        return active;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Point playerPos = new Point(Player.get().getCenterX(), Player.get().getCenterY());
        if (active) {
            active = enemies.stream()
            .anyMatch(e -> e.getTranslatedArea(e.provokedArea).contains(playerPos));
        } else {
            active = enemies.stream()
            .anyMatch(e -> e.getTranslatedArea(e.provocationArea).contains(playerPos));
        }
        active = active || timeSinceHit < DEFAULT_DAMAGE_COOLDOWN;
        timeSinceHit++;
    }

    public void notifyHit() {
        active = true;
        timeSinceHit = 0;
    }

    public void add(Enemy enemy) {
        enemies.add(enemy);
    }

    public boolean remove(Enemy enemy) {
        return enemies.remove(enemy);
    }
}
