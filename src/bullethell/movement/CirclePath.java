package bullethell.movement;

import java.awt.Graphics;
import java.awt.Point;

public class CirclePath extends Path {

	protected double radius;
	protected float increaseRate;
	protected float oTheta = 0;
	protected boolean clockwise = true;
	protected Point center;
	protected int pointX, pointY;
	
	public CirclePath(Point center, Point startPoint, float increaseRate, boolean clockwise) {
		super();
		this.increaseRate = increaseRate;
		this.clockwise = clockwise;
		this.center = center;
		this.pointX = (int) startPoint.getX();
		this.pointY = (int) startPoint.getY();
		radius = Point.distance(center.x, center.y, pointX, pointY);
		
	}
	
	public CirclePath(Point center, Point startPoint, boolean clockwise) {
		this(center, startPoint, 0, clockwise);
	}
	
	public CirclePath(int centerX, int centerY, int startPointX, int startPointY, boolean clockwise) {
		this(new Point(centerX, centerY), new Point(startPointX, startPointY), 0, clockwise);
	}
	
	public CirclePath(int centerX, int centerY, int startPointX, int startPointY, float increaseRate, boolean clockwise) {
		this(new Point(centerX, centerY), new Point(startPointX, startPointY), increaseRate, clockwise);
	}

	@Override
	public Point makeMove(float speed) {
		if (clockwise) oTheta += 0.1 * speed;
		else oTheta -= 0.1 * speed;
		
	    int resultX = (int) (radius * Math.cos(oTheta) + center.x + 0.5f);
	    int resultY = (int) (radius * Math.sin(oTheta) + center.y + 0.5f);
		
	    int tempPointX = pointX;
	    int tempPointY = pointY;
	    
	    pointX = resultX;
	    pointY = resultY;
	    
	    resultX -= tempPointX;
	    resultY -= tempPointY;
	    
	    radius += increaseRate;
	    
	    return new Point(resultX, resultY);		
	}

	@Override
	public void drawIndicator(Graphics g, Point p, int range) {
		double radius = Point.distance(center.x, center.y, p.x, p.y);
		int arcX = (int) (center.x - radius);
		int arcY = (int) (center.y - radius);

		g.drawArc(arcX, arcY, (int) (radius * 2), (int) (radius * 2), 0, 360);
	}

	public void setCenter(Point center) { this.center = center; }
	public void setIncrRate(float incrRate) { this.increaseRate = incrRate; }

	@Override
	public CirclePath clone() {
		return new CirclePath(center.x, center.y, pointX, pointY, increaseRate, clockwise);
	}

	public Point getCenter() { return center; }
	public Point getCurrent() { return new Point(pointX, pointY); }
	public float getIncrRate() { return increaseRate; }
	public boolean clockwise() { return clockwise; }
}
