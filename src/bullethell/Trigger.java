package bullethell;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import bullethell.ui.Container;
import bullethell.ui.UI;

public abstract class Trigger extends GameObject implements ActionListener, MouseListener, KeyListener {
    
    public static final int DEFAULT_RANGE = 200;
    
    public static final Type
      ON_CLICK = Type.ON_CLICK,
      RIGHT_CLICK = Type.RIGHT_CLICK,
      CURSOR_OVER = Type.CURSOR_OVER,
      TARGET_OVER = Type.TARGET_OVER,
      TARGET_IN_RANGE = Type.TARGET_IN_RANGE,
      NO_UI_PRESENT = Type.NO_UI_PRESENT,
      ON_KEY_PRESS = Type.ON_KEY_PRESS,
      LEFT_CLICK = Type.LEFT_CLICK;
    /**
     * Use of members in {@code Trigger.Type} is discouraged. The constants defined in
     * {@code Trigger} should be used instead.
     */
    public static enum Type {
        @Deprecated ON_CLICK,
        @Deprecated RIGHT_CLICK,
        @Deprecated CURSOR_OVER,
        @Deprecated TARGET_OVER,
        @Deprecated TARGET_IN_RANGE,
        @Deprecated NO_UI_PRESENT,
        @Deprecated ON_KEY_PRESS,
        @Deprecated LEFT_CLICK;
    }

    private final List<Integer> keyCodes = new ArrayList<>();
    private boolean rightClick = false;
    private boolean leftClick = false;
    private boolean done = false;
    private boolean repeat = true;
    private boolean pauseFire = false;
    private int range = DEFAULT_RANGE;
    private int listenerType;

    private GameObject target;
    private Predicate<GameObject> condition;

    public Trigger(BufferedImage sprite, Type[] activationType) {
        this(sprite, 1, activationType);
    }

    public Trigger(BufferedImage sprite, int layer, Type[] activationType) {
        super(sprite, layer);
        setActivationType(activationType);        
        setTarget(Player.getNullable());
    }

    public Trigger(BufferedImage sprite, int layer, Predicate<GameObject> condition) {
        super(sprite, layer);
        this.condition = condition;
        setTarget(Player.getNullable());
        Globals.GLOBAL_TIMER.addActionListener((e) -> tryActivate());
    }

    public Trigger(BufferedImage sprite, int layer, Type[] condition1, Predicate<GameObject> condition2) {
        this(sprite, layer, condition1);
        
        condition = condition.and(condition2);   
    }

    private void setActivationType(Type[] activationType) {
        Predicate<GameObject> inRange = (obj) -> 
          Point.distance(getCenterX(), getCenterY(), obj.getCenterX(), obj.getCenterY()) <= range;
          Predicate<GameObject> overTrigger = (obj) -> { 
            Area area = obj instanceof GameSolid sol ? new Area(sol.getHitbox()) : new Area(new Rectangle(obj.x, obj.y, obj.w, obj.h));
            Area area2 = new Area(new Rectangle(x, y, w, h));
            area.intersect(area2);
            return !(area.equals(new Area()));
        };
        Predicate<GameObject> cursorOver = (obj) ->
          new Rectangle(x, y, w, h).contains(Player.cursorX(), Player.cursorY());
        
        Predicate<GameObject> noUI = (obj) -> {
            UI ui = UI.overUI(Player.cursorX(), Player.cursorY());
            return Container.overContainer(Player.cursorX(), Player.cursorY()) &&
              (ui == null || ui instanceof Player.HealthBar);
        };
        
        boolean onClick = false;
        boolean onKeyPress = false;
        condition = null;
        for (int i = 0; i < activationType.length; i++) {
            switch (activationType[i]) {
                case CURSOR_OVER:
                    condition = condition == null ? cursorOver : condition.and(cursorOver);
                    pauseFire = true;
                    break;
                case RIGHT_CLICK:
                    rightClick = true;
                    break;
                case ON_CLICK:
                    onClick = true;
                    break;
                case TARGET_IN_RANGE:
                    condition = condition == null ? inRange : condition.and(inRange);
                    break;
                case TARGET_OVER:
                    condition = condition == null ? overTrigger : condition.and(overTrigger);
                    break;
                case NO_UI_PRESENT:
                    condition = condition == null ? noUI : condition.and(noUI);
                    break;
                case ON_KEY_PRESS:
                    onKeyPress = true;
                    break;
                case LEFT_CLICK:
                    leftClick = true;
                    break;
            }
        }
        if (condition == null) {
            condition = (obj) -> true;
        }

        if (onClick) {
            listenerType = 2;
            Globals.frame.addMouseListener(this);
        } else if (onKeyPress) {
            listenerType = 1;
            Globals.frame.addKeyListener(this);
        } else {
            listenerType = 0;
            Globals.GLOBAL_TIMER.addActionListener(this);
        }
    }

    @Override
	public void toGhost() {
		super.toGhost();
        Globals.frame.removeMouseListener(this);
        Globals.main.removeKeyListener(this);
        Globals.GLOBAL_TIMER.removeActionListener(this);
	}

    @Override
    public void unghost() {
        super.unghost();
        switch (listenerType) {
            case 0: 
                Globals.GLOBAL_TIMER.addActionListener(this);
                break;
            case 1:
                Globals.main.addKeyListener(this);
                break;
            case 2:
                Globals.frame.addMouseListener(this);
                break;
        }
    }

    public void setTarget(GameObject newTarget) {
        target = newTarget; 
    }
    public GameObject getTarget() { return target; }
    
    public void setLoop(boolean loops) { repeat = loops; }
    public void setRange(int range) { this.range = range; }

    public void addCondition(Predicate<GameObject> newCondition) { condition = condition.and(newCondition); }
    public void setCondition(Predicate<GameObject> newCondition) { condition = newCondition; }

    public void addKeyCode(int keyCode) { keyCodes.add(keyCode); }

    private boolean tryActivate() {
        if (isAlive() && condition.test(target) && !done) {
            activate();
            if (!repeat) {
                done = true;
            }
            return true;
        }
        return false;
    }

    protected abstract void activate();

    public boolean equals(Trigger oTrigger) {
        return super.equals(oTrigger) && target.equals(oTrigger.target) && condition.equals(oTrigger.condition) &&
          rightClick == oTrigger.rightClick && done == oTrigger.done && repeat == oTrigger.repeat;
    }

    @Override
    public Trigger clone() {
        Trigger old = this;
        Trigger obj = new Trigger(sprite, layerNumber, condition) {
			@Override
            protected void activate() {
                old.activate();
            }
        };
        obj.done = done;
        obj.repeat = repeat;
        obj.target = target;
        
		obj.setLocation(getLocation());
		obj.isAlive = isAlive;
		obj.essential = essential;
		return obj;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tryActivate();
    }
    
    @Override 
    public void keyPressed(KeyEvent e) {
        if (keyCodes.size() == 0 || keyCodes.contains(e.getKeyCode())) {
            tryActivate();
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if ((!rightClick && !leftClick) ||
          (rightClick && e.getButton() == MouseEvent.BUTTON3) || 
          (leftClick && e.getButton() == MouseEvent.BUTTON1)) {
            if (tryActivate() && pauseFire) {
                Player.get().setFiring(false);
            }
        }
    }
    
    @Override public final void keyTyped(KeyEvent e) { }
    @Override public final void keyReleased(KeyEvent e) { }
    @Override public final void mouseClicked(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public final void mouseEntered(MouseEvent e) { }
    @Override public final void mouseExited(MouseEvent e) { }
}
