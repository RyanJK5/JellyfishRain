package bullethell;

import java.awt.Color;
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
import java.util.Random;

import javax.imageio.ImageIO;

import bullethell.items.Item;
import bullethell.items.ItemDrop;
import bullethell.items.StackableItem;
import bullethell.movement.Path;

public class Enemy extends Entity {
    
	protected HealthBar healthBar;    
    protected String name;
    protected int timeSinceHit = 0;
    protected int dmgTaken;
    protected int groupID = -1;

    private List<Hit> hits = new ArrayList<>();
    private HashMap<Item, Float> lootTable = new HashMap<>();

    public Enemy(Spritesheet spritesheet, String name, Path path, int maxHP, int dmg, float speed) throws IOException {
        super(spritesheet, path, dmg, maxHP, speed, false);
        this.name = name;
        healthBar = new HealthBar();
        healthBar.kill();   
    }
    
    public Enemy(Spritesheet spritesheet, String name, int maxHP, int dmg, float speed) throws IOException {
        this(spritesheet, name, Path.DEFAULT_PATH, maxHP, dmg, speed);
    }

    private Enemy(Enemy enemy) throws IOException {
        super(enemy);
        name = enemy.name;
        healthBar = new HealthBar();
        healthBar.kill();
        lootTable = enemy.lootTable;
    }

    public void addItemsLootTable(HashMap<Item, Float> newItems) {
        lootTable.putAll(newItems);
    }

    public void addItemToLootTable(Item item, float chance) {
        lootTable.put(item, chance);
    }

    @Override  
    public void update() {
        move();
		if (readyToKill()) {
            gameKill();
        } 
        timeSinceHit++;
        if (timeSinceHit >= HealthBar.SHOW_TIME) {
    		healthBar.kill();
    	}
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
            healthBar.setLocation((x + w / 2) - healthBar.w / 2, y + h + 10);
        }
    }

    @Override
    public boolean onCollision(GameSolid obj) {
        if (!(obj instanceof Entity)) {
            if (!ignoreSolids) {
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
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
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
    	healthBar.kill();
        if (groupID >= 0 && !groupIsAlive()) {
            Globals.setGameState(GameState.DEFAULT);
        }
    }

    private void gameKill() {
        permakill();

        Random rand = new Random();
        for (Item item : lootTable.keySet()) {
            float chance = rand.nextFloat();
            if (lootTable.get(item) >= chance) {
                if (item instanceof StackableItem stack) {
                    item = new StackableItem(stack, rand.nextInt(stack.getCount(), stack.getCapacity() + 1));
                } 
                new ItemDrop(item, getCenterX() + rand.nextInt(-20, 20), getCenterY() + rand.nextInt(-20, 20));
            }
        }
    }
    
    @Override
    public void revive() {
        isAlive = true;
        resetHP();
    } 

    public void addToGroup(int id) {
        groupID = id;
    }

    public boolean groupIsAlive() {
        for (GameSolid solid : solids) {
            if (solid instanceof Enemy enemy && enemy.groupID == groupID && enemy.isAlive()) {
                return true;
            }
        }
        return false;
    }
    
    class Hit {
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

    public class HealthBar extends GameObject {
		
		public static final int SHOW_TIME = 2000 / Globals.TIMER_DELAY;
    	
    	public HealthBar() throws IOException {
    		super(ImageIO.read(new File("sprites/HealthBarBackSmall.png")));
    	}
    	
    	@Override
		public void paint(Graphics g) {
			try {
				super.paint(g);
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

    public Enemy clone() {
        try {
            return new Enemy(this);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void registerDMG(int dmg) {
        if (dmg <= 0) {
            return;
        }
        
        dmgTaken = dmg;
        hp -= dmg;
        timeSinceHit = 0;
        new Hit(dmgTaken, x + w, y);
        healthBar.revive();
    }
}