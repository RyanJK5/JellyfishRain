package bullethell.scenes;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bullethell.GameObject;
import bullethell.GameSolid;
import bullethell.GameState;
import bullethell.Globals;
import bullethell.Player;
import bullethell.Trigger;
import bullethell.Player.Equipment;
import bullethell.combat.Enchantment;
import bullethell.combat.EnchantmentType;
import bullethell.combat.StatusEffectType;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.ui.Button;
import bullethell.ui.Container;
import bullethell.ui.Inventory;

public final class EnchantmentForge implements Scene {

    private static final EnchantmentForge ENCHANTMENT_FORGE = new EnchantmentForge();
    private static final Font BOX_FONT = new Font("", Globals.DEFAULT_FONT.getStyle(), 60);
    
    private static final int MAX_ENCHANT_NUM = 3;

    private int enchantIndex;
    private int effectIndex;
    private int enchantPercent = 20;

    private GameSolid forge;
    private ControlTrigger startTrigger;
    private Trigger endTrigger;

    private GameObject enchantBox;
    private GameObject statusBox;

    private Button enchantUp, enchantDown;
    private Button effectUp, effectDown;
    
    private Container<Item> enchantSlot;
    private Inventory<Item> availableItems;
    private Item[] equipArr;
    private Button enchantButton;

    public static EnchantmentForge get() {
        return ENCHANTMENT_FORGE;
    }

    private EnchantmentForge() { }

    @Override
    public GameState getState() {
        return GameState.DEFAULT;
    }

    @Override
    public void start(int x, int y) {
        forge = new GameSolid(Globals.getImage("EnchantForge"));
        forge.setLocation(x, y);

        startTrigger = new ControlTrigger();

        endTrigger = new Trigger(null, new Trigger.Type[] {Trigger.ON_KEY_PRESS}) {
            @Override
            protected void activate() {
                startTrigger.end();
            }
        };
        endTrigger.addKeyCode(java.awt.event.KeyEvent.VK_ESCAPE);

        forge.addTrigger(startTrigger);

        setupBoxes();
        setupArrows();
        setupSlots();

        GameObject[] fieldArr = {
            enchantBox, statusBox, enchantUp, enchantDown, effectUp, effectDown, endTrigger, availableItems, enchantButton
        };
        enchantSlot.setLayer(100);
        for (GameObject obj : fieldArr) {
            obj.setLayer(99);
        }
        killAll();
    }

    private void setupBoxes() {
        statusBox = new GameObject(Globals.getImage("BlankButton"), 100) {
            @Override
            public void update(Graphics g) {
                super.update(g);
                g.setFont(BOX_FONT);
                g.setColor(StatusEffectType.values()[effectIndex].getColor());
                g.drawString(StatusEffectType.values()[effectIndex].presentTense(), x + 20, y + h / 2 + 22);
            }
        };

        enchantBox = new GameObject(Globals.getImage("BlankButton"), 100) {
            @Override
            public void update(Graphics g) {
                super.update(g);
                g.setFont(BOX_FONT);
                String str = "";
                switch (EnchantmentType.values()[enchantIndex]) {
                    case EFFECT_DAMAGE_BOOST:
                        str = "+" + enchantPercent + "% damage to";
                        break;
                    case INFLICT_EFFECT:
                        str = "Inflicts";
                        break;
                }
                g.drawString(str, x + 20, y + h / 2 + 22);
            }
        };
    }

    private void setupArrows() {
        BufferedImage sheet = Globals.getImage("VerticalArrowButtons");
        BufferedImage up = sheet.getSubimage(0, 0, sheet.getWidth(), sheet.getHeight() / 2);
        BufferedImage down = sheet.getSubimage(0, sheet.getHeight() / 2, sheet.getWidth(), sheet.getHeight() / 2);

        enchantUp = new Button(up) {
            @Override
            protected void activate() {
                if (!getAltCondition().get()) {
                    if (enchantPercent > 20 && enchantIndex == Globals.indexOf(EnchantmentType.values(), EnchantmentType.EFFECT_DAMAGE_BOOST)) {
                        enchantPercent -= 20;
                        return;
                    }
                    enchantIndex--;
                }
            }
        };
        enchantUp.setGlowOnHover(false);
        enchantUp.setAltCondition(() -> enchantIndex <= 0 &&
          !(enchantPercent > 20 && enchantIndex == Globals.indexOf(EnchantmentType.values(), EnchantmentType.EFFECT_DAMAGE_BOOST)));

        enchantDown = new Button(down) {
            @Override
            protected void activate() {
                if (!getAltCondition().get()) {
                    if (enchantPercent < 100 && enchantIndex == Globals.indexOf(EnchantmentType.values(), EnchantmentType.EFFECT_DAMAGE_BOOST)) {
                        enchantPercent += 20;
                        return;
                    }
                    enchantIndex++;
                }
            }
        };
        enchantDown.setGlowOnHover(false);
        enchantDown.setAltCondition(() -> enchantIndex >= EnchantmentType.values().length - 1 ||
          (enchantPercent >= 100 && enchantIndex != Globals.indexOf(EnchantmentType.values(), EnchantmentType.EFFECT_DAMAGE_BOOST)));

        effectUp = new Button(up) {
            @Override
            protected void activate() {
                if (!getAltCondition().get()) {
                    effectIndex--;
                }
            }
        };
        effectUp.setGlowOnHover(false);
        effectUp.setAltCondition(() -> effectIndex <= 0);

        effectDown = new Button(down) {
            @Override
            protected void activate() {
                if (!getAltCondition().get()) {
                    effectIndex++;
                }
            }
        };
        effectDown.setGlowOnHover(false);
        effectDown.setAltCondition(() -> effectIndex >= StatusEffectType.values().length - 1);
    }

