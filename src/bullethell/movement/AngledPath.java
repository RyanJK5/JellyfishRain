package bullethell.movement;

import java.awt.Point;
import java.awt.Graphics;

public class AngledPath extends Path {
	
	protected double angle;
	
	public AngledPath(double angle) {
		this.angle = Math.toRadians(angle);
	}

	public double getAngle() { return angle; }

	@Override
	public Point makeMove(float speed) {
		double speedX = speed * Math.sin(angle);
		double speedY = speed * Math.cos(angle);
		
		return new Point(speedX > 0 ? (int) (speedX + 0.5) : (int) (speedX - 0.5), 
						 speedY > 0 ? (int) (speedY + 0.5) : (int) (speedY - 0.5));
	}

	@Override
	public void drawIndicator(Graphics g, Point p, int range) {
		Point movePoint = move(range);
		Point farPoint = new Point(p.x + movePoint.x, p.y + movePoint.y);
		g.drawLine(farPoint.x, farPoint.y, p.x, p.y);
	}

	@Override
	public AngledPath clone() {
		return new AngledPath(angle);
	}
}
