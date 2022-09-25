package bullethell;

import static bullethell.Globals.DEFAULT_COLOR;
import static bullethell.Globals.DEFAULT_FONT;
import static bullethell.Globals.GLOBAL_TIMER;
import static bullethell.Globals.KEY_MAP;
import static bullethell.Globals.NATIVE_SCREEN_HEIGHT;
import static bullethell.Globals.NATIVE_SCREEN_WIDTH;
import static bullethell.Globals.PAINT_QUADTREE;
import static bullethell.Globals.eAction;
import static bullethell.Globals.frame;
import static bullethell.Globals.freezeCursor;
import static bullethell.Globals.freezeHotkeys;
import static bullethell.Globals.main;
import static bullethell.Globals.mouseDown;
import static bullethell.Globals.player;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import bullethell.enemies.Enemy;
import bullethell.enemies.EnemyID;
import bullethell.items.ItemID;
import bullethell.movement.Direction;
import bullethell.scenes.MainMenu;
import bullethell.scenes.OptionsMenu;
import bullethell.scenes.Scene;
import bullethell.ui.Inventory;
import bullethell.ui.UI;

public final class GameDisplay extends JPanel {
	
	private static Cursor cursor;
	private static Cursor blankCursor;

	public final EnumMap<Direction, Boolean> directionMap = new EnumMap<>(Direction.class);

	public MouseMoveListener mouseMoveListener;

	private Scene scene;

	public void gameStart() throws IOException {
		mouseMoveListener.locationChange(java.awt.MouseInfo.getPointerInfo().getLocation());

		SaveSystem.loadWorld(true);
		player.setLocation(Globals.SCREEN_WIDTH / 2, Globals.SCREEN_HEIGHT / 2);
		player.setLocation(Globals.WIDTH / 2, 100);
		player.getInventory().addItem(ItemID.DASH_ABILITY.getItem());
		player.getInventory().addItem(ItemID.HEAL_ABILITY.getItem());
		player.getInventory().addItem(ItemID.TP_ABILITY.getItem());
		player.getInventory().addItem(ItemID.FOCUS_ABILITY.getItem());
		player.getInventory().addItem(ItemID.ADRENALINE_ABILITY.getItem());
		player.getInventory().addItem(ItemID.TRIPLE_KNIFE.getItem());

		Enemy ent = EnemyID.getEnemy(EnemyID.PIXIE);
		ent.setLocation(500, 200);
		ent.addStatusEffect(new StatusEffect(10, 500, 2000));
	}
	
	protected void endGame() {
		Globals.freezeHotkeys = true;
		Entity.removeAll(null);
		if (scene != null) {
			scene.end();
		}
		for (Audio audio : Audio.values()) {
			if (audio.getClip().isOpen()) {
				Globals.stopsound(audio);
			}
		}

		eAction.turningOn = true;
	}

	public void setScene(Scene newScene, int x, int y) {
		if (scene != null) {
			scene.end();
		}
		scene = newScene;
		if (newScene != null) {
			newScene.start(x, y);
		}
	}

	public Scene getScene() { 
		return scene;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics g2 = g.create();
		g2.setFont(DEFAULT_FONT);
		g2.setColor(DEFAULT_COLOR);
		g2.translate(-Player.cameraX(), -Player.cameraY());
		GameObject.updateAll(g2);
		if (PAINT_QUADTREE) GameSolid.quadtree.paint(g2);
	}

