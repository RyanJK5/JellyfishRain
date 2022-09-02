package bullethell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bullethell.Player.Equipment;
import bullethell.enemies.Enemy;
import bullethell.enemies.EnemyID;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.movement.AngledPath;
import bullethell.movement.ChargePath;
import bullethell.movement.CirclePath;
import bullethell.movement.LinePath;
import bullethell.movement.Path;
import bullethell.movement.SeekingPath;
import bullethell.movement.StraightPath;
import bullethell.scenes.World;
import bullethell.ui.Container;

@SuppressWarnings("unused")
public final class SaveSystem {
    
    private SaveSystem() { }

    private static String getData(Enemy obj) {
        return getData((Entity) obj) + "," + obj.getHP();
    }

    private static String getData(GameObject obj) {
        return obj.getX() + "," + obj.getY() + "," + obj.rotationDeg + "," + obj.getLayer();
    }

    public static void writePlayerData(boolean onDeath) throws IOException {
        File file = new File("data\\PlayerData.dat");
        Player obj = Player.get();

        int txtX, txtY;
        if (onDeath) {
            try (Scanner scanner = new Scanner(file)) {
                String data = scanner.nextLine();
                Object[] nums = readNums(data, ',');
                txtX = (int) nums[0];
                txtY = (int) nums[1];
            }
        } else {
            txtX = obj.x;
            txtY = obj.y;
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(
              txtX + "," + txtY + "," 
            + obj.rotationDeg + ","
            + obj.getLayer() + ","
            + obj.equipmentInvIndex + "," 
            + (!onDeath ? obj.healNum : obj.maxHealNum) + "," 
            + (!onDeath ? obj.adren : 0) + "," 
            + obj.maxAdr + "," 
            + obj.hitToAdrDelay + "," 
            + (!onDeath ? obj.mana : 0) + "," 
            + obj.maxMana + "," 
            + obj.maxRegenDelay + "," 
            + obj.minRegenDelay + "," 
            + obj.regenDelay + "," 
            + obj.hitToRegenDelay + "," 
            + obj.regenDecreaseRate + "," 
            + (!onDeath ? obj.timeSinceTP : Player.DEFAULT_TP_COOLDOWN) + "," 
            + obj.invincTime + ","
            + (!onDeath ? obj.hp : obj.maxHP) + "," 
            + obj.maxHP + ",\n");
            
            String cs;
            if (obj.getCursorSlot() != null) {
                cs = ItemID.getID(obj.getCursorSlot()) + (obj.getCursorSlot().canStack ? "(" + obj.getCursorSlot().count +")"
                : ""); 
            } else {
                cs = "n";
            }
            writer.write(cs);
            String inv = "\n";
            for (Container<Item> cont : obj.getInventory()) {
                Item item = cont.getItem();
                if (item == null) {
                    break;
                }
                inv += ItemID.getID(item);
                if (item.canStack) {
                    inv += "(" + item.count + ")";
                }
                inv += ",";
            }
            writer.write(inv);

            for (int i = 0; i < obj.getLoadouts().size(); i++) {
                Equipment eqp = obj.getLoadouts().get(i);
                
                String ld = "\n";

                Container<Item>[] accArr = eqp.getAccSlots();
                for (int j = 0; j < accArr.length; j++) {
                    Item item = accArr[j].getItem();
                    ld += (item != null ? ItemID.getID(item) : "n") + ",";
                }
                Container<Item>[] armArr = eqp.getAbilitySlots();
                for (int j = 0; j < armArr.length; j++) {
                    Item item = armArr[j].getItem();
                    ld += (item != null ? ItemID.getID(item) : "n") + ",";
                }
                ld += (eqp.getWepSlot().getItem() != null ? ItemID.getID(eqp.getWepSlot().getItem()) : "n") + ",";
                ld += (eqp.getCoreSlot().getItem() != null ? ItemID.getID(eqp.getCoreSlot().getItem()) : "n") + ",";
                writer.write(ld);
            }
        }
    }

