package bullethell.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import bullethell.Globals;
import bullethell.Player;
import bullethell.Trigger;
import bullethell.items.Item;
import bullethell.items.StackableItem;
import bullethell.movement.Direction;

public class Inventory<T extends Item> extends UI implements Iterable<Container<T>> {
    
	protected final int width, height;
    private List<Container<T>> displayedSlots;
    private List<Container<T>> slots;
    private int currentStartIndex;
    private boolean interactable;
    private final Class<T> itemClass;
    private transient Trigger backButton, forwardButton;
    private transient JTextField searchBar;
    private final int size;
    private final Dimension collisionBox;
    private boolean navigable;
    private boolean movable;

    /**
     * @param itemClass The generic type for this inventory as a class variable.
     */
    public Inventory(Dimension dimensions, BufferedImage slotSprite, Class<T> itemClass) {
        super(slotSprite, 99);
        
        this.itemClass = itemClass;
        size = dimensions.width * dimensions.height;
        width = dimensions.width;
        height = dimensions.height;

        interactable = true;
        navigable = true;
        movable = true;

        slots = new ArrayList<>();
        expand();
        displayedSlots = slots;
        currentStartIndex = 0;

        makeNavigation();

        fullWidth = width * w;
        fullHeight = height * h;
        collisionBox = new Dimension(fullWidth, fullHeight + backButton.getHeight() + 10);
        setLocation(0, 0);
    }

    /**
     * @param itemClass The generic type for this inventory as a class variable.
     */
    public Inventory(Dimension dimensions, BufferedImage slotSprite, List<T> items, Class<T> itemClass) {
        this(dimensions, slotSprite, itemClass);

        for (int i = 0; i < items.size(); i++) {
            addItem(items.get(i));
        }
    }

