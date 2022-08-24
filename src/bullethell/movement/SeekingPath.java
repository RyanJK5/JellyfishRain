package bullethell.movement;

import java.awt.Graphics;
import java.awt.Point;

import bullethell.GameObject;

public class SeekingPath extends Path {

	protected GameObject predator;
	protected GameObject prey;
	protected boolean loop;
	private double angle;
	
	public SeekingPath(GameObject predator, GameObject prey) {
		this(predator, prey, true);
	}

	public SeekingPath(GameObject predator, GameObject prey, boolean loop) {
		this.predator = predator;
		this.prey = prey;
		this.loop = loop;

		angle = Math.atan2(predator.getCenterX() - prey.getCenterX(), predator.getCenterY() - prey.getCenterY());
		angle = Math.toDegrees(angle);
		angle += 180;
		if (angle < 0) angle += 360;
	}

	public void setLoop(boolean loop) { this.loop = loop; }

	@Override
	public Point makeMove(float speed) {
		if (loop) {
			angle = Math.atan2(predator.getCenterX() - prey.getCenterX(), predator.getCenterY() - prey.getCenterY());
			angle = Math.toDegrees(angle);
			angle += 180;
			if (angle < 0) angle += 360;
		}
		
		Path angledPath = new AngledPath(angle);
		return angledPath.move(speed);
	}

	@Override
	public void drawIndicator(Graphics g, Point p, int range) {
		if (loop) {
			g.drawLine(p.x, p.y, prey.getCenterX(), prey.getCenterY());
		} else {
			Point movePoint = new AngledPath(angle).move(range);
			Point farPoint = new Point(p.x + movePoint.x, p.y + movePoint.y);
			g.drawLine(p.x, p.y, farPoint.x, farPoint.y);
		}
	}

	public GameObject getPredator() { return predator; }
	public GameObject getPrey() { return prey; }

	@Override
	public SeekingPath clone() {
		return new SeekingPath(predator, prey, loop);
	}
}
