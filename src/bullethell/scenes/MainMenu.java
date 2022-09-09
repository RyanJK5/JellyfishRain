package bullethell.scenes;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import bullethell.GameState;
import bullethell.Globals;
import bullethell.Player;
import bullethell.SaveSystem;
import bullethell.ui.Button;
import bullethell.ui.Text;

public final class MainMenu implements Scene {

    private static final MainMenu MAIN_MENU = new MainMenu();

    private Text title;
    private Button newGame;
    private Button continueGame;
    private Button saveExitButton;

    public static MainMenu get() {
        return MAIN_MENU;
    }

    private MainMenu() { }

    @Override
    public GameState getState() {
        return GameState.MENU;
    }

    @Override
    public void start(int x, int y) {
        try {
            SaveSystem.readSettingsData();
            Globals.setGameState(GameState.MENU);
            Player.get().kill();
            Player.setCameraPos(0, 0);
            final int titleWidth = 1184;
            title = new Text("Untitled Awesome", new Font(null, Font.BOLD, 128));
            title.setLocation(Globals.SCREEN_WIDTH / 2 - titleWidth / 2, 300);
            
            BufferedImage titleScreenButtons = ImageIO.read(new File("sprites\\TitleButtons.png"));
            BufferedImage trig1 = titleScreenButtons
              .getSubimage(0, 0, titleScreenButtons.getWidth(), titleScreenButtons.getHeight() / 3);
            newGame = new Button(trig1) {
                @Override
                protected void activate() {
                    try {
                        World.get().setEvents(false);
                        Globals.main.gameStart();
                		Globals.main.setScene(null, 0, 0);
                        SaveSystem.writeData(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            newGame.setLocation(Globals.SCREEN_WIDTH / 2 - newGame.getWidth() / 2, Globals.SCREEN_HEIGHT / 2);

            BufferedImage trig2 = titleScreenButtons.getSubimage(0, titleScreenButtons.getHeight() / 3, 
              titleScreenButtons.getWidth(), titleScreenButtons.getHeight() / 3);
            continueGame = new Button(trig2) {
                @Override
                protected void activate() {
                    if (!new File("data\\PlayerData.dat").exists()) {
                        return;
                    }
            		Globals.main.setScene(null, 0, 0);
                    SaveSystem.readData(false);
                }
            };
            continueGame.setLocation(newGame.getX(), newGame.getY() + newGame.getHeight());
            continueGame.setAltCondition(() -> !new File("data\\PlayerData.dat").exists());

            saveExitButton = new Button(titleScreenButtons.getSubimage(0, (int) (titleScreenButtons.getHeight() * (2f/3f)),
            titleScreenButtons.getWidth(), titleScreenButtons.getHeight() / 3)) {
                @Override
                protected void activate() {
                    SaveSystem.writeData(false);
                    System.exit(0);      
                }
            };
            saveExitButton.setLocation(continueGame.getX(), continueGame.getY() + continueGame.getHeight());
        } catch (IOException ioe) { ioe.printStackTrace(); }
    }

    @Override
    public void end() {
        Globals.setGameState(GameState.DEFAULT);
        Player.setCameraPos(Player.get().getCenterX() - Globals.SCREEN_WIDTH / 2, Player.get().getCenterY() - Globals.SCREEN_HEIGHT / 2);
        Player.get().getEquipmentInv().clear();
        Player.get().getInventory().clear();
        Player.get().revive();
        if (!Globals.alwaysShowUI) {
            Player.get().killUI();
        }
        title.kill();
        newGame.kill();
        continueGame.kill();
        saveExitButton.kill();
    }

    @Override
    public boolean isActive() {
        return title.isAlive();
    }
}
