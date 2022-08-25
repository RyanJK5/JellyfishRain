package bullethell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import javax.imageio.ImageIO;

import bullethell.Player.Equipment;
import bullethell.items.Ability;
import bullethell.items.Accessory;
import bullethell.items.Core;
import bullethell.items.Item;
import bullethell.items.MeleeWeapon;
import bullethell.items.Recipe;
import bullethell.items.StackableItem;
import bullethell.items.Weapon;
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
    
    private static final int ITEM_IDS_SIZE = 12;
    private static final Item[] ITEM_IDS = new Item[ITEM_IDS_SIZE];

    private static final int RECIPE_IDS_SIZE = 4;
    private static final Recipe[] RECIPE_IDS = new Recipe[RECIPE_IDS_SIZE];

    private static final int ENTITY_IDS_SIZE = 4;
    private static final Entity[] ENTITY_IDS = new Entity[ENTITY_IDS_SIZE];

    private SaveSystem() { }
    static {
        try {
            declareItemIDs();
            declareRecipeIDs();
            declareEntityIDs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void declareEntityIDs() throws IOException {
        Enemy pixie = new Enemy(Spritesheet.getSpriteSheet("Pixie"), 
          "Pixie", 50, 50, 10);
        StackableItem stack = new StackableItem((StackableItem) getItem(3), 5);
		stack.setCapacity(6);
		pixie.addItemToLootTable(stack, 1f);
        pixie.setIgnoreSolids(true);
        addEntity(pixie);
    }

    private static void declareItemIDs() throws IOException {
        // Sword
        ITEM_IDS[0] = new MeleeWeapon(ImageIO.read(new File("Sprites\\Sword.png")), "Swingy", 30, MeleeWeapon.DEFAULT_FIRE_TIME);
        
        // Casty
        ITEM_IDS[1] = new Weapon(ImageIO.read(new File("Sprites\\Staff.png")), 
          ImageIO.read(new File("Sprites\\TriangleBullet.png")), "Casty", 75, 5, 0, 100);
        
        // Shooty
        ITEM_IDS[2] = new Weapon(ImageIO.read(new File("Sprites\\Scepter.png")), "Shooty", 10, Weapon.DEFAULT_FIRE_TIME, 0, 0);
        
        // Magic Dust
        ITEM_IDS[3] = new StackableItem(ImageIO.read(new File("Sprites\\MagicDust.png")), "Magic Dust");
        
        // Health Charm
        Accessory acc = new Accessory(ImageIO.read(new File("Sprites\\HealthCharm.png")), "Health Charm");
        acc.setBonuses(new int[] {100, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        ITEM_IDS[4] = acc;
        
        // Metal
        ITEM_IDS[5] = new StackableItem(ImageIO.read(new File("Sprites\\Metal.png")), "Metal");

        // Strange Sigil
        ITEM_IDS[6] = new Item(ImageIO.read(new File("Sprites\\Sigil.png")), "Strange Sigil");

        // Teleport Charm, Dash Charm, Slow Charm, Heal Charm
        for (int i = 0; i < Ability.Type.values().length; i++) {
            Ability.Type type = Ability.Type.values()[i];
            ITEM_IDS[i + 7] = Ability.getAbility(type);
        }
    }

    private static void declareRecipeIDs() throws IOException {
        RECIPE_IDS[0] = new Recipe(new Item[] {
            getStackable(3, 20)
        }, getItem(1));
        RECIPE_IDS[1] = new Recipe(new Item[] {
            getStackable(3, 5)
        }, getItem(4));
        RECIPE_IDS[2] = new Recipe(new Item[] {
            getStackable(3, 4), 
            getStackable(5, 6)
        }, getItem(6));
        RECIPE_IDS[3] = new Recipe(new Item[] {
            getStackable(5, 8)
        }, getItem(0));

        Player.get().addRecipes(RECIPE_IDS);
    }

    public static StackableItem getStackable(int id, int count) {
        if (ITEM_IDS[id] instanceof StackableItem stack) {
            return new StackableItem(stack, count);
        }
        throw new ClassCastException("ID " + id + " is not a StackableItem");
    }

    private static int getID(GameObject obj) {
        Objects.requireNonNull(obj);
        if (obj instanceof Item item) {
            if (obj instanceof Recipe recipe) {
                for (int i = 0; i < RECIPE_IDS_SIZE; i++) {
                    Recipe testRecipe = RECIPE_IDS[i];
                    if (recipe.equals(testRecipe)) {
                        return i;
                    }
                }
            }
            for (int i = 0; i < ITEM_IDS_SIZE; i++) {
                Item testObj = ITEM_IDS[i];
                if (item.equals(testObj)) {
                    return i;
                }
            }
        }
        else if (obj instanceof Projectile proj) {
            for (int i = 0; i < ENTITY_IDS_SIZE; i++) {
                Entity testObj = ENTITY_IDS[i];
                if (testObj instanceof Projectile testProj && proj.equals(testProj)) {
                    return i;
                }
            }
        }
        else if (obj instanceof Enemy enemy) {
            for (int i = 0; i < ENTITY_IDS_SIZE; i++) {
                Entity testObj = ENTITY_IDS[i];
                if (testObj instanceof Enemy testEnem && enemy.equals(testEnem)) {
                    return i;
                }
            }
        }
        throw new UnindexedGameObjectException(obj);
    }

    public static Recipe getRecipe(int id) {
        return RECIPE_IDS[id].clone();
    }

    public static Item getItem(int id) {
        return ITEM_IDS[id].clone();
    }

    public static Entity getEntity(int id) {
        Entity result = ENTITY_IDS[id].clone();
        result.revive();
        result.unghost();
        return result;
    }

    private static String getData(Enemy obj) {
        return getData((Entity) obj) + "," + writePath(obj.path) + "," + obj.getSpeed() + "," + obj.getHP();
    }

    private static String getData(Projectile obj) {
        return getData((Entity) obj) + "," + writePath(obj.path) + "," + obj.getSpeed() + "," + obj.distanceTraveled;
    }

    private static String getData(GameObject obj) {
        return obj.getX() + "," + obj.getY() + "," + obj.rotationDeg + "," + obj.getLayer();
    }

    public static void writePlayerData(boolean onDeath) throws IOException {
        File file = new File("Data\\PlayerData.dat");
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
                cs = getID(obj.getCursorSlot()) + (obj.getCursorSlot() instanceof StackableItem stack ? "(" + stack.getCount() +")"
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
                inv += getID(item);
                if (item instanceof StackableItem stack) {
                    inv += "(" + stack.getCount() + ")";
                }
                inv += ",";
            }
            writer.write(inv);

            for (int i = 0; i < obj.getLoadouts().size(); i++) {
                Equipment eqp = obj.getLoadouts().get(i);
                
                String ld = "\n";

                Container<Accessory>[] accArr = eqp.getAccSlots();
                for (int j = 0; j < accArr.length; j++) {
                    Item item = accArr[j].getItem();
                    ld += (item != null ? getID(item) : "n") + ",";
                }
                Container<Ability>[] armArr = eqp.getAbilitySlots();
                for (int j = 0; j < armArr.length; j++) {
                    Item item = armArr[j].getItem();
                    ld += (item != null ? getID(item) : "n") + ",";
                }
                ld += (eqp.getWepSlot().getItem() != null ? getID(eqp.getWepSlot().getItem()) : "n") + ",";
                ld += (eqp.getCoreSlot().getItem() != null ? getID(eqp.getCoreSlot().getItem()) : "n") + ",";
                writer.write(ld);
            }

            String rr = "\n";
            for (Recipe recipe : obj.getResearchedRecipes()) {
                rr += getID(recipe) + ",";
            }
            writer.write(rr);
        }
    }

    public static void readPlayerData() throws IOException {
        File file = new File("Data\\PlayerData.dat");
        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter(",");

            Player obj = Player.get();
            obj.getInventory().clear();
            obj.getEquipmentInv().clear();
            obj.getResearchedRecipes().clear();
            obj.applyStatChanges(new float[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, new int[10]);

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
                    eqp.setAccSlot(j, (Accessory) parseItem(str));
                }
                for (int j = 0; j < Equipment.abilitySlotNum; j++) {
                    eqp.setAbilitySlot(j, (Ability) parseItem(scanner.next()));
                }
                eqp.setWepSlot((Weapon) parseItem(scanner.next()));
                eqp.setCoreSlot((Core) parseItem(scanner.next()));
                scanner.nextLine();
            }

            while (scanner.hasNext()) {
                obj.addRecipe(parseRecipe(scanner.next()));
            }
        }
    }

    public static void writeEntityData() throws IOException {
        File file = new File("Data\\EntityData.dat");
        
        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < GameSolid.solids.size(); i++) {
                GameSolid solid = GameSolid.solids.get(i);
                if (solid instanceof Player) {
                    continue;
                }
                try {
                    if (solid instanceof Enemy enemy) {
                        writer.write("e" + getID(enemy) + "(" + getData(enemy) + ")" + (enemy.isAlive() ? 1 : 0) + "\n");
                        continue;
                    }
                    if (solid instanceof Projectile proj && !proj.friendly()) {
                        writer.write("p" + getID(proj) + "(" + getData(proj) + ")" + (proj.isAlive() ? 1 : 0) + "\n");
                        continue;
                    }
                } catch (UnindexedGameObjectException uigoe) { System.err.println(solid); }
            }
        }
    }

    public static void readEntityData() throws IOException {
        File file = new File("Data\\EntityData.dat");
        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String str = scanner.nextLine();
                GameObject result = parseEntity(str);
                result.unghost();
            }
        }
    }

    public static void writeWorldData() throws IOException {
        File file = new File("Data\\WorldData.dat");

        try (FileWriter writer = new FileWriter(file)) {
            for (boolean bool : World.get().getEvents()) {
                writer.write((bool ? "1" : "0") + "\n");
            }
        }
    }

    public static void loadWorld(boolean newWorld) throws IOException {
        File file = new File("Data\\WorldData.dat");
        
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
        File file = new File("Data\\SettingsData.dat");
        
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
        File file = new File("Data\\SettingsData.dat");
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

    private static void addEntity(Entity ent) {
        for (int i = 0; i < ENTITY_IDS_SIZE; i++) {
            if (ENTITY_IDS[i] == null) {
                ENTITY_IDS[i] = ent;
                ent.permakill();
                ent.toGhost();
                return;
            }
        }
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
            Item item = getItem(Integer.parseInt(str.substring(0, parentheses)));
            StackableItem stack = new StackableItem(item.getSprite(), item.getName());
            stack.setCount(count);
            return stack;
        } else {
            return getItem(Integer.parseInt(str));
        }
    }

    private static Recipe parseRecipe(String str) {
        if (str.equals("n")) {
            return null;
        }
        return getRecipe(Integer.parseInt(str));
    }

    private static Entity parseEntity(String str) {
        Entity result;
        char entType = str.charAt(0);

        if (str.charAt(0) == 'e') {
            result = getEntity(Integer.parseInt(str.substring(1, str.indexOf('('))));
            result.setHP(Integer.parseInt(str.substring(str.lastIndexOf(',') + 1, str.lastIndexOf(')'))));
        } else {
            Projectile proj = (Projectile) getEntity(Integer.parseInt(str.substring(1, str.indexOf('('))));
            proj.distanceTraveled = Double.parseDouble(str.substring(str.lastIndexOf(',') + 1, str.lastIndexOf(')')));
            result = proj;
        }

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
                    result.setPath(parsePath(baseData.substring(lowIndex, highIndex), result));
                    break;
                case 5:
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

    private static String writePath(Path path) {
        String result = "";
        if (path instanceof AngledPath aPath) {
            result = "a(" + Math.toDegrees(aPath.getAngle()) + ")";
        }
        else if (path instanceof ChargePath cPath) {
            result = "h(" + cPath.getChargeTime() + "|" + cPath.getChargeDelay() + "|" + cPath.getNumOfCharges() + ")";
        }
        else if (path instanceof CirclePath cPath) {
            result = "c(" + cPath.getCenter().x + "|" + cPath.getCenter().y + "|" + cPath.getCurrent().x + "|" + 
              cPath.getCurrent().y + "|" + cPath.getIncrRate() + "|" + (cPath.clockwise() ? "1" : "0") + ")";
        }
        else if (path instanceof LinePath lPath) {
            result = "l(" + (int) lPath.line.getX1() + "|" + (int) lPath.line.getY1() + "|" + (int) lPath.line.getX2() + "|"
              + (int) lPath.line.getY2() + ")" + (lPath.loop ? "1" : "0") + ")";
        }
        else if (path instanceof SeekingPath) {
            result = "e";
        }
        else if (path instanceof StraightPath sPath) {
            result = "d(" + sPath.xDif + "|" + sPath.yDif + ")";
        }
        else {
            result = "n";
        }
        return result;
    }

    private static Path parsePath(String str, Entity sender) {

        char type = str.charAt(0);
        // seekpath
        if (type == 'e') { 
            return new SeekingPath(sender, Player.get());
        } else if (type == 'n') {
            return Path.DEFAULT_PATH;
        }
        Path result = null;
        String baseData = str.substring(str.indexOf('(') + 1, str.lastIndexOf(')'));
        switch (type) {
            // angledpath -> a(angle)
            case 'a':
                result = new AngledPath(Double.parseDouble(baseData));
                break;
            // chargepath -> h(chargeTime, chargeDelay, chargeNum)
            case 'h':
                Object[] cNums = readNums(baseData, '|');
                result = new ChargePath(sender, Player.get(), (int) cNums[0], (int) cNums[1]);
                ((ChargePath) result).setNumOfCharges((int) cNums[2]);
                break;
            // circlepath -> c(centerX, centerY, currentX, currentY, incrRate, clockwise)
            case 'c':
                Object[] nums = readNums(baseData, '|');
                result = new CirclePath((int) nums[0], (int) nums[1], (int) nums[2], (int) nums[3], (float) nums[4], 
                  (int) nums[5] == 1);
                break;
            // linepath -> l(x1, x2, y1, y2, loop)
            case 'l':
                Object[] lNums = readNums(baseData, '|');
                result = new LinePath((int) lNums[0], (int) lNums[1], (int) lNums[2], (int) lNums[3], (int) lNums[5] == 1);
                break;
            // straightpath -> d(xDif, yDif)
            case 'd':
                Object[] dNums = readNums(baseData, '|');
                result = new StraightPath((int) dNums[0], (int) dNums[1]);
                break;
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
            super(obj instanceof Item item ? item.getName() : obj + " could not be found in the ID list.");
        }
    }
}
