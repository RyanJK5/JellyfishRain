package bullethell.enemies;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import bullethell.GameObject;
import bullethell.GameSolid;
import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.combat.Entity;
import bullethell.combat.tags.StatusEffect;
import bullethell.combat.tags.StatusEffectType;
import bullethell.combat.tags.Tag;
import bullethell.combat.tags.TagActivationType;
import bullethell.items.ItemDrop;
import bullethell.items.ItemLoot;
import bullethell.ui.TextBubble;

public abstract class Enemy extends Entity {
    
    public EnemyID id;
	protected HealthBar healthBar;    
    public String name;
    public int timeSinceHit = 0;
    public int dmgTaken;
    public boolean bossEnemy;
    public boolean paused;
    
    protected ItemLoot[] lootTable;
    public final List<Tag> tags;
    
    public Area provocationArea;
    public Area provokedArea;
    public int groupID = -1;

    protected Enemy() {
        super();
        solids.remove(this);
        
        if (new File("sprites\\enemies\\" + getClass().getSimpleName() + ".png").exists()) {
            Dimension dimensions = getSpritesheetDimensions();
            setAnimations(new Spritesheet(Globals.getImage("enemies\\" + getClass().getSimpleName()), dimensions.width, dimensions.height)); 
        } else {
            System.out.println(getClass().getSimpleName());
            setAnimations(new Spritesheet(Globals.getImage("enemies\\Default"), 1, 1));
        }

        try {
            healthBar = new HealthBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lootTable = new ItemLoot[0];
        tags = new ArrayList<>();
        name = "Unnamed";

        setValues();
        hp = maxHP;
        createLootTable();

        provocationArea = EnemyGroup.getAreaFromEnemy(this, EnemyGroup.DEFAULT_DETECTION_RADIUS);
        provokedArea = EnemyGroup.getAreaFromEnemy(this, EnemyGroup.DEFAULT_DETECTION_RADIUS * 2);

        solids.add(this);
    }

    protected abstract void setValues();

    protected abstract void createLootTable();

    protected Dimension getSpritesheetDimensions() {
        return new Dimension(1, 1);
    }

    public void addTag(Tag tag) {
        if (tag == null || (!tag.canStack() && tags.stream().anyMatch(t -> t.getClass() == tag.getClass()))) {
            return;
        }
        if (tag.getActivationType() == TagActivationType.IMMEDIATE) {
            tag.apply(this);
            if (tag.oneTime()) {
                return;
            }
        }
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    @Override  
    public void update() {
        updateTags(TagActivationType.EVERY_TICK);
        timeSinceHit++;
        if (timeSinceHit >= HealthBar.SHOW_TIME) {
            healthBar.kill();
        }
        
        if (paused || (groupID >= 0 && !EnemyGroup.getGroup(groupID).anyDetectPlayer())) {
            return;
        }

        move();
    }

    @Override
    public final Enemy clone() {
        return EnemyID.getEnemy(id);
    }
    
    @Override
    public void setLocation(int x, int y) {
        AffineTransform at = AffineTransform.getTranslateInstance(x - this.x, y - this.y);
		Area area = new Area(getHitbox());
		area.transform(at);
		setHitbox(area);
        this.x = x;
        this.y = y;
        if (healthBar != null) {
            healthBar.setLocation((x + w / 2) - healthBar.getWidth() / 2, y + h + 10);
        }
    }

    @Override
    public boolean onCollision(GameSolid obj) {
        if (!(obj instanceof Entity)) {
            if (!ignoreSolids) {
                if (groupID >= 0) {
                    EnemyGroup.getGroup(groupID).notifyHit();
                }
                    
                Point p = path.move(speed);
                while (obj.getHitbox().intersects(x + p.x, y + p.y, w, h)) {
                    p = path.move(speed);
                    setLocation(x - p.x, y - p.y);
                }
            }
            return false;
        }

        try {
            Entity ent = (Entity) obj;
            if (!ent.isInvicible() && ent.friendly() != friendly()) {
                ent.registerDMG(dmg);
            }
        } catch (UnsupportedOperationException usoe) {}
        return false;
    }
    
    public Area getTranslatedArea(Area area) {
        return area.createTransformedArea(AffineTransform.getTranslateInstance(
            getCenterX() - area.getBounds().width / 2, 
            getCenterY() - area.getBounds().height / 2));
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        Map<StatusEffectType, Integer> toBeDrawn = new HashMap<>();
        for (Tag tag : tags) {
            if (tag instanceof StatusEffect status) {
                if (toBeDrawn.keySet().contains(status.type)) {
                    toBeDrawn.put(status.type, toBeDrawn.get(status.type) + 1);
                } else {
                    toBeDrawn.put(status.type, 1);
                }
            }
        }
        for (int i = 0; i < toBeDrawn.size(); i++) {
            StatusEffectType type = toBeDrawn.keySet().toArray(new StatusEffectType[0])[i];
            BufferedImage icon = type.getIcon();
            final int width = icon.getWidth() + 15;
            final int xPos = getCenterX() - (int) (toBeDrawn.size() / 2f * width) + i * width;
         
            g.drawImage(icon, xPos, getY() - 20, null);
            if (toBeDrawn.get(type) > 1) {
                g.drawString(Integer.toString(toBeDrawn.get(type)), xPos + icon.getWidth(), getY() - 20 + icon.getHeight());
            }
        }

        if (Player.cursorX() >= x && Player.cursorY() >= y &&
            Player.cursorX() <= x + w && Player.cursorY() <= y + w) {
                g.setColor(Color.WHITE);
                g.drawString(name, Player.cursorX(), Player.cursorY() - 5);
                g.drawString(getHP() + " / " + getMaxHP(), Player.cursorX(), Player.cursorY() - 5 - g.getFont().getSize());
        }
    }

    @Override
    public boolean readyToKill() {
        return getHP() <= 0;
    }
    
    @Override
    public void kill() {
    	isAlive = false;
        if (healthBar != null) {
            healthBar.kill();
        }
    }

    private void gameKill() {
        permakill();

        updateTags(TagActivationType.ON_DEATH);

        if (lootTable == null) {
            return;
        }

        for (ItemLoot item : lootTable) {
            float chance = Globals.rand.nextFloat();
            if (item.chance >= chance) {
                new ItemDrop(item.item, Globals.rand.nextInt(item.minAmount, item.maxAmount + 1), 
                  getCenterX() + Globals.rand.nextInt(-20, 20), getCenterY() + Globals.rand.nextInt(-20, 20));
            }
        }
    }
    
    @Override
    public void revive() {
        isAlive = true;
        resetHP();
    } 

    private class HealthBar extends GameObject {
		
		public static final int SHOW_TIME = 2000 / Globals.TIMER_DELAY;
    	
    	public HealthBar() throws IOException {
    		super(ImageIO.read(new File("sprites/HealthBarBackSmall.png")));
    	}
    	
    	@Override
		public void update(Graphics g) {
			try {
				super.update(g);
                BufferedImage frontSprite = ImageIO.read(new File("sprites/HealthBarFrontSmall.png"));
				if (getHP() > 0) {
					float slivWidth;
					slivWidth = (float) w / (float) getMaxHP();
	
					int fullWidth = (int) (slivWidth * getHP());
					g.drawImage(frontSprite, x, y, fullWidth, h, null);
				}
			} catch (IOException e) { e.printStackTrace(); }
		}
    }

    @Override
    public String toString() {
        return name;
    }

    public void registerDMG(int dmg, StatusEffectType statusType) {
        if (dmg <= 0) {
            return;
        }
        if (groupID >= 0) {
            EnemyGroup.getGroup(groupID).notifyHit();
        }
        dmgTaken = Globals.damageFormula(dmg);
        hp -= dmgTaken;
        timeSinceHit = 0;
        new TextBubble(Integer.toString(dmgTaken), 1f, statusType != null ? statusType.getColor() : 
          Globals.DEFAULT_COLOR).setLocation(getCenterX() + Globals.rand.nextInt(-50, 50), getCenterY() + Globals.rand.nextInt(-25, 50));
        healthBar.revive();

        updateTags(TagActivationType.ON_HIT);
    
        if (readyToKill()) {
            gameKill();
        }
    }

    public void registerDMG(int dmg) {
        registerDMG(dmg, null);
    }

    private void updateTags(TagActivationType type) {
        List<Tag> toRemove = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (tag.getActivationType() != type) {
                continue;
            }
            if (!tag.active()) {
                toRemove.add(tag);
                continue;
            }
            tag.apply(this);
            if (tag.oneTime()) {
                toRemove.add(tag);
            }
        }
        tags.removeAll(toRemove);
    }
}