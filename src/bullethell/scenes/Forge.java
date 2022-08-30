package bullethell.scenes;

import static bullethell.Globals.DEFAULT_FONT;
import static bullethell.Globals.eAction;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.GameSolid;
import bullethell.GameState;
import bullethell.Globals;
import bullethell.Player;
import bullethell.Trigger;
import bullethell.items.Item;
import bullethell.items.Recipe;
import bullethell.items.StackableItem;
import bullethell.ui.Container;
import bullethell.ui.Inventory;
import bullethell.ui.Text;

final class Forge implements Scene {

    private static final Forge FORGE = new Forge();

    private final Player player = Player.get();
    private GameSolid forge;
    private Inventory<Item> components;
    private Inventory<Item> availableComponents;
    private Container<Item> result;
    private Inventory<Recipe> inv;
    private Trigger craftButton;
    private Trigger exitTrigger;
    private StartTrigger startTrigger;
    private Text componentsText;
    private Text availableComponentsText;

    public static Forge get() {
        return FORGE;
    }

    private Forge() { }

    @Override
    public GameState getState() {
        return GameState.DEFAULT;
    }

    @Override
    public void start(int x, int y) {
        try {
            BufferedImage forgeImg = ImageIO.read(new File("sprites/Forge.png"));
            forge = new GameSolid(forgeImg, 1);
            forge.setHitbox(new Ellipse2D.Float(0, 0, forge.getWidth(), forge.getHeight()));
            forge.setLocation(x, y);

            components = new Inventory<>(new Dimension(5, 3), 
              ImageIO.read(new File("sprites/InventorySlot.png")), Item.class);
            components.setInteractable(false);
            components.setNavigable(false);
            components.setMovable(false);
            components.kill();

            availableComponents = new Inventory<>(new Dimension(5, 3),
            ImageIO.read(new File("sprites/InventorySlot.png")),
            Item.class);
            availableComponents.setInteractable(false);
            availableComponents.setNavigable(false);
            availableComponents.setMovable(false);
            availableComponents.kill();
            
            Font font = new Font("bigger default font", DEFAULT_FONT.getStyle(), DEFAULT_FONT.getSize() * 2);
            availableComponentsText = new Text("Inventory:", font);
            availableComponentsText.kill();
            componentsText = new Text("Components:", font);
            componentsText.kill();

            result = new Container<>(ImageIO.read(new File("sprites/InventorySlot.png")), Item.class) {
                @Override
                public boolean moveItem(boolean taking, Inventory<? super Item> inventory) {
                    Player player = Player.get();
                    Item item = getItem();
                    Class<Item> itemClass = getItemClass();

                    boolean stackable = item != null && item instanceof StackableItem;
                    if (!stackable) {
                        stackable = stackable();
                    }
                    boolean cursorStackable = player.getCursorSlot() instanceof StackableItem || player.getCursorSlot() == null;

                    if (item != null) {
                        if (taking && stackable) {
                            if (player.getCursorSlot() != null && cursorStackable) {
                                ((StackableItem) player.getCursorSlot()).addFrom(1, (StackableItem) item);
                            } else if (player.getCursorSlot() == null) {
                                player.select(((StackableItem) item).take(1));
                            }
                        } else if (player.getCursorSlot() == null) {
                            player.select(item);
                            setItem(null);
                            player.getInventory().removeItem(item);
                        } else if (itemClass.isInstance(player.getCursorSlot())) {
                            if (stackable && cursorStackable && item.equals(player.getCursorSlot())) {
                                ((StackableItem) item).addFrom(0, (StackableItem) player.getCursorSlot());
                            }
                        }
                    }

                    if (item != null && !item.isAlive()) {
                        setItem(null);
                    }
                    if (player.getCursorSlot() != null && !player.getCursorSlot().isAlive()) {
                        player.select(null);
                    }

                    return true;
                }
            };
            result.kill();

            inv = new Inventory<>(new Dimension(10, 8), 
              ImageIO.read(new File("sprites/InventorySlot.png")), Recipe.class) {
                
                @Override
                public void setLocation(int x, int y) {
                        super.setLocation(x, y);
                        
                        components.setLocation(x + fullWidth + 20, y);
                        componentsText.setLocation(components.getX(), components.getY() - 20);
                        availableComponents.setLocation(components.getX(), components.getY() + components.getFullHeight() + components.getHeight());
                        availableComponentsText.setLocation(availableComponents.getX(), availableComponents.getY() - 20);
                        if (craftButton != null) {
                            craftButton.setLocation(availableComponents.getX(), availableComponents.getY() + availableComponents.getFullHeight());
                            result.setLocation(craftButton.getX() + craftButton.getWidth(), craftButton.getY());  
                        }

                }

                @Override
                public void sort() {
                    List<Container<Recipe>> slots = getSlots();

                    for (Container<Recipe> cont : this) {
                        if (cont.getItem() != null && !cont.getItem().isAlive()) {
                            cont.setItem(null);
                        }
                    }

                    slots.sort((Container<Recipe> o1, Container<Recipe> o2) -> {
                        if (o1.getItem() == null && o2.getItem() == null) {
                            return 0;
                        }
                        if (o1.getItem() == null) {
                            return 1;
                        }
                        if (o2.getItem() == null) {
                            return -1;
                        }

                        boolean o1Craft = o1.getItem().canCraftFromInv();
                        boolean o2Craft = o2.getItem().canCraftFromInv();

                        if (o1Craft && !o2Craft) {
                            return -1;
                        }
                        if (!o1Craft && o2Craft) {
                            return 1;
                        }

                        String str1 = o1.getItem().getName();
                        String str2 = o2.getItem().getName();
                        for (int i = 0; i < str1.length(); i++) {
                            char char1 = Character.toUpperCase(str1.charAt(i));
                            char char2 = Character.toUpperCase(str2.charAt(i));
                            if (char1 > char2) {
                                return 1;
                            } else if (char1 < char2) {
                                return -1;
                            }
                        }
                        return 0;
                    });

                    if (slots.size() <= getSize()) {
                        return;
                    }
                    if (slots.subList(getSize(), slots.size())
                        .stream()
                        .allMatch((Container<Recipe> container) -> container.getItem() == null)) {
                        slots = slots.subList(0, getSize());
                    }
                }
            };
            inv.setInteractable(false);
            inv.setMovable(false);
            inv.kill();
            inv.setHitbox(inv.getFullWidth() + inv.getWidth() * 5 + 20, inv.getHeight());

            craftButton = new Trigger(ImageIO.read(new File("sprites/CraftButton.png")),
              new Trigger.Type[] {Trigger.CURSOR_OVER, Trigger.ON_CLICK}) {
               
                @Override
                protected void activate() {
                    if (getTarget() != player) {
                        @SuppressWarnings("unchecked")
                        Container<Recipe> cont = (Container<Recipe>) getTarget();
                        Item resultItem = cont.getItem().craftFromInv();
                        if (resultItem != null) {
                            if (result.getItem() != null) {
                                player.getInventory().addItem(result.getItem());
                            }
                            result.setItem(resultItem);
                            player.getInventory().addItem(resultItem);
                            availableComponents.sort();
                            inv.sort();
                            setContSprites();
                        }
                    }
                }
            };
            craftButton.setLayer(99);
            craftButton.kill();

            startTrigger = new StartTrigger();
            forge.addTrigger(startTrigger);
            
            exitTrigger = new Trigger(null, new Trigger.Type[] {Trigger.ON_KEY_PRESS}) {
                @Override
                protected void activate() {
                    if (inv.isAlive()) {
                        startTrigger.end();
                        kill();
                    }
                    try {
                        Robot robot = new Robot();
                        robot.keyPress(KeyEvent.VK_ESCAPE);
                        robot.keyRelease(KeyEvent.VK_ESCAPE);
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                }
            };
            exitTrigger.addKeyCode(KeyEvent.VK_ESCAPE);
            exitTrigger.kill();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void end() {
        Globals.resetGameState();
        forge.kill();
        exitTrigger.actionPerformed(null);
    }
   
    private void setContSprites() {
        try {
            for (Container<Recipe> cont : inv) {
                if (cont.getItem() != null && !cont.getItem().canCraftFromInv()) {
                    cont.setSprite(ImageIO.read(new File("sprites/RedInventorySlot.png")));
                } else {
                    cont.setSprite(ImageIO.read(new File("sprites/InventorySlot.png")));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private class StartTrigger extends Trigger {

        public StartTrigger() {
            super(null, new Trigger.Type[] 
              {Trigger.RIGHT_CLICK, Trigger.ON_CLICK, Trigger.CURSOR_OVER, Trigger.TARGET_IN_RANGE, Trigger.NO_UI_PRESENT});
        }
        
        @Override
        protected void activate() {
            if (inv.isAlive()) {
                end();
            } else {
                start();
            }
            inv.setLocation(Player.get().getInventory().getLocation());
        }

        void start() {
            Player.get().showUI();
            Player.get().setTimeSinceUI(-1);
            inv.setItems(player.getResearchedRecipes());
            inv.revive();
            exitTrigger.revive();
            
            player.getInventory().kill();
            eAction.disable();
        
            for (Container<Recipe> cont : inv) {
                if (cont.getItem() == null) {
                    continue;
                }
                Trigger trig = new Trigger(null, new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER}) {
                    
                    @Override
                    protected void activate() {
                        if (result.getItem() != null) {
                            result.setItem(null);
                        }
                        components.setItems(cont.getItem().getComponentList());
                        availableComponents.setItems(player.getInventory().hasItems(cont.getItem().getComponents()));
                        components.revive();
                        componentsText.revive();
                        availableComponents.revive();
                        availableComponentsText.revive();
                        craftButton.revive();
                        result.revive();
                        craftButton.setTarget(cont);	
                    }
                };

                trig.setTarget(cont);
                cont.addTrigger(trig);
            }

            setContSprites();
        }

        void end() {
            if (!Globals.getGameState().combat()) {
                Player.get().setTimeSinceUI(0);
            }
            if (result.getItem() != null) {
                player.getInventory().addItem(result.getItem());
                result.setItem(null);
            }

            components.kill();
            componentsText.kill();
            availableComponents.kill();
            availableComponentsText.kill();
            result.kill();
            craftButton.kill();
            inv.kill();
            eAction.enable();
        }
    }

    @Override
    public boolean isActive() {
        return forge.isAlive();
    }
}
