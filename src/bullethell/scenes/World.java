package bullethell.scenes;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.GameObject;
import bullethell.GameSolid;
import bullethell.GameState;
import bullethell.Globals;
import bullethell.Parallax;
import bullethell.Player;
import bullethell.SaveSystem;
import bullethell.SolidContainer;
import bullethell.Trigger;
import bullethell.combat.Entity;
import bullethell.enemies.Enemy;
import bullethell.enemies.EnemyGroup;
import bullethell.enemies.EnemyID;
import bullethell.items.Item;
import bullethell.items.ItemDrop;
import bullethell.items.ItemID;
import bullethell.movement.CirclePath;
import bullethell.movement.SeekingPath;

public final class World implements Scene, ActionListener {
    
    private static final World WORLD = new World();

    @WorldData
    private boolean castyBool = false;
    @WorldData
    private boolean chestBool = false;

    private int timeSinceSave = 0;

    private ItemDrop casty;
    private GameSolid chest;
    private SolidContainer<Item> sigilPedestal;

    public static World get() {
        return WORLD;
    }

    @Override
    public GameState getState() {
        return GameState.DEFAULT;
    }

    @Override
    public void start(int x, int y) {
        try {
            new Parallax("JellyBG");
            Globals.GLOBAL_TIMER.addActionListener(this);

            if (!castyBool) {
                casty = new ItemDrop(ItemID.EXAMPLE_STAFF, 1, 1000, 200);
            }
            
            if (!chestBool) {
                chest = new GameSolid(Globals.getImage("chest"));
                chest.addTrigger(new Trigger(null, new Trigger.Type[] {
                    Trigger.CURSOR_OVER, Trigger.TARGET_IN_RANGE, Trigger.ON_CLICK, Trigger.RIGHT_CLICK
                }) {
                    @Override
                    protected void activate() {
                        Globals.setGameState(GameState.ENCOUNTER);
                        Player.get().getInventory().addItem(ItemID.METAL.getItem().clone(14));
                        
                        permakill();
                        chest.permakill();
                        EnemyGroup group = new EnemyGroup();
                        for (int i = 0; i < 6; i++) {
                            Entity ent = EnemyID.PIXIE.getEnemy();
                            Point start = new Point(900, 200);
                            ent.setLocation(900 - ent.getWidth() / 2, 200 - ent.getHeight() / 2);
                            ent.setPath(new CirclePath(new Point(1000, 200), start, 2, true));
                            for (int j = i; j >= 0; j--) {
                                ent.move();
                            }
                            ent.setPath(new SeekingPath(ent, Player.get()));
                            group.add((Enemy) ent);
                        }
                    }
                });
                chest.setLocation(100, 800);
            }
            
            sigilPedestal = new SolidContainer<>(ImageIO.read(new File("sprites\\SigilContainer.png")), 
              Item.class) {
                @Override
                public boolean moveItem(boolean taking) {
                    if (Player.get().getCursorSlot() != null &&
                      !Player.get().getCursorSlot().id.equals(ItemID.STRANGE_SIGIL)) {
                        return false;
                    }
                    return super.moveItem(taking);
                }
              }; 
            sigilPedestal.setHitbox(new java.awt.geom.Ellipse2D.Float(0, 0, sigilPedestal.getWidth(), sigilPedestal.getHeight()));
            sigilPedestal.setLocation(Globals.WIDTH / 2 - sigilPedestal.getWidth() / 2, 
            Globals.HEIGHT / 2 - sigilPedestal.getHeight() / 2);
            
            Forge.get().start(300, 300);
            
            Globals.GLOBAL_TIMER.addActionListener(new ActionListener() {

                int timesPerformed = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (sigilPedestal.getItem() != null) {
                        if (timesPerformed < 20) {
                            timesPerformed++;
                            return;
                        }
                        Player.get().getInventory().addItem(ItemID.STRANGE_SIGIL.getItem());
                        Player.get().setLocation(sigilPedestal.getX() - 200, sigilPedestal.getY());
                        
                        SaveSystem.writeData(false);
                        
                        sigilPedestal.setItem(null);
                        sigilPedestal.permakill();
                        Globals.main.setScene(ErnestoBoss.get(), 0, 0);
                        if (chest != null) {
                            chest.permakill();
                        }
                        Forge.get().end();
                        if (Player.get().getInventory().isAlive()) {
                            Globals.eAction.toggle();
                        }
                        Player.get().setLocation(Globals.WIDTH / 2 - Player.get().getWidth() / 2, Globals.HEIGHT / 2 - Player.get().getHeight() / 2);
                    }
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void end() {
        Forge.get().end();
        if (casty != null) {
            casty.kill();
        }
        if (chest != null) {
            chest.kill();
        }
        if (sigilPedestal != null) {
            sigilPedestal.kill();
        }
        Entity.removeAll(e -> true);
        Globals.GLOBAL_TIMER.removeActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (GameState.BOSS == Globals.getGameState()) {
            return;
        }

        if (timeSinceSave >= 10 * 1000 / Globals.TIMER_DELAY) {
            SaveSystem.writeData(false);
            timeSinceSave = 0;
        }

        if (EnemyGroup.anyGroupAlive() && Globals.getGameState() == GameState.DEFAULT) {
            Globals.setGameState(GameState.ENCOUNTER);
        } else if (Globals.getGameState() == GameState.ENCOUNTER) {
            Globals.setGameState(GameState.DEFAULT);
        }
        
        for (Field field : World.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(WorldData.class)) {
                Field corresField;
                try {
                    corresField = World.class.getDeclaredField(field.getName().substring(0, field.getName().indexOf('B')));
                    GameObject obj = (GameObject) corresField.get(get());
                    if (obj != null && !obj.isAlive()) {
                        field.setBoolean(get(), true);
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        }

        timeSinceSave++;
    }

    public boolean[] getEvents() {
        List<Boolean> result = new ArrayList<>();
        for (Field field : World.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(WorldData.class)) {
                try {
                    result.add(field.getBoolean(get()));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        boolean[] resultArr = new boolean[result.size()];
        for (int i = 0; i < result.size(); i++) {
            resultArr[i] = result.get(i);
        }
        return resultArr;
    }

    public void setEvents(boolean[] events) {
        Field[] fields = World.class.getDeclaredFields();
        int index = 0;
        for (Field field : fields) {
            if (field.isAnnotationPresent(WorldData.class)) {
                try {
                    field.setBoolean(get(), events[index]);
                    index++;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setEvents(boolean allEvents) {
        for (Field field : World.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(WorldData.class)) {
                try {
                    field.setBoolean(get(), false);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface WorldData { }

    @Override
    public boolean isActive() {
        return true;
    }
}
