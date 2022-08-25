package bullethell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.items.Ability;
import bullethell.items.Accessory;
import bullethell.items.Core;
import bullethell.items.Item;
import bullethell.items.Recipe;
import bullethell.items.StackableItem;
import bullethell.items.Weapon;
import bullethell.movement.Direction;
import bullethell.scenes.Bossfight;
import bullethell.ui.Container;
import bullethell.ui.Inventory;
import bullethell.ui.Text;
import bullethell.ui.UI;

public final class Player extends Entity {
	
	public static final float DEFAULT_SPEED = 10;
	public static final int DEFAULT_MAX_HP = 300;
	public static final int DEFAULT_INVINC_TIME = 1000 / Globals.TIMER_DELAY;
	public static final int DEFAULT_MIN_REGEN_DELAY = 25;
	public static final int DEFAULT_REGEN_DECREASE_RATE = 2;
	public static final int DEFAULT_HIT_TO_REGEN_DELAY = 3000 / Globals.TIMER_DELAY;
	public static final int DEFAULT_TP_COOLDOWN = 10 * 1000 / Globals.TIMER_DELAY;
	public static final int DEFAULT_HEAL_AMOUNT = (int) (DEFAULT_MAX_HP * 0.3f);
	public static final int DEFAULT_DASH_DELAY = 1000 / Globals.TIMER_DELAY;
	public static final int UI_HIDE_COOLDOWN = 1500 / Globals.TIMER_DELAY;
	
	private static final float SLOW_CHARM_SPEED_MULTIPLIER = 1.5f;

	protected List<Direction> lastDirections = new ArrayList<>();
	
	private static Player player;
	
	protected int equipmentInvIndex;
	private Inventory<Item> inventory;
	protected HealthBar healthBar;
	protected HealthBar adrenalineBar;
	protected HealthBar manaBar;
	protected StatusWheel tpCooldown;
	protected StatusWheel dashCooldown;
	protected StatusWheel healWheel;
	protected final GameObject deathScreen;
	protected Item cursorSlot;
	protected List<Equipment> loadouts;
	protected final List<Recipe> researchedRecipes;
	
	protected int maxHealNum = 3;
	protected int healNum = maxHealNum;

	private static int cameraX, cameraY;
	private static int cursorX, cursorY;
	
	protected boolean firing;
	
	protected int adren;
	protected int maxAdr = 1000;
	protected int hitToAdrDelay = 100;

	protected int mana;
	protected int maxMana = 1000;

	protected int maxRegenDelay = 400 / Globals.TIMER_DELAY;
	protected int minRegenDelay = DEFAULT_MIN_REGEN_DELAY;
	protected int regenDelay = maxRegenDelay;
	protected int hitToRegenDelay = DEFAULT_HIT_TO_REGEN_DELAY;
	protected int regenDecreaseRate = DEFAULT_REGEN_DECREASE_RATE;
	
	protected int currentFire = 0;
	protected int currentInvinc = 0;
	protected int timeSinceHit = 0;
	protected int timeSinceTP = DEFAULT_TP_COOLDOWN;
	private int timeSinceUI = -1;

	protected int invincTime = DEFAULT_INVINC_TIME;

	protected int dashLength = 5;
	protected int timeSinceDash = DEFAULT_DASH_DELAY;
	protected int dashDelay = DEFAULT_DASH_DELAY;
	protected boolean dashInvinc = false;

	/* list of player stat multipliers in the following order: 
	 *  0 - HP
	 *  1 - DMG
	 *  2 - FIRE TIME
	 *  3 - RANGE
	 *  4 - INVINCIBILITY TIME
	 *  5 - MIN REGEN DELAY
	 *  6 - REGEN DECREASE RATE
	 *  7 - HIT TO REGEN DELAY
	 *  8 - SHOT SPEED
	 *  9 - SPEED
	 */
	protected float[] multipliers = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	protected int[] bonuses = new int[10];