    private void makeNavigation() {
        try {
            backButton = new Trigger(ImageIO.read(new File("sprites/BackButton.png")), 
              new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER}) {
    
                @Override
                protected void activate() {
                    if (currentStartIndex - size >= 0) {
                        currentStartIndex -= size;
                    }
                }
            };
            forwardButton = new Trigger(ImageIO.read(new File("sprites/ForwardButton.png")), 
              new Trigger.Type[] {Trigger.ON_CLICK, Trigger.CURSOR_OVER}) {
                    
                @Override
                protected void activate() {
                    if (currentStartIndex + size < displayedSlots.size()) {
                        currentStartIndex += size;
                    }
                }
            };
			layers.get(backButton.getLayer()).remove(backButton);
			layers.get(forwardButton.getLayer()).remove(forwardButton);
        } catch (IOException e) { e.printStackTrace(); }

        
        searchBar = new JTextField("");
		searchBar.setFocusTraversalKeysEnabled(false);
		searchBar.setSize(sprite.getWidth() * 4, 40);
		searchBar.setFont(Globals.DEFAULT_FONT);
        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) { }
            @Override
            public void insertUpdate(DocumentEvent e) { updated(); }
            @Override
            public void removeUpdate(DocumentEvent e)  { updated(); }

            private void updated() {
                currentStartIndex = 0;
                if (searchBar.getText().length() == 0) {
                    displayedSlots = slots;
                    return;
                }

                displayedSlots = new ArrayList<>();
                for (int i = 0; i < slots.size(); i++) {
                    if (slots.get(i).getItem() != null && slots.get(i).getItem().getName().toLowerCase().contains(searchBar.getText().toLowerCase())) {
                        displayedSlots.add(slots.get(i));
                    }
                }
                while (displayedSlots.size() == 0 || displayedSlots.size() % 80 != 0) {
                    displayedSlots.add(new Container<T>(sprite, itemClass, true));
                }
                sort(displayedSlots);
            }
        });
        searchBar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                for (Direction dir : Direction.values()) {
                    Globals.main.directionMap.put(dir, false);
                }     
            }
        });
        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    Globals.frame.requestFocusInWindow();
                }
            }
        });
        searchBar.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Player.setCursorPos(e.getX() + Player.cameraX(), e.getY() + Player.cameraY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Player.setCursorPos(e.getXOnScreen() + Player.cameraX(), e.getYOnScreen() + Player.cameraY());
            }
        });
        Globals.frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!searchBar.contains(e.getX(), e.getY()) && searchBar.hasFocus()) {
                    Globals.frame.requestFocusInWindow();
                }
            }
        });
    }

    @Override
    public Iterator<Container<T>> iterator() { return slots.iterator(); }

    @Override
    public void kill() {
        super.kill();
        for (Container<T> cont : slots) {
            cont.kill();
        }
        searchBar.setText("");
        displayedSlots = slots;
        Globals.main.remove(searchBar);
    }

    public void revive() {
        super.revive();
        for (Container<T> cont : slots) {
            cont.revive();
        }
        if (navigable) {
            Globals.main.add(searchBar);
        }
    }

    @Override
    public void paint(Graphics g) {
        int j = -1;
        for (int i = currentStartIndex; i < currentStartIndex + size; i++) {
            Container<T> currentSlot = displayedSlots.get(currentStartIndex * 2 + size - 1 - i);
            if (i % width == 0) {
                j++;
            }

            if (currentSlot.isAlive()) {
                currentSlot.setLocation(
                    x + (currentStartIndex * 2 + size - 1 - i) % width * w, 
                    y + (height - 1 - j) * h);
                currentSlot.paint(g);
            }
        }

        if (navigable) {
            backButton.setLocation(x + fullWidth / 2 - backButton.getWidth() / 2 - 30, y + fullHeight);
            forwardButton.setLocation(x + fullWidth / 2 - forwardButton.getWidth() / 2 + 30, y + fullHeight);
            searchBar.setLocation(-Player.cameraX() + x, -Player.cameraY() +  y + fullHeight);
            
            backButton.paint(g);
            forwardButton.paint(g);
        }
    }

    public void clear() {
        for (Container<T> cont : slots) {
            cont.setItem(null);
        }
    }

    public boolean contains(T obj) {
        return slots
          .stream()
          .anyMatch(
          (Container<T> cont) -> cont != null && cont.getItem() != null && cont.getItem().equals(obj));
    }

    private void expand() {
        for (int i = 0; i < size; i++) {
            slots.add(new Container<T>(sprite, itemClass, true));
        }
    }

    /**
     * Sorts the Inventory alphabetically and combines equal StackableItems. Should not be called directly 
     * unless the Inventory's StackableItems are being manipulated directly and a blank Container may
     * remain afterwards.
     */
    public void sort() {
        sort(slots);
    }

    private void sort(List<Container<T>> slots) {
        List<StackableItem> stacks = new ArrayList<>();
        outer: for (int i = 0; i < slots.size(); i++) {
            Container<T> cont = slots.get(i);

            if (cont.getItem() != null && !cont.getItem().isAlive()) {
                cont.setItem(null);
            }

            if (cont.getItem() instanceof StackableItem stack) {
                for (StackableItem oStack : stacks) {
                    if (oStack.equals(stack)) {
                        oStack.addFrom(0, stack);
                        if (!stack.isAlive()) {
                            cont.setItem(null);
                        }
                        continue outer;
                    }
                }
                stacks.add(stack);
            }
        }
        
        slots.sort((Container<T> o1, Container<T> o2) -> {
            if (o1.getItem() == null && o2.getItem() == null) {
                return 0;
            }
            if (o1.getItem() == null) {
                return 1;
            }
            if (o2.getItem() == null) {
                return -1;
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

        if (slots.size() <= size) {
            return;
        }
        if (slots.subList(size, slots.size())
          .stream()
          .allMatch((Container<T> container) -> container.getItem() == null)) {
            slots = slots.subList(0, size);
        }
    }

    public void setItems(List<T> items) {
        slots.clear();
        expand();
        for (int i = 0; i < items.size(); i++) {
            addItem(items.get(i));
        }
        sort();
    }

    public void addItem(T item) {
        int slotNum = -1;
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getItem() == null && slotNum == -1) {
                slotNum = i;
            }

            if (item == slots.get(i).getItem()) {
                return;
            }
        }
        if (slotNum >= 0) {
            slots.get(slotNum).setItem(item);
        } else {
            expand();
        }
        sort(slots);
        return;
    }

    public void replaceItem(T item, T replacement) {
        slots.get(getSlot(item)).setItem(replacement);
        sort(slots);
    }

    public void removeItem(T item) {
        replaceItem(item, null);
    }

    public void removeItem(int index) {
        setSlot(index, null);
    }

    public int getSlotNum(int x, int y) {
        for (int i = currentStartIndex; i < currentStartIndex + size; i++) {
            if (displayedSlots.get(i).isAlive() && 
              x >= displayedSlots.get(i).getX() && 
              x <= displayedSlots.get(i).getX() + displayedSlots.get(i).getWidth() &&
              y >= displayedSlots.get(i).getY() && 
              y <= displayedSlots.get(i).getY() + displayedSlots.get(i).getHeight()) {
                return i;
            }
        }
        return -1;
    }

    public T getSlot(int x, int y) {
        for (int i = currentStartIndex; i < currentStartIndex + size; i++) {
            if (displayedSlots.get(i).isAlive() && 
              x >= displayedSlots.get(i).getX() && 
              x <= displayedSlots.get(i).getX() + displayedSlots.get(i).getWidth() &&
              y >= displayedSlots.get(i).getY() && 
              y <= displayedSlots.get(i).getY() + displayedSlots.get(i).getHeight()) {
                return displayedSlots.get(i).getItem();
            }
        }
        return null;
    }

    public void move(int x, int y, int lastX, int lastY, Inventory<?> target) {
        if (!movable) {
            return;
        }

        if (isAlive() &&
          getSlot(x, y) == null &&
          Player.get().getCursorSlot() == null &&
          target == this) {
            int newX = this.x + (x - lastX);
            int newY = this.y + (y - lastY);
            if (newX < Player.cameraX()) {
                newX = Player.cameraX();
            } else if (newX > Player.cameraX() + Globals.SCREEN_WIDTH - collisionBox.width) {
                newX = Player.cameraX() + Globals.SCREEN_WIDTH - collisionBox.width;
            }
            if (newY < Player.cameraY()) {
                newY = Player.cameraY();
            } else if (newY > Player.cameraY() + Globals.SCREEN_HEIGHT - collisionBox.height) {
                newY = Player.cameraY() + Globals.SCREEN_HEIGHT - collisionBox.height;
            }

            for (UI ui : UI.allUI) {
                if (ui instanceof Player.HealthBar || ui == this || !ui.isAlive()) { 
                    continue;
                }

                if (ui instanceof Inventory<?> inv) {
                    if (new Rectangle(x, y, collisionBox.width, collisionBox.height)
                      .intersects(new Rectangle(inv.x, inv.y, inv.collisionBox.width, inv.collisionBox.height))) {
                        return;
                    }
                    continue;
                }

                if (new Rectangle(x, y, collisionBox.width, collisionBox.height)
                  .intersects(ui.getX(), ui.getY(), ui.fullWidth, ui.fullHeight)) {
                    return;
                }
            }
            setLocation(newX, newY);
        }
    
    }

    public boolean moveItem(int x, int y, boolean taking) {
        if (!interactable) return false;
        
        int index = getSlotNum(x, y);
        if (index >= 0) {
            if (displayedSlots.get(index).moveItem(taking, this)) {
                sort(displayedSlots);
                return true;
            }
        }
        
        return false;
    }

    public int getSlot(T item) {
        for (int i = 0; i < slots.size(); i++) {
            if (item == slots.get(i).getItem()) {
                return i;
            }
        }
        return -1;
    }

    public void setSlot(int slotNum, T item) {
        if (slotNum < 0) {
            throw new IllegalArgumentException();
        }
        if (slotNum >= slots.size()) {
            expand();
        }
        slots.get(slotNum).setItem(item);
        sort(slots);
    }

    public int emptySlots() {
        int result = slots.size();
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) != null) {
                result--;
            }
        }
        return result;
    }

    public int numberOfItems() {
        return slots.size() - emptySlots();
    }

    public List<Item> hasItems(T[] itemList) {
        List<Item> result = new ArrayList<>();
        for (int i = 0; i < itemList.length; i++) {
            Item oItem = itemList[i];
            
            List<Item> foundItems = new ArrayList<>();
            for (Container<T> cont : this) {
                Item item = cont.getItem();
                if (item == null || !item.equals(oItem)) {
                    continue;
                }
                foundItems.add(item);
            }
            result.addAll(foundItems);
        }
        return result;
    }

    public List<Container<T>> getSlots() { return slots; }
    public int getSize() { return size; }

    public void setHitbox(int width, int height) { collisionBox.setSize(width, height); }

    /**
     * Sets whether or not the inventory can change locations through the user dragging it.
     */
    public void setMovable(boolean bool) { movable = bool; }

    /**
     * Sets whether or not the items contained within the inventory can be manipulated by the user.
     */
    public void setInteractable(boolean bool) { interactable = bool; }

    /**
     * Sets whether or not the inventory can be sorted with the search bar and page buttons by the user.
     */
    public void setNavigable(boolean bool) {
        navigable = bool;
        if (bool) {
            Globals.main.add(searchBar);
            backButton.revive();
            forwardButton.revive();
            return;
        }
        Globals.main.remove(searchBar);
        backButton.kill();
        forwardButton.kill();
    }

    @Override
    public String toString() {
        String str = "{";
        for (int i = 0; i < slots.size(); i++) {
            str += (slots.get(i).getItem() == null ? "null" : slots.get(i).getItem().getName()) + ", ";
        }
        str += "}";
        return str;
    }

    public boolean equals(Inventory<T> obj) {
        boolean bool = false;
        for (int i = 0; i < slots.size(); i++) {
            Container<T> cont = slots.get(i);
            Container<T> oCont = slots.get(i);
            if (cont.equals(oCont)) {
                bool = true;
            }
        }
        return super.equals(obj) && bool;
    }
}
