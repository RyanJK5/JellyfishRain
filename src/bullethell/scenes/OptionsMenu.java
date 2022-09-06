package bullethell.scenes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import bullethell.AudioType;
import bullethell.GameState;
import bullethell.Globals;
import bullethell.Player;
import bullethell.SaveSystem;
import bullethell.movement.Direction;
import bullethell.ui.Button;
import bullethell.ui.SliderButton;
import bullethell.ui.ToggleButton;

public final class OptionsMenu extends KeyAdapter implements Scene {

    
    private static final BufferedImage buttons = Globals.getImage("OptionsButtons");
    private static final BufferedImage bindButtons = Globals.getImage("ControlsButtons");
    
    private static final OptionsMenu OPTIONS_MENU = new OptionsMenu();

    // hub screen
    private Button controlsButton;
    private Button videoButton;
    private Button audioButton;
    private Button saveExitButton;

    // controls screen
    private KeyBindButton[] keyBindButtons = new KeyBindButton[11];
    private OptionsButton resetToDefault;

    // video screen
    private Button[] videoButtons = new Button[2];

    //Options audio screen
    private Button[] audioButtons = new Button[3];

    public static OptionsMenu get() {
        return OPTIONS_MENU;
    }

    private OptionsMenu() {
        Globals.frame.addKeyListener(this);
        controlsSetup();
        videoSetup();
        audioSetup();
        exitSetup();
        endHub();
    }

    @Override
    public GameState getState() {
        return GameState.MENU;
    }

    @Override
    public void start(int x, int y) {
        startHub();
        for (Direction dir : Globals.main.directionMap.keySet()) {
            Globals.main.directionMap.put(dir, false);
        }
        if (Player.get().getInventory().isAlive()) {
            Globals.eAction.toggle();
        }
        Globals.freezeHotkeys = true;

    }
    