    public static void readPlayerData() throws IOException {
        File file = new File("data\\PlayerData.dat");
        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter(",");

            Player obj = Player.get();
            obj.getInventory().clear();
            obj.getEquipmentInv().clear();
            obj.getResearchedRecipes().clear();

            int pX = 0;
            int pY = 0;
            for (int i = 0; i < 20; i++) {
                String str = scanner.next();
                switch (i) {
                    case 0:
                        pX = Integer.parseInt(str);            
                        break;
                    case 1:
                        pY = Integer.parseInt(str);
                        break;
                    case 2:
                        obj.rotationDeg = Float.parseFloat(str);
                        break;
                    case 3:
                        obj.setLayer(Integer.parseInt(str));
                        break;
                    case 4:
                        obj.changeLoadout(Integer.parseInt(str));
                        break;
                    case 5:
                        obj.healNum = Integer.parseInt(str);
                        break;
                    case 6:
                        obj.adren = Integer.parseInt(str);
                        break;
                    case 7:
                        obj.maxAdr = Integer.parseInt(str);
                        break;
                    case 8:
                        obj.hitToAdrDelay = Integer.parseInt(str);
                        break;
                    case 9:
                        obj.mana = Integer.parseInt(str);
                        break;
                    case 10:
                        obj.maxMana = Integer.parseInt(str);
                        break;
                    case 11:
                        obj.maxRegenDelay = Integer.parseInt(str);
                        break;
                    case 12:
                        obj.minRegenDelay = Integer.parseInt(str);
                        break;
                    case 13:
                        obj.regenDelay = Integer.parseInt(str);
                        break;
                    case 14:
                        obj.hitToRegenDelay = Integer.parseInt(str);
                        break;
                    case 15:
                        obj.regenDecreaseRate = Integer.parseInt(str);
                        break;
                    case 16:
                        obj.timeSinceTP = Integer.parseInt(str);
                        break;
                    case 17:
                        obj.invincTime = Integer.parseInt(str);
                        break;
                    case 18:
                        obj.hp = Integer.parseInt(str);
                        break;
                    case 19:
                        obj.maxHP = Integer.parseInt(str);
                        break;
                }
            }
            obj.setLocation(pX, pY);

            scanner.nextLine();

            obj.select(parseItem(scanner.nextLine()));

            String invLine = scanner.nextLine();
            int startIndex = 0;
            while (true) {
                int lowIndex = startIndex;
                int highIndex = invLine.indexOf(",", lowIndex + 1);
                if (lowIndex == -1 || highIndex == -1) {
                    break;
                }
                obj.getInventory().addItem(parseItem(invLine.substring(lowIndex, highIndex)));
                startIndex = invLine.indexOf(",", startIndex) + 1;
            }

            for (int i = 0; i < obj.getLoadouts().size(); i++) {
                Equipment eqp = obj.getLoadouts().get(i);
                for (int j = 0; j < Equipment.accSlotNum; j++) {
                    String str = scanner.next();
                    eqp.setAccSlot(j, parseItem(str));
                }
                for (int j = 0; j < Equipment.abilitySlotNum; j++) {
                    eqp.setAbilitySlot(j, parseItem(scanner.next()));
                }
                eqp.setWepSlot((Item) parseItem(scanner.next()));
                eqp.setCoreSlot(parseItem(scanner.next()));
                scanner.nextLine();
            }
        }
    }

    public static void writeEntityData() throws IOException {
        File file = new File("data\\EntityData.dat");
        
        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < GameSolid.solids.size(); i++) {
                GameSolid solid = GameSolid.solids.get(i);
                if (solid instanceof Player) {
                    continue;
                }
                try {
                    if (solid instanceof Enemy enemy) {
                        writer.write(EnemyID.getID(enemy) + "(" + getData(enemy) + ")" + (enemy.isAlive() ? 1 : 0) + "\n");
                        continue;
                    }
                } catch (UnindexedGameObjectException uigoe) { System.err.println(solid); }
            }
        }
    }

    public static void readEntityData() throws IOException {
        File file = new File("data\\EntityData.dat");
        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String str = scanner.nextLine();
                Enemy result = parseEnemy(str);
                result.unghost();
            }
        }
    }

    public static void writeWorldData() throws IOException {
        File file = new File("data\\WorldData.dat");

        try (FileWriter writer = new FileWriter(file)) {
            for (boolean bool : World.get().getEvents()) {
                writer.write((bool ? "1" : "0") + "\n");
            }
        }
    }

    public static void loadWorld(boolean newWorld) throws IOException {
        File file = new File("data\\WorldData.dat");
        
        if (newWorld || !file.exists()) {
            World.get().start(0, 0);
            return;
        }
        
        try (Scanner scanner = new Scanner(file)) {
            List<Boolean> result = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String str = scanner.nextLine();
                result.add(Integer.parseInt(str) == 1);
            }
            World.get().setEvents(result.toArray(new Boolean[0]));
        }

        World.get().start(0, 0);
    }

    public static void writeSettingsData() throws IOException {
        File file = new File("data\\SettingsData.dat");
        
        Integer[] keySet = Globals.KEY_MAP.keySet().toArray(new Integer[0]);
        Boolean[] values = Globals.KEY_MAP.values().toArray(new Boolean[0]);
        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < keySet.length; i++) {
                writer.write(keySet[i] + "," + (values[i] ? 1 : 0) + "\n");
            }
            writer.write((Globals.lockScreen ? 1 : 0) + "\n");
            writer.write((Globals.alwaysShowUI ? 1 : 0) + "\n");
            writer.write(Globals.getVolume() + "\n");
        }
    }

    public static void readSettingsData() throws IOException {
        File file = new File("data\\SettingsData.dat");
        if (!file.exists()) {
            return;
        }

        List<Integer> keySet = new ArrayList<>();
        List<Boolean> values = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            for (int i = 0; i < Globals.KEY_MAP.size(); i++) {
                String str = scanner.nextLine();
                    keySet.add(Integer.parseInt(str.substring(0, str.indexOf(','))));
                    values.add(Integer.parseInt(str.substring(str.indexOf(',') + 1)) == 1);
            }
            Globals.lockScreen = Integer.parseInt(scanner.nextLine()) == 1;
            Globals.alwaysShowUI = Integer.parseInt(scanner.nextLine()) == 1;
            Globals.setVolume(Float.parseFloat(scanner.nextLine()));
        }
        Globals.makeKeyMap(keySet.toArray(new Integer[0]), values.toArray(new Boolean[0]));
    }

    public static void writeData(boolean onDeath) {
        try {
            writePlayerData(onDeath);
            writeEntityData();
            writeWorldData();
            writeSettingsData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void readData(boolean newWorld) {
        try {
            readPlayerData();
            readEntityData();
            loadWorld(newWorld);
            readSettingsData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static Item parseItem(String str) {
        if (str.equals("n")) {
            return null;
        }

        int parentheses = str.indexOf("(");
        if (parentheses >= 0) {
            int count = Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(")")));
            Item item = ItemID.getItem(Integer.parseInt(str.substring(0, parentheses)));
            item.count = count;
            return item;
        } else {
            return ItemID.getItem(Integer.parseInt(str));
        }
    }

    private static Enemy parseEnemy(String str) {
        Enemy result;

        result = EnemyID.getEnemy(Integer.parseInt(str.substring(0, str.indexOf('('))));
        result.setHP(Integer.parseInt(str.substring(str.lastIndexOf(',') + 1, str.lastIndexOf(')'))));

        String baseData = str.substring(str.indexOf('(') + 1, str.lastIndexOf(')'));
        int startIndex = 0;
        int x = 0;
        outer: for(int i = 0; ; i++) {
            int lowIndex = startIndex;
            int highIndex = baseData.indexOf(",", lowIndex + 1);
            if (lowIndex == -1 || highIndex == -1) {
                break;
            }

            switch (i) {
                case 0:
                    x = Integer.parseInt(baseData.substring(lowIndex, highIndex));
                    break;
                case 1:
                    result.setLocation(x, Integer.parseInt(baseData.substring(lowIndex, highIndex)));
                    break;
                case 2:
                    result.rotate(Float.parseFloat(baseData.substring(lowIndex, highIndex)));
                    break;
                case 3:
                    result.setLayer(Integer.parseInt(baseData.substring(lowIndex, highIndex)));
                    break;
                case 4:
                    result.setSpeed(Float.parseFloat(baseData.substring(lowIndex, highIndex)));
                    break outer;
            }

            startIndex = baseData.indexOf(",", startIndex) + 1;
        }
        if (str.charAt(str.length() - 1) == '1') {
            result.revive();
        } else {
            result.kill();
        }
        return result;
    }

    private static Object[] readNums(String baseData, char indicator) {
        
        baseData += "|";

        List<Object> result = new ArrayList<>();
        int startIndex = 0;
        for (int i = 0; ; i++) {
            int lowIndex = startIndex;
            int highIndex = baseData.indexOf(indicator, lowIndex + 1);
            if (lowIndex == -1 || highIndex == -1) {
                break;
            }
            String substr = baseData.substring(lowIndex, highIndex);
            if (substr.contains(".")) {
                result.add(Float.parseFloat(substr));
            } else {
                result.add(Integer.parseInt(substr));
            }

            startIndex = highIndex + 1;
        }
        return result.toArray();
    }

    private static GameObject parseGameObject(String str) throws IOException {
        GameObject result = new GameObject();
        String baseData = str.substring(str.indexOf('(') + 1, str.indexOf(')'));
        Object[] nums = readNums(baseData, ',');
        result.setLocation((int) nums[0], (int) nums[1]);
        result.rotate((float) nums[2]);
        result.setLayer((int) nums[3]);
        return result;
    }

    private static class UnindexedGameObjectException extends RuntimeException {

        public UnindexedGameObjectException(GameObject obj) {
            super(obj instanceof Item item ? item.name : obj + " could not be found in the ID list.");
        }
    }
}
