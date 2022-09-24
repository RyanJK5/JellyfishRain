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
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.Entity;
import bullethell.GameObject;
import bullethell.GameSolid;
import bullethell.Globals;
import bullethell.Player;
import bullethell.Spritesheet;
import bullethell.items.ItemDrop;
import bullethell.items.ItemLoot;

public abstract class Enemy extends Entity {
    
    public EnemyID id;
	protected HealthBar healthBar;    
    public String name;
    public int timeSinceHit = 0;
    public int dmgTaken;
    public boolean bossEnemy;
    protected ItemLoot[] lootTable;
    
    public Area provocationArea;
    public Area provokedArea;
    public int groupID = -1;

    private final List<Hit> hits = new ArrayList<>();

    protected Enemy() {
        super();
        solids.remove(this);
        
        if (new File("sprites\\enemies\\" + getClass().getSimpleName() + ".png").exists()) {
            Dimension dimensions = getSpritesheetDimensions();
            setAnimations(new Spritesheet(Globals.getImage("enemies\\" + getClass().getSimpleName()), dimensions.width, dimensions.height)); 
        } else {
            setAnimations(new Spritesheet(Globals.getImage("enemies\\Default"), 1, 1));
        }

        try {
            healthBar = new HealthBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lootTable = new ItemLoot[0];
        name = "Unnamed";

        setValues();
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

    @Override  
    public void update() {
        if (readyToKill()) {
            gameKill();
        } 

        if (groupID >= 0 && !EnemyGroup.getGroup(groupID).anyDetectPlayer()) {
            return;
        }
        
        move();
        timeSinceHit++;
        if (timeSinceHit >= HealthBar.SHOW_TIME) {
    		healthBar.kill();
    	}
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

        g.setColor(new Color(1f, 0, 0, 0.2f));
        if (groupID >= 0 && EnemyGroup.getGroup(groupID).anyDetectPlayer()) {
            ((java.awt.Graphics2D) g).fill(getTranslatedArea(provokedArea));
        } else {
            ((java.awt.Graphics2D) g).fill(getTranslatedArea(provocationArea));
        }
        if (Player.cursorX() >= x && Player.cursorY() >= y &&
            Player.cursorX() <= x + w && Player.cursorY() <= y + w) {
                g.setColor(Color.WHITE);
                g.drawString(name, Player.cursorX(), Player.cursorY() - 5);
                g.drawString(getHP() + " / " + getMaxHP(), Player.cursorX(), Player.cursorY() - 5 - g.getFont().getSize());
        }

        for (int i = 0; i < hits.size(); i++) {
            Hit hit = hits.get(i);
            g.setColor(hit.color);
            if (hit.y > hit.origY - 100) hit.move(1,-10);
            if (hit.y < hit.origY - 50) hit.update();
            g.drawString(hit.text, hit.x, hit.y);
        }
        g.setColor(Color.WHITE);
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

    private class Hit {
        String text;
        int x, y;
        int origY;
        Color color = new Color(255, 0, 0, 255);
        private int decrNum = 25;

        Hit(int value, int x, int y) {
            this.text = Integer.toString(value);
            this.x = x;
            this.y = y;
            this.origY = y;
            hits.add(this);
        }

        void update() {
            if (color.getAlpha() <= 0) hits.remove(this);
            
            while (true) {
                try {
                    color = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() - decrNum);
                    return;
                } catch (IllegalArgumentException iae) {
                    decrNum--;
                    if (decrNum <= 0) return;
                }
            }
        }

        void move(int x, int y) {
            this.x += x;
            this.y += y;
        }
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

    @Override
    public void registerDMG(int dmg) {
        if (dmg <= 0) {
            return;
        }
        if (groupID >= 0) {
            EnemyGroup.getGroup(groupID).notifyHit();
        }
        dmgTaken = Globals.damageFormula(dmg);
        hp -= dmgTaken;
        timeSinceHit = 0;
        new Hit(dmgTaken, x + w, y);
        healthBar.revive();
    }
}