	private void setMouseMovements() {
		mouseMoveListener = new MouseMoveListener();
		
		frame.addMouseMotionListener(mouseMoveListener);
		frame.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (freezeHotkeys) {
					return;
				}
				
				if (updateMap(e, true) && e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3) {
					return;
				}

				mouseDown = true;

				if (freezeCursor) {
					return;
				} 

				UI.uiCheck(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (freezeHotkeys) {
					return;
				}
				updateMap(e, false);
				
				mouseDown = false;
				mouseMoveListener.moveInv = null;
				if (e.getButton() == MouseEvent.BUTTON1 || !frame.isFocused()) player.setFiring(false);
			}

			private boolean updateMap(MouseEvent e, boolean pressed) {
				Integer[] keyset = KEY_MAP.keySet().toArray(new Integer[0]);
				for (int i = 0; i < keyset.length; i++) {
					if (KEY_MAP.get(keyset[i]) || keyset[i] != e.getButton()) {
						continue;
					}
					switch (i) {
						case 0:
							directionMap.put(Direction.UP, pressed);
							return true;
						case 1:
							directionMap.put(Direction.LEFT, pressed);
							return true;
						case 2:
							directionMap.put(Direction.DOWN, pressed);
							return true;
						case 3:
							directionMap.put(Direction.RIGHT, pressed);
							return true;
						case 4:
							if (pressed) {
								eAction.toggle();
							}
							return true;
						case 5:
							player.activateAbility(0, pressed);
							return true;
						case 6:
							player.activateAbility(1, pressed);
							return true;
						case 7:
							player.activateAbility(2, pressed);
							return true;
						case 8:
							if (pressed) {
								player.changeLoadout(1);
							}
							return true;
						case 9:
							if (pressed) {
								player.changeLoadout(2);
							}
							return true;
						case 10:
							if (pressed) {
								player.changeLoadout(0);
							}
							return true;
					}
				}
				return false;
			}
		});
		frame.addMouseWheelListener(e -> {
			int direction = e.getWheelRotation();
			if (direction < 0) {
				player.changeLoadout(player.equipmentInvIndex + 1);
			} else {
				player.changeLoadout(player.equipmentInvIndex - 1);
			}
		});
	}

	private void setMovementKeys() {
		for (Direction i : Direction.values()) {
			directionMap.put(i, false);
		}
		frame.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				updateMap(e, true);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				updateMap(e, false);
			}

			private void updateMap(KeyEvent e, boolean pressed) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && pressed) {
					if (!OptionsMenu.get().isActive() && !freezeHotkeys) {
						OptionsMenu.get().start(0, 0);
					}
					else if (OptionsMenu.get().isActive()) {
						OptionsMenu.get().end();
					}
				}
				
				if (freezeHotkeys) {
					return;
				}

				if (!player.isAlive() && e.getKeyCode() == KeyEvent.VK_SPACE) {
					SaveSystem.readData(false);
					player.revive();
					return;
				}

				Integer[] keyset = KEY_MAP.keySet().toArray(new Integer[0]);
				for (int i = 0; i < keyset.length; i++) {
					if (!KEY_MAP.get(keyset[i]) || keyset[i] != e.getKeyCode()) {
						continue;
					}
					switch (i) {
						case 0:
							directionMap.put(Direction.UP, pressed);
							return;
						case 1:
							directionMap.put(Direction.LEFT, pressed);
							return;
						case 2:
							directionMap.put(Direction.DOWN, pressed);
							return;
						case 3:
							directionMap.put(Direction.RIGHT, pressed);
							return;
					}
					switch (i) {
						case 4:
							if (pressed) {
								eAction.toggle();
							}
							return;
						case 5:
							player.activateAbility(0, pressed);
							return;
						case 6:
							player.activateAbility(1, pressed);
							return;
						case 7:
							player.activateAbility(2, pressed);
							return;
						case 8:
							if (pressed) {
								player.changeLoadout(0);
							}
							return;
						case 9:
							if (pressed) {
								player.changeLoadout(1);
							}
							return;
						case 10:
							if (pressed) {
								player.changeLoadout(2);
							}
							return;
					}
				}
			}
		});
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				main = new GameDisplay();
				player = Player.get();
				main.setVisible(true);
				main.setScene(MainMenu.get(), 0, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private GameDisplay() throws IOException {

		setFocusTraversalKeysEnabled(false);

		BufferedImage cursorImage = ImageIO.read(new File("sprites/Cursor.png"));
		cursor = getToolkit().createCustomCursor(cursorImage, new Point(), "cursor");
		blankCursor = getToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), 
		  new Point(), "blank cursor");
		
		setCursor(cursor);
		BufferedImage worldSprite = new BufferedImage(Globals.WIDTH, Globals.HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics g = worldSprite.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Globals.WIDTH, Globals.HEIGHT);
		g.dispose();
		
		GameObject world = new GameObject(worldSprite);
		world.setEssential(true);
		
		frame = new JFrame();
		frame.setUndecorated(true);
		frame.setContentPane(this);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setFocusTraversalKeysEnabled(false);
		frame.setFocusCycleRoot(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(0, 0, NATIVE_SCREEN_WIDTH, NATIVE_SCREEN_HEIGHT);
		frame.setVisible(true);

		setBackground(Color.WHITE);
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		setMouseMovements();
		setMovementKeys();
		GLOBAL_TIMER.addActionListener((ActionEvent e) -> {
			repaint();
			GameSolid.quadtreeCheck();
		});
		GLOBAL_TIMER.start();

		GLOBAL_TIMER.addActionListener(new ActionListener() {
			int lastX;
			int lastY;
			boolean flipped = false;
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Robot robot = new Robot();
					if (freezeCursor) {
						if (!flipped) {
							setCursor(blankCursor);
							lastX = Player.cursorX();
							lastY = Player.cursorY();
							for (Direction dir : directionMap.keySet()) {
								directionMap.put(dir, false);
							}
							player.getInventory().kill();
							eAction.turningOn = true;
						}
						flipped = true;
						robot.mouseMove(0, 0);
						Player.setCursorPos(0, 0);
					} else {
						if (flipped) {
							setCursor(cursor);
							robot.mouseMove(Player.cameraX() + lastX, Player.cameraY() + lastY);
							Player.setCursorPos(lastX, lastY);
						}
						flipped = false;
					}
				} catch (AWTException e1) { }
			}	
		});
	}

	public class MouseMoveListener implements MouseMotionListener {
		public Inventory<?> moveInv;
		private int lastX, lastY;
		@Override
		public void mouseDragged(MouseEvent e) {
			for (GameObject ui : UI.allUI) {
				if (ui instanceof Inventory<?> inv) {
					inv.move(e.getX(), e.getY(), lastX, lastY, moveInv);
				}
			}
			locationChange(e.getPoint());
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			locationChange(e.getPoint());
		}
		
		public void locationChange(Point point) {
			Player.setCursorPos((int) point.x + Player.cameraX(),
			  (int) point.y + Player.cameraY());
			lastX = point.x;
			lastY = point.y;
		}
	}


	public static final class ToggleAction {
		
		boolean disabled = false;
		boolean turningOn;

		ToggleAction(boolean startOn) {
			this.turningOn = !startOn;
		}

		public void disable() { disabled = true; }
		public void enable() { disabled = false; }

		public void toggle() {
			if (disabled || !player.isAlive()) {
				return;
			}
			if (turningOn) {
				player.showUI();
				player.setTimeSinceUI(-1);
				player.getInventory().revive();
			} else {
				player.getInventory().kill();
				player.setTimeSinceUI(0);
			}
			turningOn = !turningOn;
		}
	}
}