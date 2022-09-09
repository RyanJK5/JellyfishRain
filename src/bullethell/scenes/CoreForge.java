package bullethell.scenes;

import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.imageio.ImageIO;

import bullethell.GameObject;
import bullethell.GameState;
import bullethell.Player;
import bullethell.SolidContainer;
import bullethell.Trigger;
import bullethell.items.EquipType;
import bullethell.items.Item;
import bullethell.items.PlayerModifiers;
import bullethell.items.WeaponModifiers;
import bullethell.items.charms.CustomCore;
import bullethell.ui.Container;

final class CoreForge implements Scene {

    private static final CoreForge CORE_FORGE = new CoreForge();

    public static CoreForge get() {
        return CORE_FORGE;
    }

    private CoreForge() { }

    @Override
    public GameState getState() {
        return GameState.DEFAULT;
    }

    @Override
    public void start(int x, int y) {
        try {
            GameObject coreForge = new GameObject(ImageIO.read(new File("sprites\\CoreForge.png")), 1);
            coreForge.setLocation(x, y);

            int[] xpoints = {28, 63, 119, 192, 276, 362, 452, 537, 622, 694, 750, 785, 393};
            int[] ypoints = {3,  87, 160, 216, 251, 263, 263, 251, 216, 160, 87,  3,   112};

            @SuppressWarnings("unchecked")
            Container<Item>[] pedestals = new Container[12];
            Container<Item> result = null;
            for (int i = 0; i < 13; i++) {
                xpoints[i] += coreForge.getX();
                ypoints[i] += coreForge.getY();
                
                Container<Item> cont = new SolidContainer<>(ImageIO.read(new File(i < 12 ? "sprites\\CoreForgeSmall.png" :
                 "sprites\\CoreForgeBig.png")), Item.class) {
                    @Override
                    public boolean moveItem(boolean taking) {
                        if (Player.get().getCursorSlot() != null && Player.get().getCursorSlot().equipType != EquipType.CORE) {
                            return false;
                        }
                        return super.moveItem(taking);
                    }
                 };
                cont.setLayer(1);
                cont.setLocation(xpoints[i], ypoints[i]);

                if (i < 12) {
                    pedestals[i] = cont;
                } else {
                    result = cont;
                }
            }
            Container<Item> finalResult = result;

            Trigger trigger = new Trigger(ImageIO.read(new File("sprites/button.png")), 
            new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER, Trigger.TARGET_IN_RANGE}) {
                
                HashMap<Item, List<Item>> coreToBasicCores = new LinkedHashMap<>();
                @Override
                public void activate() {
                    boolean resultNull = finalResult.getItem() == null;
                    List<Item> cores = new ArrayList<>();
                    for (Container<Item> cont : pedestals) {
                        Item item = cont.getItem();
                        if (item == null) continue;
                        if (!resultNull) return;
                        cores.add(item);
                    }
                    
                    if (cores.size() == 1) return;
                    if (cores.size() == 0 && resultNull) return;

                    for (Container<Item> cont : pedestals) {
                        cont.setItem(null);
                    }

                    if (!resultNull) {
                        List<Item> oldCores = coreToBasicCores.get(finalResult.getItem());
                        if (oldCores == null) return;
                        for (int i = 0; i < oldCores.size(); i++) {
                            pedestals[i].setItem(oldCores.get(i));
                        }
                        finalResult.setItem(null);
                        return;
                    }

                    if (coreToBasicCores.containsValue(cores)) {
                        for (int i = 0; i < coreToBasicCores.keySet().size(); i++) {
                            if (coreToBasicCores.values().toArray()[i].equals(cores)) {
                                finalResult.setItem(coreToBasicCores.keySet().toArray(new Item[0])[i]);
                                return;
                            }
                        }
                    }

                    WeaponModifiers resultWepMods = new WeaponModifiers();
                    PlayerModifiers resultPlayMods = new PlayerModifiers();
                    for (Item core : cores) {
                        resultWepMods.apply(core.weaponModifiers);
                        resultPlayMods.apply(core.playerModifiers);
                    }
                    resultWepMods.apply(f -> f / cores.size(), i -> i / cores.size());
                    resultPlayMods.apply(f -> f / cores.size(), i -> i / cores.size());
                    Item resultCore = new CustomCore(resultWepMods, resultPlayMods);

                    Thread thread = new Thread(() -> {
                        resultCore.name = Scene.promptString("Enter the new core's name: ");
                    });
                    thread.start();
                    finalResult.setItem(resultCore);
                    coreToBasicCores.put(resultCore, cores);
                }
            
                @Override
                public void update(Graphics g) {
                    super.update(g);
                    if (!Player.cursorOver(toRect())) return;

                    int activePedestals = 0;
                    for (int i = 0; i < pedestals.length; i++) {
                        if (pedestals[i].getItem() != null) {
                            activePedestals++;
                        }
                    }

                    if (finalResult.getItem() != null) {
                        if (activePedestals > 0) {
                            return;
                        }
                        if (coreToBasicCores.containsKey(finalResult.getItem())) {
                            g.drawString("Smelt the core into its basic cores.", Player.cursorX(), Player.cursorY());
                        }
                    } else {
                        if (activePedestals > 1) {
                            g.drawString("Combine these cores to make a new core with weaker versions of all their effects.",
                            Player.cursorX() + 10, Player.cursorY() + 20);
                        }
                    }
                }
            };
		    trigger.setLocation(390 + coreForge.getX() + trigger.getWidth() / 2, 12 + coreForge.getY() + trigger.getHeight() / 2);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void end() { }

    @Override
    public boolean isActive() {
        return true;
    }
}
