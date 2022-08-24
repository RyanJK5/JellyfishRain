package bullethell.movement;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Line2D;

public class LinePath extends AngledPath {
	
    public final Line2D line;
	protected double distance;
    protected double distanceTraveled;
    public boolean loop;
    protected final Point origin;

    public LinePath(Line2D line, boolean loop) {
        super(0);
        double angle = Math.toDegrees(Math.atan2(line.getX2() - line.getX1(), line.getY2() - line.getY1()));
        if (angle < 0) {
            angle += 360;
        }
        this.line = line;
        this.angle = Math.toRadians(angle);

        this.loop = loop;
        origin = new Point((int) line.getX1(), (int) line.getY1());
        distance = origin.distance(line.getX2(), line.getY2());
    }

    public LinePath(int x1, int y1, int x2, int y2, boolean loop) {
        this(new Line2D.Float(x1, y1, x2, y2), loop);
    }

    public LinePath(int x1, int y1, int x2, int y2, int maxDistance) {
        this(x1, y1, x2, y2, false);
        distance = maxDistance;
    }

    @Override
	public Point makeMove(float speed) {
		int resultX = (int) Math.round(speed * Math.sin(angle));
		int resultY = (int) Math.round(speed * Math.cos(angle));

        if (!loop) {
            distanceTraveled += origin.distance(origin.x + resultX, origin.y + resultY);
            if (distanceTraveled >= distance) {
                stop();
            }
        }
		return new Point(resultX, resultY);
	}

    @Override
	public void drawIndicator(Graphics g, Point p, int range) {
		Point movePoint;
        if (loop) {
            movePoint = move(range);   
        } else {
            loop = true;
            movePoint = move(range);
            loop = false;
        }
        Point farPoint = new Point(p.x + movePoint.x, p.y + movePoint.y);
		g.drawLine(farPoint.x, farPoint.y, p.x, p.y);
	}

}
