package bullethell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class Quadtree <T extends GameObject> {
    
    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 5;

    private final int level;
    private final List<T> objects;
    private final Rectangle bounds;
    private final Quadtree<T>[] nodes;

    @SuppressWarnings("unchecked")
    private Quadtree(int level, Rectangle bounds) {
        this.level = level;
        objects = new ArrayList<>();
        this.bounds = bounds;
        
        nodes = new Quadtree[4];
    }

    public Quadtree(Rectangle bounds) {
        this(0, bounds);
    }

    public List<T> retrieve(List<T> returnObjects, T obj) {
        int index = getIndex(obj);
        if (index != -1 && nodes[0] != null) {
            nodes[index].retrieve(returnObjects, obj);
        }

        returnObjects.addAll(objects);

        return returnObjects;
    }

    public void clear() {
        objects.clear();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }

    public void insert(T obj) {
        if (nodes[0] != null) {
            int index = getIndex(obj);

            if (index != -1) {
                nodes[index].insert(obj);
                return;
            }
        }

        objects.add(obj);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    private int getIndex(T obj) {
        int index = -1;
        double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
        double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);

        // Object can completely fit within the top quadrants
        boolean topQuadrant = (obj.getY() < horizontalMidpoint
                && obj.getY() + obj.getHeight() < horizontalMidpoint);
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = (obj.getY() > horizontalMidpoint);

        // Object can completely fit within the left quadrants
        if (obj.getX() < verticalMidpoint && obj.getX() + obj.getWidth() < verticalMidpoint) {
            if (topQuadrant) {
                index = 1;
            } else if (bottomQuadrant) {
                index = 2;
            }
        }
        // Object can completely fit within the right quadrants
        else if (obj.getX() > verticalMidpoint) {
            if (topQuadrant) {
                index = 0;
            } else if (bottomQuadrant) {
                index = 3;
            }
        }

        return index;
    }

    private void split() {
        int subWidth = (int) (bounds.getWidth() / 2);
        int subHeight = (int) (bounds.getHeight() / 2);
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();

        nodes[0] = new Quadtree<T>(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new Quadtree<T>(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new Quadtree<T>(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new Quadtree<T>(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.RED);
        g2d.draw(bounds);
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].paint(g);
            }
        }
    }
}