	public static Player get() {
		if (player == null) {
			try {
				player = new Player(Spritesheet.getSpriteSheet("PlayerSprite"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return player;
	}

	public static Player getNullable() {
		return player;
	}

	public static void changePlayer(Player newPlayer) {
		if (getNullable() == player) {
			return;
		}
		player.toGhost();

		player = newPlayer;

		try {
			player.createBars();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Player(Spritesheet sprite) throws IOException {
		super(sprite, null, 0, DEFAULT_MAX_HP, DEFAULT_SPEED, true);
		cameraX = 0;
		cameraY = 0;

		setHitbox(new Rectangle(w / 5, (int) (h / 2 - (1f/6f) * h), (int) (w * 0.6f), (int) ((1f/3f) * h)));
		setLayer(5);
		setEssential(true);
		createBars();

		BufferedImage slotSprite = ImageIO.read(new File("Sprites/InventorySlot.png"));
		loadouts = new ArrayList<>();
		
		
		for (int i = 0; i < 3; i++) {
			Equipment eqp = new Equipment();
			eqp.setLocation(-Globals.SCREEN_WIDTH, -Globals.SCREEN_HEIGHT);
			loadouts.add(eqp);
			if (i > 0) {
				loadouts.get(i).kill();
			} 
		}
		equipmentInvIndex = 0;

		class PlayerInventory extends Inventory<Item> implements ActionListener {

			private float rotDeg = 0.25f;
			private Text txt = new Text("");
			private Color color = Globals.DEFAULT_COLOR;
			private int timesPerformed = 0;

			public PlayerInventory() {
				super(new Dimension(10, 8), slotSprite, Item.class);
			}

			@Override
			public void addItem(Item item) {
				if (player.getCursorSlot() == null) {
					int count = 0;
					rotDeg = -rotDeg;
					if (item instanceof StackableItem oStack) {
						count += oStack.getCount();
					}
					txt.setText(item.getName() + (count > 0 ? " (" + count + ")" : ""));
					txt.setLocation(get().getX() - 10, get().getY() - 10);
					color = Globals.DEFAULT_COLOR;
					timesPerformed = 0;
					Globals.GLOBAL_TIMER.addActionListener(this);
				}

				super.addItem(item);
			}

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				txt.rotate(rotDeg);
				if (timesPerformed >= 20) {
					if (color.getAlpha() - 10 > 0) {
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() - 10);
					} else {
						txt.kill();
						Globals.GLOBAL_TIMER.removeActionListener(this);
						txt = new Text("");
						return;
					}
				}
				txt.setColor(color);
				timesPerformed++;
			}
		}
		
		inventory = new PlayerInventory();
		inventory.setEssential(true);
		inventory.setMovable(false);
		inventory.kill();
		Globals.GLOBAL_TIMER.addActionListener((PlayerInventory) inventory);

		researchedRecipes = new ArrayList<>();

		BufferedImage redTint = new BufferedImage(Globals.SCREEN_WIDTH, 
		Globals.SCREEN_HEIGHT, 
		  BufferedImage.TYPE_INT_ARGB);
		Graphics g = redTint.getGraphics();
		Color bg = new Color(255, 0, 0, 100);
		g.setColor(bg);
		g.fillRect(0, 0, Globals.WIDTH, Globals.HEIGHT);
		Color textColor = new Color(255, 145, 145);
		g.setColor(textColor);
		g.setFont(new java.awt.Font(null, Globals.DEFAULT_FONT.getStyle(), 60));
		g.drawString("You Died!", Globals.SCREEN_WIDTH / 2 - 257 / 2, Globals.SCREEN_HEIGHT / 2 - 44 / 2);
		g.dispose();
		
		deathScreen = new GameObject(redTint);
		deathScreen.setEssential(true);
		deathScreen.kill();

		setLocation(Globals.WIDTH / 2, Globals.HEIGHT / 2);
		cameraX = x - Globals.NATIVE_SCREEN_WIDTH / 2;
		cameraY = y - Globals.NATIVE_SCREEN_HEIGHT / 2;
		healthBar.setLocation(cameraX + 50, cameraY + 50);
		manaBar.setLocation(healthBar.x, healthBar.y + 60);
		adrenalineBar.setLocation(manaBar.x + 40, manaBar.y + 20);
		inventory.setLocation(cameraX + 50, manaBar.y + manaBar.h + 10);
		getEquipmentInv().setLocations();
		setCursorPos(cursorX - x, cursorY - y);
	}
	
	private void createBars() throws IOException {
		BufferedImage ui = Globals.getImage("UI");
		BufferedImage empty = Globals.getImage("UIEmpty");
		healthBar = new HealthBar(empty.getSubimage(0, 332, 300, 54), ui.getSubimage(0, 332, 300, 54));
		adrenalineBar = new HealthBar(ImageIO.read(new File("Sprites/AdrenalineBarBack.png")),
		 ImageIO.read(new File("Sprites/AdrenalineBarFront.png"))) {
			
			@Override
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity));
				g2.drawImage(sprite, x, y, null);
				
				if (adren > 0) {
					float slivWidth = (float) w / (float) maxAdr;
					int fullSlivWidth = (int) (slivWidth * adren);
					if (fullSlivWidth < 1) fullSlivWidth++;
					g2.drawImage(frontSprite.getSubimage(0, 0, fullSlivWidth, h), x, y, null);
				}
				g2.drawString("+" + (int) (((float) adren / (float) maxAdr) * 100) + "%", x, y);
				g2.dispose();
			}
		 };
		manaBar = new HealthBar(empty.getSubimage(161, 80, 34, 128), ui.getSubimage(161, 80, 34, 128)) {
			@Override
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity));
				g2.drawImage(sprite, x, y, null);
				
				if (mana > 0) {
					float slivWidth = 95f / (float) maxMana;
					int fullSlivHeight = (int) (slivWidth * mana);
					if (fullSlivHeight < 1) fullSlivHeight++;
					g2.drawImage(frontSprite.getSubimage(0, frontSprite.getHeight() - fullSlivHeight, w, fullSlivHeight),
					  x, y + frontSprite.getHeight() - fullSlivHeight, null);
				}
				g2.dispose();
			}
		 };
	
		tpCooldown = new StatusWheel("timeSinceTP", DEFAULT_TP_COOLDOWN, Color.LIGHT_GRAY, Color.GRAY);
		dashCooldown = new StatusWheel("timeSinceDash", DEFAULT_DASH_DELAY, new Color(255, 158, 250), 
		  Color.MAGENTA);
		healWheel = new StatusWheel("healNum", maxHealNum, Color.RED, new Color(122, 0, 0)) {
			@Override
			public void paint(Graphics g) {
				
			}
		};
		
		healthBar.setEssential(true);
		adrenalineBar.setEssential(true);
		manaBar.setEssential(true);
		tpCooldown.setEssential(true);
		dashCooldown.setEssential(true);
	}

	@Override
	public void paint(Graphics g) {
		if (currentInvinc % 5 == 0) {
			super.paint(g);
		}
	}
	
	@Override
	public void update() {
		if (!isAlive()) {
			return;
		}

		List<Direction> newDirections = new ArrayList<>();
		for (int i = 0; i < Direction.values().length; i++) {
			Direction dir = Direction.values()[i];
			if (Globals.main.directionMap.get(dir) && isAlive()) {
				newDirections.add(dir);
			}
		}
		if (newDirections.size() > 0) {
			lastDirections = newDirections;
		}
		move(newDirections);

		/*for (int i = 0; i < Direction.values().length; i++) {
			Direction dir = Direction.values()[i];
			if (!currentAnimation.equals(getAnimation(i)) && lastDirections.size() == 1) {
				for (Direction active : lastDirections) {
					if (dir == active) {
						setAnimation(i);
					}
				}
			} 
			else if (lastDirections.size() == 2) {
				Direction[] arr = {lastDirections.get(0), lastDirections.get(1)};
				for (int j = 0; j < Direction.getCompoundDirections().length; j++) {
					Direction[] dirArr = Direction.getCompoundDirections()[j];
					if (Arrays.equals(arr, dirArr) && !currentAnimation.equals(getAnimation(4 + j))) {
						setAnimation(4 + j);
					}
				}
			}
			if (!currentAnimation.active()) {
				currentAnimation.start();
			}
		}
		if (lastDirections.size() == 0 && currentAnimation.getFrameIndex() == currentAnimation.getNumOfFrames() / 2)
			currentAnimation.stop();*/

		if (firing()) fire();
		incrementInvinc();
		
		if (cursorSlot != null) cursorSlot.setLocation(cursorX(), cursorY());

		if (timeSinceHit >= hitToRegenDelay && hp < maxHP && timeSinceHit % regenDelay == 0) {
			hp++;
			if (regenDelay > minRegenDelay) regenDelay -= regenDecreaseRate;
			if (regenDelay < minRegenDelay) regenDelay = minRegenDelay;
		}

		Weapon wep = getEquipmentInv().getWepSlot().getItem();
		if (timeSinceHit > hitToAdrDelay && timeSinceHit % 3 == 0) {
			if (Globals.getGameState().combat() && adren < maxAdr)	{
				adren++;
			} else if (!Globals.getGameState().combat() && adren > 0) {
				adren = 0;
			}
			if (wep != null) {
				wep.setWepDMG((int) ((1 + (float) adren / (float) maxAdr) * (wep.getDefaultWepDMG() * multipliers[1] + bonuses[1])));
				wep.updateData();
			}
		} else if (timeSinceHit == 0 && wep != null) {
			wep.updateData();
		}
		
		if (!Globals.alwaysShowUI && 
		  timeSinceUI >= UI_HIDE_COOLDOWN && 
		  !Globals.getGameState().combat() && !inventory.isAlive()) {
			GameObject[] objs = {healthBar, adrenalineBar, manaBar, tpCooldown, dashCooldown,
			  loadouts.get(0), loadouts.get(1), loadouts.get(2)};
			int numDone = 0;
			for (GameObject obj : objs) {
				if (obj.opacity - 0.05f >= 0) {
					obj.setOpacity(obj.opacity - 0.05f);	
				} else {
					numDone++;
					obj.kill();
				}
			}
			if (numDone == objs.length) {
				timeSinceUI = -1;
			}
		} else if (!Globals.alwaysShowUI && timeSinceUI == 0) {
			showUI();
		}

		if (readyToKill()) { 
			gameKill();
		}

		currentFire++;
		timeSinceHit++;
		timeSinceTP++;
		timeSinceDash++;
		if (timeSinceUI >= 0) {
			timeSinceUI++;
		}
	}
	
	private static void translateCamera(int dx, int dy) {
		cameraX += dx;
		cameraY += dy;

		for (UI ui : UI.allUI) {
			ui.setLocation(ui.x + dx, ui.y + dy);
		}
		setCursorPos(cursorX + dx, cursorY + dy);
	}

	@Override
	public void setLocation(int newX, int newY) {
		Point point = new Point(newX - x, newY - y); 
		if (Globals.getGameState() == GameState.BOSS && Globals.main.getScene() instanceof Bossfight bossfight &&
		  Globals.lockScreen) {
			if (bossfight.getAnchorX() != cameraX && bossfight.getAnchorY() != cameraY) {
				translateCamera(-cameraX + bossfight.getAnchorX(), -cameraY + bossfight.getAnchorY());
				getEquipmentInv().setLocations();
			}
		} else {
			translateCamera(-cameraX + getCenterX() - Globals.SCREEN_WIDTH / 2 + point.x,
			  -cameraY + getCenterY() - Globals.SCREEN_HEIGHT / 2 + point.y);
			getEquipmentInv().setLocations();
		}
		super.setLocation(newX, newY);
	}
	
	private boolean slowActivated = false;
	public void activateAbility(int abilityNum, boolean activating) {
		Ability.Type type = getEquipmentInv().getAbilityType(abilityNum);
		if (type == null) {
			return;
		}
		switch (type) {
			case DASH:
				if (activating) {
					dash();
				}
				break;
			case HEAL:
				if (activating) {
					heal();
				}
				break;
			case SLOW:
				if (activating && !slowActivated) {
					multipliers[9] /= 2.5f;
					applyStatChanges(multipliers, bonuses);
					slowActivated = true;
				} else if (!activating && slowActivated) {
					multipliers[9] *= 2.5f;
					applyStatChanges(multipliers, bonuses);
					slowActivated = false;
				}
				break;
			case TELEPORT:
				if (activating) {
					tp(Player.cursorX() - w / 2, Player.cursorY() - h / 2);
				}
				break;
		}
	}

	private void tp(int x, int y) {
		if (timeSinceTP > DEFAULT_TP_COOLDOWN) {
			setLocation(x, y);
			timeSinceTP = 0;
		}
	}

	private void dash() {
		if (timeSinceDash < dashDelay) {
			return;
		}
		timeSinceDash = 0;
		dashInvinc = true;

		Globals.GLOBAL_TIMER.addActionListener(new ActionListener() {
			int timesPerformed = 0;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (timesPerformed == dashLength) {
					dashInvinc = false;
					Globals.GLOBAL_TIMER.removeActionListener(this);
					return;
				}
				if (lastDirections.size() > 0) {
					speed *= 5;
					move(lastDirections);
					speed /= 5;
				}
				timesPerformed++;
			}
		});
	}

	private void heal() {
		if (healNum > 0) {
			this.hp += DEFAULT_HEAL_AMOUNT;
			if (hp > maxHP || hp < 0) {
				hp = maxHP;
			}
			healNum--;
		}
	}

	@Override
	public boolean readyToKill() {
		return hp <= 0;
	}

	public void hideUI() {
		if (inventory.isAlive()) {
			Globals.eAction.toggle();
		}
	}

	public void killUI() {
		GameObject[] objs = {healthBar, adrenalineBar, manaBar, tpCooldown, dashCooldown, 
			loadouts.get(0), loadouts.get(1), loadouts.get(2)};
		for (GameObject obj : objs) {
			obj.kill();
		}
	}

	public void showUI() {
		if (healthBar.isAlive() && healthBar.opacity == 1) {
			return;
		}
		GameObject[] objs = {healthBar, adrenalineBar, manaBar, tpCooldown, dashCooldown, getEquipmentInv()};
		for (GameObject obj : objs) {
			obj.revive();
			obj.setOpacity(0);
		}
		Globals.GLOBAL_TIMER.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int numDone = 0;
				for (GameObject obj : objs) {
					if (obj.opacity + 0.1f <= 1) {
						obj.setOpacity(obj.opacity + 0.1f);	
					} else {
						obj.setOpacity(1);
						numDone++;
					}
				}
				if (numDone == objs.length) {
					Globals.GLOBAL_TIMER.removeActionListener(this);
				}
			}
		});
	}

	@Override
	public void kill() {
		killUI();

		timeSinceDash = DEFAULT_DASH_DELAY;
		timeSinceHit = 0;
		timeSinceTP = DEFAULT_TP_COOLDOWN;
		currentFire = 0;
		currentInvinc = 0;
		invincible = false;
		isAlive = false;
		firing = false;
		slowActivated = false;
		
		for (Direction dir : Globals.main.directionMap.keySet()) {
			Globals.main.directionMap.put(dir, false);
		}
	}

	private void gameKill() {
		kill();
		try {
			if (new File("Data\\PlayerData.dat").exists()) {
				SaveSystem.writePlayerData(true);
				SaveSystem.writeEntityData();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		deathScreen.setLocation(cameraX, cameraY);
		deathScreen.revive();
		Globals.main.endGame();
	}

	@Override
	public void revive() {
		showUI();

		isAlive = true;
		hp = maxHP;
		deathScreen.kill();
		invincible = false;
		currentInvinc = 0;
	}
	
	@Override
	public boolean onCollision(GameSolid obj) {
		if (obj instanceof GameSolid && !(obj instanceof Entity)) {
			int xDif = 0; 
			int yDif = 0;
			for (Direction dir : lastDirections) {
				if (obj.getHitbox().intersects(new Rectangle((int) (lastX + (dir.xDif * speed)), 
				(int) (lastY + (dir.yDif * speed)), w, h))) {
					xDif += dir.xDif;
					yDif += dir.yDif;
				}
			}

			if (xDif == 0 && yDif == 0) {
				for (Direction dir : lastDirections) {
					xDif += dir.xDif;
					yDif += dir.yDif;
				}
			}

			while (collidedWith(obj)) {
				setLocation(x - xDif, y - yDif);
			}

			return true;
		}
		return false;
	}
	
	public void fire() {
		Weapon wep = getEquipmentInv().getWepSlot().getItem(); 
		if (wep == null || currentFire < wep.getFireTime()) {
			return;
		} 
		if (mana >= wep.getManaCost()) {
			wep.attack();
			mana -= wep.getManaCost();
		}
	}

	public void registerDealtDMG(int dmg, GameSolid sender) {
		if (sender instanceof Projectile) {
			return;
		}
		mana += dmg;
		if (mana > maxMana) {
			mana = maxMana;
		}
	}

	@Override
	public void registerDMG(int dmg) {
		if (dashInvinc) {
			return;
		}
		hp -= dmg;
		setInvincible(true);
		timeSinceHit = 0;
		regenDelay = maxRegenDelay;
		adren = 0;
	}

	public void move(List<Direction> dir) {
		float xDif = 0;
		float yDif = 0;
		for (int i = 0; i < dir.size(); i++) {
			xDif += dir.get(i).xDif;
			yDif += dir.get(i).yDif;
		}

		if (xDif != 0 && yDif != 0) {
			xDif /= 1.25f;
			yDif /= 1.25f;
		}

		double localSpeed = speed;
		int newX = (int) (x + xDif * localSpeed);
		int newY = (int) (y + yDif * localSpeed);

		if (newX < 0) {
			newX = 0;
		} else if (newX > Globals.WIDTH - w) {
			newX = Globals.WIDTH - w;
		}
		
		if (newY < 0) {
			newY = 0;
		} else if (newY > Globals.HEIGHT - h) {
			newY = Globals.HEIGHT - h;
		}
		
		setLocation(newX, newY);
	}

	public void applyStatChanges(float[] newMultipliers, int[] newBonuses) {
		Weapon wep = getEquipmentInv().getWepSlot().getItem();
		
		float[] stats = {DEFAULT_MAX_HP, 0, 0, 0, DEFAULT_INVINC_TIME, DEFAULT_MIN_REGEN_DELAY, DEFAULT_REGEN_DECREASE_RATE,
		DEFAULT_HIT_TO_REGEN_DELAY, 0, DEFAULT_SPEED};
		if (wep != null) {
			stats[1] = wep.getDefaultWepDMG();
			stats[2] = wep.getDefaultFireTime();
			stats[3] = wep.getDefaultRange();
			stats[8] = wep.getDefaultShotSpeed();
		}

		for (int i = 0; i < multipliers.length; i++) {
			if (wep == null && (i == 1 || i == 2 || i == 3 || i == 8)) {
				continue;
			}
			
			float value = stats[i] * newMultipliers[i] + newBonuses[i];
			switch(i) {
				case 0:
					if (hp > value) hp = (int) value;
					maxHP = (int) value;
					break;
				case 1:
					wep.setWepDMG((int) value);
					break;
				case 2:
					wep.setFireTime((int) value);
					break;
				case 3:
					wep.setRange((int) value);
					break;
				case 4:
					invincTime = (int) value;
					break;
				case 5:
					minRegenDelay = (int) value;
					break;
				case 6:
					regenDecreaseRate = (int) value;
					break;
				case 7:
					hitToRegenDelay = (int) value;
					break;
				case 8:
					wep.setShotSpeed(value);
					break;
				case 9:
					speed = value;
					break;
			}
		}

		multipliers = newMultipliers;
		bonuses = newBonuses;

		if (wep != null) {
			wep.updateData();
		}
	}

	public void incrementInvinc() {
		if (isInvicible()) {
			currentInvinc++;
		}
		if (currentInvinc >= invincTime || !isInvicible()) {
			invincible = false;
			currentInvinc = 0;
		}
	}
	
	public boolean hasRecipe(Recipe recipe) {
		return researchedRecipes.contains(recipe);
	}

	public void addRecipe(Recipe recipe) {
		if (!researchedRecipes.contains(recipe)) {
			researchedRecipes.add(recipe);
		}
	}

	public void addRecipes(Recipe[] recipes) {
		for (Recipe recipe : recipes) {
			addRecipe(recipe);
		}
	}

	public void select(Item item) {
		cursorSlot = item;
	}

	public static boolean cursorOver(Rectangle rect) {
		return rect.contains(cursorX, cursorY);
	}

	public void changeLoadout(int loadoutNum) {
		timeSinceUI = 0;
		if (loadoutNum < 0 || loadoutNum >= loadouts.size()) {
			return;
		}
		getEquipmentInv().kill();
		loadouts.forEach(obj -> obj.setOpacity(1));
		equipmentInvIndex = loadoutNum;
		getEquipmentInv().revive();
		getEquipmentInv().setLocations();
	}

	public void setFiring(boolean firing) { this.firing = firing; }
	
	public boolean firing() { return firing; }
	
	public GameObject getDeathScreen() { return deathScreen; }
	
	public HealthBar getHealthBar() { return healthBar; }
	public HealthBar getAdrenalineBar() { return adrenalineBar; }
	public void resetAdren() { this.adren = 0; }

	public Inventory<Item> getInventory() { return inventory; }
	public List<Recipe> getResearchedRecipes() { return researchedRecipes; }

	public Equipment getEquipmentInv() { return loadouts.get(equipmentInvIndex); }

	public Item getCursorSlot() { return cursorSlot; }

	public List<Equipment> getLoadouts() { return loadouts; }

	public int getHealNum() { return healNum; }
	public void resetHeals() { healNum = maxHealNum; }

	public static void setCursorPos(int x, int y) {
		cursorX = x;
		cursorY = y;
	}

	public static void setCameraPos(int x, int y) {
		translateCamera(x - cameraX, y - cameraY);
	}

	public static int cameraX() { return cameraX; }
	public static int cameraY() { return cameraY; }

	public int getTimeSinceUI() { return timeSinceUI; }
	/**
	 * 
	 * @param timeSinceUI set to 0 to show UI and then hide it, set to 1 if UI is active and will be hidden, set to -1
	 * to retain current UI state.
	 */
	public void setTimeSinceUI(int timeSinceUI) { this.timeSinceUI = timeSinceUI; }

	public static int cursorX() { return cursorX; }
	public static int cursorY() { return cursorY; }

	public int getCurrentFire() { return currentFire; }
	public void setCurrentFire(int currentFire) { this.currentFire = currentFire; }

	public float[] getMultipliers() { return multipliers; }
	public int[] getBonuses() { return bonuses; }
	
	public static class Equipment extends UI {
		
		public static final float 
		  WEP_SCALE_FACTOR = 2.5f,
		  CORE_SCALE_FACTOR = 1.25f,
		  ABILITY_SCALE_FACTOR = 0.5f;
		
		static int accSlotNum = 4;
		static int abilitySlotNum = 3;

		private int 
		  wepX, wepY,
		  accX, accY,
		  armorX, armorY,
		  coreX, coreY;

		@SuppressWarnings("unchecked")
		private static Container<Accessory>[] accSlots = new EquipmentContainer[accSlotNum];
		@SuppressWarnings("unchecked")
		private static Container<Ability>[] abilitySlots = new EquipmentContainer[abilitySlotNum];
		private Container<Weapon> wepSlot;
		private static Container<Core> coreSlot;

		private static BufferedImage abilityBackground;
		
		public Equipment() {
			super(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB), 100);
			
			final BufferedImage ui = Globals.getImage("UI");
			final BufferedImage emptyUI = Globals.getImage("UIEmpty");
			final Rectangle charmBounds = new Rectangle(0, 204, 64, 64);
			final Rectangle wepBounds = new Rectangle(0, 45, 160, 160);
			final Rectangle coreBounds = new Rectangle(160, 0, 80, 80);
			final Rectangle abilityBounds = new Rectangle(0, 0, 160, 44);

			abilityBackground = ui.getSubimage(abilityBounds.x, abilityBounds.y, abilityBounds.width, abilityBounds.height);

			setEssential(true);
			
			wepSlot = new EquipmentContainer<>(
				ui.getSubimage(wepBounds.x, wepBounds.y, wepBounds.width, wepBounds.height),
				emptyUI.getSubimage(wepBounds.x, wepBounds.y, wepBounds.width, wepBounds.height),
				Weapon.class);
			
			if (coreSlot != null) {
				return;
			}
			
			for (int i = 0; i < accSlotNum; i++) {
				accSlots[i] = new EquipmentContainer<>(
					ui.getSubimage(charmBounds.x, charmBounds.y, charmBounds.width, charmBounds.height), 
					emptyUI.getSubimage(charmBounds.x, charmBounds.y, charmBounds.width, charmBounds.height),
					Accessory.class);
			}
			for (int i = 0; i < abilitySlotNum; i++) {
				abilitySlots[i] = new EquipmentContainer<>(
					ui.getSubimage(12 + i * 44, 6, 32, 32),
					emptyUI.getSubimage(12 + i * 44, 6, 32, 32),
					Ability.class);
			}
			coreSlot = new EquipmentContainer<>(
				ui.getSubimage(coreBounds.x, coreBounds.y, coreBounds.width, coreBounds.height),
				emptyUI.getSubimage(coreBounds.x, coreBounds.y, coreBounds.width, coreBounds.height),
				Core.class);

			setLocations();
		}

		@Override
		public void paint(Graphics g) {
			if (this != player.getEquipmentInv()) return;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity));
			
			g2.drawImage(abilityBackground, armorX, armorY, null);
			for (int i = 0; i < accSlotNum; i++) {
				accSlots[i].paint(g2);
			}
			for (int i = 0; i < abilitySlotNum; i++) {
				abilitySlots[i].paint(g2);
			}
			wepSlot.paint(g2);
			coreSlot.paint(g2);

			if (player.cursorSlot != null) {
				player.cursorSlot.paint(g2);
			}
		}

		@Override
		public void toGhost() {
			super.toGhost();
			Arrays.stream(abilitySlots).forEach((arm) -> arm.toGhost());
			wepSlot.toGhost();
			coreSlot.toGhost();
		}

		@Override
		public void unghost() {
			super.unghost();
			Arrays.stream(accSlots).forEach((acc) -> Container.containers.add(acc));
			Arrays.stream(abilitySlots).forEach((arm) -> Container.containers.add(arm));
			Container.containers.add(wepSlot);
			Container.containers.add(coreSlot);
		}

		@Override
		public void kill() {
			isAlive = false;
			wepSlot.kill();
			coreSlot.kill();
			for (int i = 0; i < accSlotNum; i++) {
				accSlots[i].kill();
			}
			for (int i = 0; i < abilitySlotNum; i++) {
				abilitySlots[i].kill();
			}
		}

		@Override
		public void revive() {
			isAlive = true;
			wepSlot.revive();
			coreSlot.revive();
			for (int i = 0; i < accSlotNum; i++) {
				accSlots[i].revive();
			}
			for (int i = 0; i < abilitySlotNum; i++) {
				abilitySlots[i].revive();
			}
		}

		public boolean equals(Equipment equip) {
			boolean accSuccess = true;
			for (int i = 0; i < accSlots.length; i++){ 
				if (!accSlots[i].equals(accSlots[i])) {
					accSuccess = false;
					break;
				}
			}
			boolean armSuccess = true;
			for (int i = 0; i < abilitySlots.length; i++){ 
				if (!abilitySlots[i].equals(abilitySlots[i])) {
					armSuccess = false;
					break;
				}
			}

			return super.equals(equip) && accSuccess && armSuccess && wepSlot.equals(equip.wepSlot) &&
			  coreSlot.equals(coreSlot);
		}

		public boolean hasAbility(Ability.Type abilityType) {
			return Arrays.stream(abilitySlots).anyMatch(obj -> obj.getItem() != null && obj.getItem().getType() == abilityType);
		}

		public Ability.Type getAbilityType(int index) {
			return abilitySlots[index].getItem() != null ? abilitySlots[index].getItem().getType() : null;
		}

		public void setLocations() {
			wepX = cameraX;
			wepY = cameraY + Globals.SCREEN_HEIGHT - (int) (h * WEP_SCALE_FACTOR);
			wepSlot.setLocation(wepX, wepY);

			accX = cameraX + (int) (w * WEP_SCALE_FACTOR);
			accY = cameraY + Globals.SCREEN_HEIGHT - h;
			for (int i = 0; i < accSlotNum; i++) {
				accSlots[i].setLocation(accX + w * i, accY);
			}

			armorX = wepX;
			armorY = wepY - (int) (h * ABILITY_SCALE_FACTOR) - 10;
			for (int i = 0; i < abilitySlotNum; i++) {
				abilitySlots[i].setLocation(armorX + (int) (w * ABILITY_SCALE_FACTOR) * i + (i * 12) + 12, armorY + 5);
			}
			setLocation(armorX, armorY);

			coreX = cameraX + (int) (w * WEP_SCALE_FACTOR);
			coreY = cameraY + Globals.SCREEN_HEIGHT - h - (int) (h * CORE_SCALE_FACTOR);
			coreSlot.setLocation(coreX, coreY);
		}

		public Container<Weapon> getWepSlot() { return wepSlot; }
		public Container<Accessory>[] getAccSlots() { return accSlots; }
		public Container<Ability>[] getAbilitySlots() { return abilitySlots; }
		public Container<Core> getCoreSlot() { return coreSlot; }
		
		public void clear() {
			wepSlot.setItem(null);
			coreSlot.setItem(null);
			Arrays.stream(abilitySlots).forEach(cont -> cont.setItem(null));
			Arrays.stream(accSlots).forEach(cont -> cont.setItem(null));
			updateModifiers();
		}

		public void setWepSlot(Weapon wepSlotItem) {
			if (wepSlot.getItem() != null) {
				wepSlot.getItem().resetStats();
				wepSlot.getItem().updateData();
			}
			wepSlot.setItem(wepSlotItem);
			updateModifiers();
		}

		public void setAccSlot(int index, Accessory accSlotItem) { 
			accSlots[index].setItem(accSlotItem);
			updateModifiers();
		}

		public void setCoreSlot(Core newCoreItem) { 
			coreSlot.setItem(newCoreItem);
			updateModifiers();
		}

		public void setAbilitySlot(int index, Ability abilitySlotItem) {
			abilitySlots[index].setItem(abilitySlotItem);
			updateModifiers();
		}

		public void updateModifiers() {
			player.getEquipmentInv().setLocations();
			
			float[] multResult = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
			int[] bonusResult = new int[10];

			for (int i = 0; i < multResult.length; i++) {
				for (int j = 0; j < accSlotNum; j++) {
					if (accSlots[j].getItem() != null) {
						multResult[i] *= accSlots[j].getItem().getMultipliers()[i];
						bonusResult[i] += accSlots[j].getItem().getBonuses()[i];
					}
				}

				if (i == 9) {
					for (int j = 0; j < abilitySlotNum; j++) {
						if (abilitySlots[j].getItem() != null && abilitySlots[j].getItem().getType() == Ability.Type.SLOW) {
							multResult[i] *= SLOW_CHARM_SPEED_MULTIPLIER;
						}
					}
				}
				if (coreSlot.getItem() != null) {
					multResult[i] *= coreSlot.getItem().getMultipliers()[i];
					bonusResult[i] += coreSlot.getItem().getBonuses()[i];
				}
			}
			player.applyStatChanges(multResult, bonusResult);
		}

		public int emptySlots() {
			int result = 0;
			if (wepSlot.getItem() == null) result++;
			if (coreSlot.getItem() == null) result++;
			for (int i = 0; i < accSlotNum; i++) {
				if (accSlots[i].getItem() == null) result++;
			}
			for (int i = 0; i < abilitySlotNum; i++) {
				if (abilitySlots[i].getItem() == null) result++;
			}
			return result;
		}

		public int numberOfItems() {
			return abilitySlotNum + accSlotNum + 2 - emptySlots();
		}

		private class EquipmentContainer<T extends Item> extends Container<T> {

			private BufferedImage mainSprite;
			private BufferedImage altSprite;

			EquipmentContainer(BufferedImage mainSprite, BufferedImage altSprite, Class<T> itemClass) {
				super(mainSprite, itemClass);
				this.mainSprite = mainSprite;
				this.altSprite = altSprite;
				setLayer(101);
				toGhost();
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean moveItem(boolean taking, Inventory<? super T> inventory) {
				Player player = Player.get();
				Class<T> itemClass = getItemClass();
				
				if (player.getCursorSlot() != null) {
					for (int i = 0; i < accSlotNum; i++) {
						if (this == accSlots[i]) {
							for (int j = 0; j < accSlotNum; j++) {
								if (player.getCursorSlot().equals(accSlots[j].getItem())) {
									return false;
								}
							}
						}
					}
				}

				if (getItem() != null) {
					if (this == wepSlot) {
						((Weapon) getItem()).resetStats();
						((Weapon) getItem()).updateData();
					}

					if (player.getCursorSlot() == null) {
						player.select(getItem());
						setItem(null);
					} else if (itemClass.isInstance(player.getCursorSlot())) {
						T temp = (T) player.getCursorSlot();
						player.select(getItem());
						setItem(temp);
					}
				} else if (!taking && player.getCursorSlot() != null && itemClass.isInstance(player.getCursorSlot())) {
					setItem((T) player.getCursorSlot());
					player.select(null);
				}
				updateModifiers();

				if (getItem() != null) {
					setSprite(altSprite);
				} else {
					setSprite(mainSprite);
				}
				return true;
			}

			@Override
			public void setItem(T item) {
				super.setItem(item);
				if (getItem() != null) {
					setSprite(altSprite);
				} else {
					setSprite(mainSprite);
				}
			}
		}
	}

	private class StatusWheel extends UI {

		private static List<StatusWheel> instances = new ArrayList<>();
		private static int numActive;
		
		private Field field;
		private int maxValue;
		private Color fColor;
		private Color bColor;

		public StatusWheel(String fieldName, int maxValue, Color frontColor, Color backColor) {
			super(null, 99);
			
			this.maxValue = maxValue;

			try {
				field = Player.class.getDeclaredField(fieldName);
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}

			fColor = frontColor;
			bColor = backColor;
			instances.add(this);
		}

		public static void staticPaint(Graphics2D g2d) {
			numActive = 0;
			for (StatusWheel wheel : instances) {
				wheel.setLocation(player.manaBar.x, player.manaBar.y);
				
				try {
					int value = (Integer) wheel.field.get(player);
	
					if ((!player.getEquipmentInv().hasAbility(Ability.Type.HEAL) && wheel == player.healWheel) || 
					  (value >= wheel.maxValue && wheel != player.healWheel)) {
						continue;
					}
					g2d.setColor(wheel.bColor);
					g2d.fillOval(wheel.x + numActive * 75, wheel.y + player.manaBar.h + 10, 50, 50);
					g2d.setColor(wheel.fColor);
					g2d.fillArc(wheel.x + numActive * 75, wheel.y + player.manaBar.h + 10, 50, 50, 0, 
						(int) ((float) value / (float) wheel.maxValue * 360));
					
					g2d.setColor(Globals.DEFAULT_COLOR);
					if (wheel != player.healWheel) {
						String healTime = Float.toString((float) ((wheel.maxValue - value) * Globals.TIMER_DELAY) / 1000);
						String result = healTime.substring(0, healTime.indexOf('.') + 2);
						g2d.drawString(result + "s", wheel.x + numActive * 75, wheel.y + player.manaBar.h + 85);
					} else {
						g2d.drawString(Integer.toString(player.healNum), 
						  wheel.x + numActive * 75 + 19, wheel.y + player.manaBar.h + 80);
					}

					numActive++;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void paint(Graphics g) {
			staticPaint((Graphics2D) g);
		}

	}

	public static class HealthBar extends UI {

		protected transient BufferedImage frontSprite;

		public HealthBar(BufferedImage backSprite, BufferedImage frontSprite) throws IOException {
			super(backSprite, 99);
			this.frontSprite = frontSprite;
		}
		
		@Override
		public void setLocation(int x, int y) {
			super.setLocation(x, y);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (player.hp > 0) {
				float slivWidth = (float) w / (float) player.maxHP;
				int fullSlivWidth = (int) (slivWidth * player.hp);
				if (fullSlivWidth < 1) fullSlivWidth++;
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, opacity));
				g2.drawImage(frontSprite.getSubimage(0, 0, fullSlivWidth, h), x, y, null);
				g2.setColor(Color.WHITE);
				g2.drawString(player.hp + "/" + player.maxHP, x + 70, y + 18);
				g2.dispose();
			}
		}
	}

	@Override
	public Entity clone() {
		throw new UnsupportedOperationException();
	}

}