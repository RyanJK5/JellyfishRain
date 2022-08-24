package bullethell;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

import bullethell.GameDisplay.ToggleAction;
import bullethell.scenes.Scene;

public final class Globals {

    private Globals() { }

    private static GameState GAME_STATE = GameState.DEFAULT;

    public static final Font DEFAULT_FONT = new Font("", 0, 20);
	public static final Color DEFAULT_COLOR = Color.DARK_GRAY;

    public static final int TIMER_DELAY = 12;

	public static final ToggleAction eAction = new ToggleAction(false);
    public static final Timer GLOBAL_TIMER = new Timer(TIMER_DELAY, null);
    
    public static Player player;
    
    public static GameDisplay main;
    public static JFrame frame;

    
    public static final Random rand = new Random();

    public static boolean mouseDown = false;
	public static boolean freezeCursor = false;
	public static boolean freezeHotkeys = false;

    public static boolean lockScreen = false;
    public static boolean alwaysShowUI = false;
    private static float volume = 1f;

	public static final boolean PAINT_QUADTREE = false;
	public static final boolean DEBUG_MODE = false;

    public static final int SCREEN_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static final int SCREEN_HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    public static final int NATIVE_SCREEN_WIDTH =  1920; 
	public static final int NATIVE_SCREEN_HEIGHT = 1080;
    
    public static final int WIDTH =  1920;
    public static final int HEIGHT = 1080;

    public static final int
        UP_ACTION = 0,
        LEFT_ACTION = 1,
        DOWN_ACTION = 2,
        RIGHT_ACTION = 3,
        INVENTORY_ACTION = 4,
        ABILITY_1_ACTION = 5,
        ABILITY_2_ACTION = 6,
        ABILITY_3_ACTION = 7,
        WEAPON_1_ACTION = 8,
        WEAPON_2_ACTION = 9,
        WEAPON_3_ACTION = 10
    ;

    public static void setVolume(float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException();
        }
        volume = value;
        Scene.changeSoundVolume();
    }

    public static float getVolume() {
        return volume;
    }

    public static void setGameState(GameState state) {
        switch (state) {
            case DEFAULT:
                freezeCursor = false;
                freezeHotkeys = false;
                GAME_STATE = GameState.DEFAULT;
                Player.get().setTimeSinceUI(1);
                break;
            case MENU:
                freezeCursor = false;
                freezeHotkeys = true;
                GAME_STATE = GameState.MENU;
                break;
            case ENCOUNTER:
                freezeCursor = false;
                freezeHotkeys = false;
                Player.get().showUI();
                Player.get().resetHeals();
                GAME_STATE = GameState.ENCOUNTER;
                break;
            case BOSS:
                freezeCursor = false;
                freezeHotkeys = false;
                Player.get().showUI();
                Player.get().resetHeals();
                GAME_STATE = GameState.BOSS;
                break;
            case CUTSCENE:
                freezeCursor = true;
                freezeHotkeys = true;
                GAME_STATE = GameState.CUTSCENE;
                break;
        }
    }

    public static void resetGameState() {
        setGameState(main.getScene() != null ? main.getScene().getState() : GameState.DEFAULT);
    }

    public static GameState getGameState() {
        return GAME_STATE;
    }

    public static final Map<Integer, Boolean> KEY_MAP = new LinkedHashMap<>();
    
    static {
        Integer[] keys = {
            KeyEvent.VK_W,
            KeyEvent.VK_A,
            KeyEvent.VK_S,
            KeyEvent.VK_D,
            KeyEvent.VK_E,
            KeyEvent.VK_F,
            KeyEvent.VK_SPACE,
            MouseEvent.BUTTON2,
            KeyEvent.VK_1,
            KeyEvent.VK_2,
            KeyEvent.VK_3,
        };
        Boolean[] values = new Boolean[11];
        for (int i = 0; i < values.length; i++) {
            values[i] = true;
        }
        values[7] = false;
        makeKeyMap(keys, values);
    }

    public static void makeKeyMap(Integer[] keys, Boolean[] values) {
        KEY_MAP.clear();
        for (int i = 0; i < keys.length; i++) {
            KEY_MAP.put(keys[i], values[i]);
        }
    }

    public static java.awt.image.BufferedImage getImage(String fileName) {
        try {
            return ImageIO.read(new File("Sprites\\" + fileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void changeKeybind(int action, int keyBind, boolean keyEvent) {
        Integer[] keys = KEY_MAP.keySet().toArray(new Integer[0]);
        if (keyBind < 0) {
            while (contains(keys, keyBind)) {
                keyBind--;
            }
        }
        keys[action] = keyBind;

        Boolean[] values = KEY_MAP.values().toArray(new Boolean[0]);
        values[action] = keyEvent;
        makeKeyMap(keys, values);
    }

    public static <T> boolean contains(T[] arr, T obj) {
        return Arrays.stream(arr).anyMatch(o -> o.equals(obj));
    }
}