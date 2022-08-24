package bullethell.ui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import bullethell.GameObject;
import bullethell.Globals;
import bullethell.Player;
import bullethell.items.StackableItem;

public class UI extends GameObject {

	public static final List<UI> allUI = new ArrayList<>();
    
    protected int fullWidth, fullHeight;
    private static boolean rightClick;
    
    
    protected UI(BufferedImage sprite, int layerNumber) {
        super(sprite, layerNumber);
        allUI.add(this);
        fullWidth = w;
        fullHeight = h;
    }

    public static UI overUI(int x, int y) {
		for (UI ui : UI.allUI) {
			if (new Rectangle(ui.x, ui.y, ui.fullWidth, ui.fullHeight).contains(x, y) &&
			ui.isAlive()) {
				return ui;
			}
		}
		return null;
	}

    public static void uiCheck(MouseEvent e) {
        rightClick = e.getButton() == MouseEvent.BUTTON3;

        UI ui = overUI(Player.cursorX(), Player.cursorY());
        Player player = Player.get();

        for (UI currentUI : allUI) {
            if (!(currentUI instanceof Inventory<?> inv)) {
                continue;
            }
            
            Thread thread = new Thread(() -> {
                if (inv.isAlive()) {
                    boolean stackable = false;

                    int i = 0;
                    outer: do {
                        try {
                            if (i == 1) {
                                for (int j = 0; j < 24; j++) {
                                    Thread.sleep(12);
                                    if (!(Globals.mouseDown && stackable && rightClick)) {
                                        if (Globals.mouseDown && !rightClick) {
                                            inv.moveItem(Player.cursorX(), Player.cursorY(), false);
                                        }
                                        break outer;
                                    }
                                }
                            }

                            inv.moveItem(Player.cursorX(), Player.cursorY(), e.getButton() == MouseEvent.BUTTON3);
                            stackable = inv.getSlot(Player.cursorX(), Player.cursorY()) instanceof StackableItem;
                            
                            Thread.sleep(12);
                            
                            i++;
                        } catch (InterruptedException ie) { }
                    } while (Globals.mouseDown && stackable && rightClick);
                }

                if (UI.overUI(Player.cursorX(), Player.cursorY()) == inv) {
                    Globals.main.mouseMoveListener.moveInv = inv;
                }
            });
            thread.start();
        }

        for (Container<?> container : Container.containers) {
            if (container.isAlive() &&
              new Rectangle(container.getX(), container.getY(), container.getWidth(), container.getHeight())
                .contains(Player.cursorX(), Player.cursorY()) 
              && container.moveItem(rightClick, null)) {
                return;
            }
        }
        
        if (e.getButton() != MouseEvent.BUTTON1) return;

        if (ui == null || ui instanceof Player.HealthBar) {
            player.setFiring(true);
            return;
        }
    }


    @Override
	public void toGhost() {
		super.toGhost();
        allUI.remove(this);
	}

	@Override
	public void unghost() {
		super.unghost();
        allUI.add(this);
	}
    
    public int getFullWidth() { return fullWidth; }
    public int getFullHeight() { return fullHeight; }

    public boolean equals(UI obj) {
        return super.equals(obj) && fullWidth == obj.fullWidth && fullHeight == obj.fullHeight;
    }
}