    private void controlsSetup() {
        controlsButton = new Button(buttons.getSubimage(0, 0, buttons.getWidth(), buttons.getHeight() / 4)) {
            @Override
            protected void activate() {
                endHub();
                for (Button button : keyBindButtons) {
                    button.revive();
                }
                resetToDefault.revive();
            }
        };

        Integer[] keys = Globals.KEY_MAP.keySet().toArray(new Integer[0]);
        for (int i = 0; i < keyBindButtons.length + 1; i++) {
            if (i == keyBindButtons.length) {
                resetToDefault = new OptionsButton(bindButtons.getSubimage(bindButtons.getWidth() / 2 * (i % 2), 
                  i / 2 * bindButtons.getHeight() / 6, bindButtons.getWidth() / 2, bindButtons.getHeight() / 6 + 1)) {
                    @Override
                    protected void activate() {
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
                            KeyEvent.VK_3
                        };
                        Boolean[] values = new Boolean[11];
                        for (int i = 0; i < values.length; i++) {
                            values[i] = true;
                        }
                        values[7] = false;

                        Globals.makeKeyMap(keys, values);
                        for (int i = 0; i < keyBindButtons.length; i++) {
                            KeyBindButton button = keyBindButtons[i];
                            button.setCode(keys[i], values[i]);
                        }
                    }
                };
                resetToDefault.kill();
                break;
            }

            keyBindButtons[i] = new KeyBindButton(bindButtons.getSubimage(bindButtons.getWidth() / 2 * (i % 2), 
                i / 2 * bindButtons.getHeight() / 6, bindButtons.getWidth() / 2, bindButtons.getHeight() / 6 + 1), 
                keys[i], i, Globals.KEY_MAP.get(keys[i]));
            keyBindButtons[i].kill();
            if (i == 0) {
                keyBindButtons[i].setLocation(Player.cameraX() + Globals.SCREEN_WIDTH / 2 - keyBindButtons[i].getWidth(),
                    Player.cameraY() + Globals.SCREEN_HEIGHT / 2 - buttons.getHeight());
            } else {
                keyBindButtons[i].setLocation(keyBindButtons[0].getX() + bindButtons.getWidth() / 2 * (i % 2), 
                    keyBindButtons[0].getY() + i / 2 * bindButtons.getHeight() / 6);
            }
        }
    }

    private void videoSetup() {
        try {
            videoButton = new Button(buttons.getSubimage(0, (int) (buttons.getHeight() / 4), 
              buttons.getWidth(), buttons.getHeight() / 4)) {
                
                @Override
                protected void activate() {
                    endHub();
                    for (Button button : videoButtons) {
                        button.revive();
                    }
                }
            };
            
            BufferedImage settingsButtons = Globals.getImage("VideoButtons");

            videoButtons[0] = new ToggleOptionsButton(
              settingsButtons.getSubimage(0, 0, settingsButtons.getWidth(), settingsButtons.getHeight() / 2),
              Globals.class.getField("lockScreen"), null);
            videoButtons[0].kill();
            
            videoButtons[1] = new ToggleOptionsButton(
              settingsButtons.getSubimage(0, settingsButtons.getHeight() / 2, 
              settingsButtons.getWidth(), settingsButtons.getHeight() / 2),
              Globals.class.getField("alwaysShowUI"), null) {
                @Override
                protected void activate() {
                    super.activate();
                    if (Globals.alwaysShowUI) {
                        Player.get().showUI();
                    } else {
                        Player.get().setTimeSinceUI(Player.UI_HIDE_COOLDOWN);
                    }
                }
              };
            videoButtons[1].kill();
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
    }

    private void audioSetup() {
        audioButton = new Button(buttons.getSubimage(0, (int) (buttons.getHeight() / 2), 
        buttons.getWidth(), buttons.getHeight() / 4)) {
            @Override
            protected void activate() {
                endHub();
                for (Button button : audioButtons) {
                    button.revive();
                }
            }
        };

        BufferedImage img = Globals.getImage("VolumeButtons");
        for (int i = 0; i < AudioType.values().length; i++) {
            AudioType aType = AudioType.values()[i];
            audioButtons[i] = new SliderOptionsButton(img.getSubimage(0, i * img.getHeight() / 3, img.getWidth(), img.getHeight() / 3), 
                aType::setVolume, aType::getVolume)
            ;
            audioButtons[i].kill();
        }
    }

    private void exitSetup() {
        saveExitButton = new Button(buttons.getSubimage(0, (int) (buttons.getHeight() * 0.75f),
          buttons.getWidth(), buttons.getHeight() / 4)) {
            @Override
            protected void activate() {
                end();
                Globals.main.setScene(MainMenu.get(), 0, 0);
                SaveSystem.writeData(false);
                World.get().end();
            }
        };
    }

    private void endHub() {
        controlsButton.kill();
        videoButton.kill();
        audioButton.kill();
        saveExitButton.kill();
    }

    private void startHub() {
        controlsButton.revive();
        controlsButton.setLocation(Player.cameraX() + Globals.SCREEN_WIDTH / 2 - buttons.getWidth() / 2,
            Player.cameraY() + Globals.SCREEN_HEIGHT / 2 - buttons.getHeight() / 2);
        for (int i = 0; i < keyBindButtons.length + 1; i++) {
            if (i == keyBindButtons.length) {
                resetToDefault.setLocation(keyBindButtons[0].getX() + bindButtons.getWidth() / 2 * (i % 2), 
                  keyBindButtons[0].getY() + i / 2 * bindButtons.getHeight() / 6);
            }
            else if (i == 0) {
                keyBindButtons[i].setLocation(Player.cameraX() + Globals.SCREEN_WIDTH / 2 - keyBindButtons[i].getWidth(),
                    Player.cameraY() + Globals.SCREEN_HEIGHT / 2 - buttons.getHeight());
            } 
            else {
                keyBindButtons[i].setLocation(keyBindButtons[0].getX() + bindButtons.getWidth() / 2 * (i % 2), 
                    keyBindButtons[0].getY() + i / 2 * bindButtons.getHeight() / 6);
            }
        }

        videoButton.revive();
        videoButton.setLocation(controlsButton.getX(), controlsButton.getY() + controlsButton.getHeight());
        videoButtons[0].setLocation(controlsButton.getX(), controlsButton.getY());
        videoButtons[1].setLocation(videoButtons[0].getX(), videoButtons[0].getY() + videoButtons[0].getHeight());

        audioButton.revive();
        audioButton.setLocation(videoButton.getX(), videoButton.getY() + videoButton.getHeight());

        for (int i = 0; i < audioButtons.length; i++) {
            audioButtons[i].setLocation(controlsButton.getX(), controlsButton.getY() + audioButtons[i].getHeight() * i);
        }

        saveExitButton.revive();
        saveExitButton.setLocation(audioButton.getX(), audioButton.getY() + audioButton.getHeight());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_ESCAPE) && (
          (keyBindButtons[0] != null && keyBindButtons[0].isAlive() && 
            Arrays.stream(keyBindButtons).noneMatch(obj -> obj.code < 0 && obj.previousCode != obj.code)) ||
          (videoButtons[0] != null && videoButtons[0].isAlive()) ||
          (audioButtons[0] != null && audioButtons[0].isAlive()))) {
            end();
            start(0, 0);
        }
    }

    @Override
    public void end() {
        try {
            SaveSystem.writeSettingsData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Globals.freezeHotkeys = false;
        endHub();
        for (OptionsButton button : keyBindButtons) {
            button.kill();
        }
        for (Button button : videoButtons) {
            button.kill();
        }
        for (Button button : audioButtons) {
            button.kill();
        }
        resetToDefault.kill();
    }

    @Override
    public boolean isActive() {
        return (controlsButton != null && controlsButton.isAlive());
    }

    private class KeyBindButton extends OptionsButton {

        final BindInputListener inputListener;
        int previousCode;
        int action;
        int code;
        boolean key;

        public KeyBindButton(BufferedImage sprite, int keyCode, int action, boolean key) {
            super(sprite);
            this.key = key;
            this.code = keyCode;
            this.action = action;
            previousCode = code;
            
            inputListener = new BindInputListener();
        }

        public void setCode(int code, boolean key) {
            this.code = code;
            this.key = key;
            previousCode = code;
            Globals.changeKeybind(action, code, key);
        }

        @Override
        public void kill() {
            super.kill();
            inputListener.remove();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            
            g.setFont(new Font(null, Globals.DEFAULT_FONT.getStyle(), 40));
            g.setColor(Color.BLACK);
            if (code >= 0) {
                int drawnCode = code;
                if (!key && code == 2) {
                    drawnCode = 3;
                } else if (!key && code == 3) {
                    drawnCode = 2;
                }
                g.drawString(key ? KeyEvent.getKeyText(code) : "Button " + drawnCode, x + 10, y + h - 20);
            }
            if (getAltCondition() != null && getAltCondition().get()) {
                g.setColor(new Color(0, 0, 0, 0.5f));
                g.fillRect(x, y, w, h);
            }
            else if (getBounds().contains(Player.cursorX(), Player.cursorY())) {
                g.setColor(new Color(1, 1, 1, 0.5f));
                g.fillRect(x, y, w, h);
            }
        }

        @Override
        public void activate() {
            code = -1;
            for (KeyBindButton button : keyBindButtons) {
                if (button == this) {
                    continue;
                }
                button.setCode(button.previousCode, button.key);
                button.inputListener.remove();
            }
            inputListener.add();
        }

        private class BindInputListener extends java.awt.event.MouseAdapter implements java.awt.event.KeyListener {

            public void remove() {
                Globals.frame.removeKeyListener(this);
                Globals.frame.removeMouseListener(this);
            }

            public void add() {
                Globals.frame.addKeyListener(inputListener);
                Globals.frame.addMouseListener(inputListener);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                updateBindings(e.getKeyCode(), true);
            }
    
            @Override
            public void mousePressed(MouseEvent e) {
                updateBindings(e.getButton(), false);
            }
            
            public void updateBindings(int keyCode, boolean newKey) {
                
                if (!newKey && keyCode == MouseEvent.BUTTON1) {
                    return;
                }

                if (keyCode == KeyEvent.VK_ESCAPE) {
                    setCode(previousCode, key);
                    remove();
                    return;
                }

                for (KeyBindButton button : keyBindButtons) {
                    if (button.inputListener != this && button.code == keyCode && button.key == newKey) {
                        button.setCode(-1, true);
                    }
                }
                
                setCode(keyCode, newKey);
                remove();
            }

            @Override
            public void keyTyped(KeyEvent e) { }
            @Override
            public void keyReleased(KeyEvent e) { }
        }
    }

    private abstract class OptionsButton extends Button {

        private boolean released;

        public OptionsButton(BufferedImage sprite) {
            super(sprite);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!released) {
                return;
            }
            super.mousePressed(e);
        }

        int i;
        @Override
        public void kill() {
            super.kill();
            released = false;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isAlive) {
                released = true;
            }
        }
    }

    private class ToggleOptionsButton extends ToggleButton {
        
        private boolean released;

        public ToggleOptionsButton(BufferedImage sprite, Field field, Object obj) {
            super(sprite, field, obj);
        }

        @Override
        public void kill() {
            super.kill();
            released = false;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!released) {
                return;
            }
            super.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isAlive) {
                released = true;
            }
        }
    }

    private class SliderOptionsButton extends SliderButton {
        
        private boolean released;
        
        public SliderOptionsButton(BufferedImage sprite, Consumer<Float> setMethod, Supplier<Float> getMethod) {
            super(sprite, setMethod, getMethod);
        }

        @Override
        public void kill() {
            super.kill();
            released = false;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!released) {
                return;
            }
            super.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isAlive) {
                released = true;
            }
        }
    }
}