    private void setupSlots() {
        enchantSlot = new Container<>(Globals.getImage("InventorySlot"), Item.class) {
            @Override
            public boolean moveItem(boolean taking, Inventory<? super Item> inventory) {
                if (Player.get().getCursorSlot() != null) {
                    Item cSlot = Player.get().getCursorSlot();
                    if (cSlot.equipType != EquipType.WEAPON || cSlot.enchantments.size() >= MAX_ENCHANT_NUM) {
                        return true;
                    }
                }
                return super.moveItem(taking, inventory);
            }
        };

        availableItems = new Inventory<>(new java.awt.Dimension(10, 8), Globals.getImage("InventorySlot"), Item.class);
        availableItems.setMovable(false);

        BufferedImage sheet = Globals.getImage("ActionButtons");
        enchantButton = new Button(sheet.getSubimage(0, 0, sheet.getWidth(), sheet.getHeight() / 2)) {
            @Override
            protected void activate() {
                if (getAltCondition().get()) {
                    return;
                }
                enchantSlot.getItem().addEnchantment(
                    new Enchantment(EnchantmentType.values()[enchantIndex], StatusEffectType.values()[effectIndex], enchantPercent / 100f)
                );
            }
        };
        enchantButton.setAltCondition(() -> conflictingEnchantments(enchantSlot.getItem()) || enchantSlot.getItem() == null || 
          enchantSlot.getItem().enchantments.size() >= 3);
    }

    private boolean conflictingEnchantments(Item item) {
        if (item == null) {
            return false;
        }
        for (Enchantment enchant : item.enchantments) {
            if (enchant.eType == EnchantmentType.values()[enchantIndex] || enchant.sType == StatusEffectType.values()[effectIndex]) {
                return true;
            }
        }
        return false;
    }

    private void killAll() {
        GameObject[] fieldArr = {
            enchantBox, statusBox, enchantUp, enchantDown, effectUp, effectDown, endTrigger, enchantSlot, availableItems, enchantButton
        };
        for (GameObject obj : fieldArr) {
            obj.kill();
        }
    }

    private void reviveAll() {
        GameObject[] fieldArr = {
            enchantBox, statusBox, enchantUp, enchantDown, effectUp, effectDown, endTrigger, enchantSlot, availableItems, enchantButton
        };
        for (GameObject obj : fieldArr) {
            obj.revive();
        }
    }

    private void setLocations() {
        statusBox.setLocation(Player.cameraX() + Globals.SCREEN_WIDTH / 2, Player.cameraY() + statusBox.getHeight() * 2);
        enchantBox.setLocation(statusBox.getX() - enchantBox.getWidth(), statusBox.getY());
        
        enchantUp.setLocation(enchantBox.getCenterX() - enchantUp.getWidth() / 2, enchantBox.getY() - enchantUp.getHeight() - 10);
        enchantDown.setLocation(enchantBox.getCenterX() - enchantDown.getWidth() / 2, enchantBox.getY() + enchantBox.getHeight() + 10);
        effectUp.setLocation(enchantUp.getX() + enchantBox.getWidth(), enchantUp.getY());
        effectDown.setLocation(enchantDown.getX() + enchantBox.getWidth(), enchantDown.getY());
        
        enchantSlot.setLocation(statusBox.getX() - (enchantButton.getWidth() + enchantSlot.getWidth() + 20) / 2, 
        statusBox.getY() + statusBox.getHeight() + 10);
        enchantButton.setLocation(enchantSlot.getX() + enchantSlot.getWidth() + 20, enchantSlot.getY());

        availableItems.setLocation(enchantBox.getX() + enchantBox.getWidth() - availableItems.getWidth() / 2, 
          enchantBox.getY() + enchantBox.getHeight() + 100);
    }

    @Override
    public void end() {
        forge.kill();
        killAll();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    private class ControlTrigger extends Trigger {
        
        private boolean starting = true;
        
        ControlTrigger() {
            super(null, new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER, Trigger.TARGET_IN_RANGE, Trigger.RIGHT_CLICK});
        }
        
        @Override
        protected void activate() {
            if (starting) {
                start();
            } else {
                end();
            }
        }

        void start() {
            setLocations();
            Globals.setGameState(GameState.MENU);
            reviveAll();
            starting = false;

            Player.get().killUI();

            List<Item> itemList = new ArrayList<>();
            equipArr = new Item[3];
            for (Container<Item> cont : Player.get().getInventory()) {
                if (cont.getItem() != null && cont.getItem().equipType == EquipType.WEAPON) {
                    itemList.add(cont.getItem());
                }
            }
            for (int i = 0; i < Player.get().getLoadouts().size(); i++) {
                Equipment loadout = Player.get().getLoadouts().get(i);
                equipArr[i] = loadout.getWepSlot().getItem();
                Player.get().getLoadouts().get(i).setWepSlot(null);
            }
            availableItems.setItems(itemList);
            availableItems.addAll(Arrays.asList(equipArr));
            Player.get().getInventory().removeAll(itemList);
        }

        void end() {
            Globals.setGameState(GameState.DEFAULT);
            for (Container<Item> cont : availableItems) {
                if (!Globals.contains(equipArr, obj -> obj == cont.getItem())) {
                    Player.get().getInventory().addItem(cont.getItem());
                }
            }
            for (int i = 0; i < Player.get().getLoadouts().size(); i++) {
                Player.get().getLoadouts().get(i).setWepSlot(equipArr[i]);
            }
            if (Globals.alwaysShowUI) {
                Player.get().showUI();
            }
            killAll();
            starting = true;
        }
    }
}