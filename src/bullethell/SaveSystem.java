package bullethell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bullethell.Player.Equipment;
import bullethell.combat.Enchantment;
import bullethell.combat.EnchantmentType;
import bullethell.combat.tags.StatusEffectType;
import bullethell.enemies.Enemy;
import bullethell.enemies.EnemyID;
import bullethell.items.Item;
import bullethell.items.ItemID;
import bullethell.items.abilities.HealAbility;
import bullethell.items.weapons.Weapon;
import bullethell.scenes.World;
import bullethell.ui.Container;

public final class SaveSystem {
    
    private SaveSystem() { }

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
                + (!onDeath ? HealAbility.healNum : HealAbility.maxHealNum) + "," 
                + (!onDeath ? obj.adren : 0) + "," 
                + obj.maxAdr + "," 
                + obj.hitToAdrDelay + "," 
                + (!onDeath ? obj.mana : 0) + "," 
                + obj.maxMana + "," 
                + obj.regenDelay + "," 
                + obj.timeSinceRegen + "," 
                + obj.hitToRegenDelay + "," 
                + obj.invincTime + ","
                + (!onDeath ? obj.hp : obj.maxHP) + "," 
                + obj.maxHP + ",\n"
            );
            
            if (obj.getCursorSlot() != null) {
                obj.getInventory().addItem(obj.getCursorSlot());
                obj.select(null);
            }

            String inv = "\n";
            for (Container<Item> cont : obj.getInventory()) {
                Item item = cont.getItem();
                if (item == null) {
                    break;
                }
                inv += writeItem(item) + ",";
            }
            writer.write(inv);
            
            String constantLoadouts = "";
            Container<Item>[] accArr = Player.Equipment.getAccSlots();
            for (int i = 0; i < accArr.length; i++) {
                Item item = accArr[i].getItem();
                constantLoadouts += writeItem(item) + ",";
            }
            
            Container<Item>[] abilityArr = Player.Equipment.getAbilitySlots();
            for (int i = 0; i < abilityArr.length; i++) {
                Item item = abilityArr[i].getItem();
                constantLoadouts += writeItem(item) + ",";
            }
            constantLoadouts += writeItem(Player.Equipment.getCoreSlot().getItem()) + ",";
            writer.write(constantLoadouts + "\n");
            
            String weapons = "";
            for (int i = 0; i < obj.getLoadouts().size(); i++) {
                weapons += writeItem(obj.getLoadouts().get(i).getWepSlot().getItem()) + ",";
            }
            writer.write(weapons);
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

            int pX = 0;
            int pY = 0;
            for (int i = 0; i < 17; i++) {
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
                        HealAbility.healNum = Integer.parseInt(str);
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
                        obj.regenDelay = Integer.parseInt(str);
                        break;
                    case 12:
                        obj.timeSinceRegen = Integer.parseInt(str);
                        break;
                    case 13:
                        obj.hitToRegenDelay = Integer.parseInt(str);
                        break;
                    case 14:
                        obj.invincTime = Integer.parseInt(str);
                        break;
                    case 15:
                        obj.hp = Integer.parseInt(str);
                        break;
                    case 16:
                        obj.maxHP = Integer.parseInt(str);
                        break;
                }
            }
            obj.setLocation(pX, pY);

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

            scanner.nextLine();
            for (int i = 0; i < Equipment.accSlotNum; i++) {
                Player.Equipment.setAccSlot(i, parseItem(scanner.next()));
            }
            for (int i = 0; i < Equipment.abilitySlotNum; i++) {
                Player.Equipment.setAbilitySlot(i, parseItem(scanner.next()));
            }
            Player.Equipment.setCoreSlot(parseItem(scanner.next()));
            scanner.nextLine();
            for (int i = 0; i < obj.getLoadouts().size(); i++) {
                Equipment eqp = obj.getLoadouts().get(i);
                eqp.setWepSlot((Item) parseItem(scanner.next()));
            }
        }
    }

    public static void writeEntityData() throws IOException {
        File file = new File("data\\EntityData.dat");
        
        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < GameSolid.solids.size(); i++) {
                GameSolid solid = GameSolid.solids.get(i);
                if (solid instanceof Enemy enemy && !enemy.bossEnemy) {
                    writer.write(writeEnemy(enemy) + "\n");
                    continue;
                }
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

    public static void readWorldData(boolean newWorld) throws IOException {
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
            boolean[] resultArr = new boolean[result.size()];
            for (int i = 0; i < resultArr.length; i++) {
                resultArr[i] = result.get(i);
            }
            World.get().setEvents(resultArr);
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
            for (AudioType aType : AudioType.values()) {
                writer.write(aType.getVolume() + "\n");
            }
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
            for (int i = 0; i < AudioType.values().length && scanner.hasNextLine(); i++) {
                AudioType.values()[i].setVolume(Float.parseFloat(scanner.nextLine()));
            }
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
            readWorldData(newWorld);
            readSettingsData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static String writeItem(Item item) {
        if (item == null) {
            return "n";
        }

        String result = Integer.toString(ItemID.getID(item));
        if (item.canStack) {
            result += "(" + item.count + ")";
        }
        if (item instanceof Weapon wep && wep.enchantments.size() > 0) {
            result += "[";
            for (Enchantment enchantment : wep.enchantments) {
                result += "{";
                result += EnchantmentType.getID(enchantment.eType) + "|";
                result += StatusEffectType.getID(enchantment.sType) + "|";
                result += enchantment.floatArg;
                result += "}";
            }
            result += "]";
        }
        return result;
    }

    private static Item parseItem(String str) {
        if (str.equals("n")) {
            return null;
        }

        Item item;
        int parentheses = str.indexOf('(');
        int brackets = str.indexOf('[');

        if (parentheses > 0) {
            item = ItemID.getItem(Integer.parseInt(str.substring(0, parentheses)));
            int count = Integer.parseInt(str.substring(parentheses + 1, str.indexOf(")")));
            item.count = count;
        } else if (brackets > 0) {
            item = ItemID.getItem(Integer.parseInt(str.substring(0, brackets)));
        } else {
            item = ItemID.getItem(Integer.parseInt(str));
        }

        if (brackets > 0) {
            Weapon wep = (Weapon) item;
            int lowIndex = brackets + 2;
            while (true) {
                int highIndex = str.indexOf('}', lowIndex);
                if (lowIndex == -1 || highIndex == -1) {
                    break;
                }
                
                String subStr = str.substring(lowIndex, highIndex);
                Object[] data = readNums(subStr, '|');
                switch ((int) data[0]) {
                    case 0:
                        wep.enchantments.add(new Enchantment((float) data[2], StatusEffectType.getType((int) data[1])));
                        break;
                    case 1:
                        wep.enchantments.add(new Enchantment(StatusEffectType.getType((int) data[1])));
                        break;
                }
                lowIndex = highIndex + 2;
            }
        }
        return item;
    }

    private static String writeEnemy(Enemy enemy) {
        return EnemyID.getID(enemy) + "(" + 
            enemy.getX() + "," + enemy.getY() + "," + enemy.rotationDeg + "," + enemy.getLayer() + "," + enemy.getHP() +
            ")" + (enemy.isAlive() ? 1 : 0);
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
        int lowIndex = 0;
        while (true) {
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

            lowIndex = highIndex + 1;
        }
        return result.toArray();
    }
}
