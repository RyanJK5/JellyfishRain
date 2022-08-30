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
import java.util.Random;

import javax.imageio.ImageIO;

import bullethell.Enemy;
import bullethell.Entity;
import bullethell.GameSolid;
import bullethell.GameState;
import bullethell.Globals;
import bullethell.Player;
import bullethell.SaveSystem;
import bullethell.SolidContainer;
import bullethell.Trigger;
import bullethell.items.Item;
import bullethell.items.ItemDrop;
import bullethell.movement.CirclePath;
import bullethell.movement.SeekingPath;

public final class World implements Scene, ActionListener {
    
    private static final World WORLD = new World();

    @WorldData
    private boolean castyTaken = false;
    @WorldData
    private boolean chestOpened = false;

    private boolean inBossFight = false;

    private int timeSinceSave = 0;

    private ItemDrop casty;
    private GameSolid chest;
    private SolidContainer<Item> sigilPedestal;

    private World() {
        Globals.GLOBAL_TIMER.addActionListener(this);
    }

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

            if (!castyTaken) {
                casty = new ItemDrop(SaveSystem.getItem(2), 1000, 200);
            }
            
            if (!chestOpened) {
                chest = new GameSolid(Globals.getImage("chest"));
                chest.addTrigger(new Trigger(null, new Trigger.Type[] {
                    Trigger.CURSOR_OVER, Trigger.TARGET_IN_RANGE, Trigger.ON_CLICK, Trigger.RIGHT_CLICK
                }) {
                    @Override
                    protected void activate() {
                        Globals.setGameState(GameState.ENCOUNTER);
                        Player.get().getInventory().addItem(SaveSystem.getStackable(5, 14));
                        
                        permakill();
                        chest.permakill();
                        for (int i = 0; i < 6; i++) {
                            Entity ent = SaveSystem.getEntity(0);
                            Point start = new Point(900, 200);
                            ent.setLocation(900 - ent.getWidth() / 2, 200 - ent.getHeight() / 2);
                            ent.setPath(new CirclePath(new Point(1000, 200), start, 2, true));
                            for (int j = i; j >= 0; j--) {
                                ent.move();
                            }
                            ent.setPath(new SeekingPath(ent, Player.get()));
                            ent.setSpeed(new Random().nextInt(8, 12));
                            ((Enemy) ent).addToGroup(1);
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
                      !Player.get().getCursorSlot().equals(SaveSystem.getItem(6))) {
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
                        Player.get().getInventory().addItem(SaveSystem.getItem(6));
                        inBossFight = true;
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inBossFight) {
            return;
        }

        if (timeSinceSave >= 10 * 1000 / Globals.TIMER_DELAY) {
            SaveSystem.writeData(false);
            timeSinceSave = 0;
        }
        
        if (!castyTaken) {
            castyTaken = casty != null && !casty.isAlive();
        }
        if (!chestOpened) {
            chestOpened = chest != null && !chest.isAlive();
        }
        timeSinceSave++;
    }

    public Boolean[] getEvents() {
        List<Boolean> result = new ArrayList<>();
        for (Field field : World.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(WorldData.class)) {
                try {
                    result.add((boolean) field.get(get()));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toArray(new Boolean[0]);
    }

    public void setEvents(Boolean[] events) {
        Field[] fields = World.class.getDeclaredFields();
        List<Field> annotatedFields = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(WorldData.class)) {
                annotatedFields.add(field);
            }
        }
        for (int i = 0; i < events.length; i++) {
            Field field = annotatedFields.get(i);
            if (field.isAnnotationPresent(WorldData.class)) {
                try {
                    field.set(get(), events[i]);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface WorldData {}

    @Override
    public boolean isActive() {
        return true;
    }
}
