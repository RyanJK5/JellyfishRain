package bullethell.movement;

import java.awt.Graphics;
import java.awt.Point;

public class StraightPath extends Path {
	
	public final int xDif, yDif;
	private Direction[] direction;

	public StraightPath(Direction direction) {
		Direction[] dirArr = {direction};
		this.direction = dirArr;
		this.xDif = direction.xDif;
		this.yDif = direction.yDif;
	}

	public StraightPath(Direction[] directions) {
		this.direction = directions;
		int xDif = 0;
		int yDif = 0;
		for (int i = 0; i < directions.length; i++) {
			Direction direction = directions[i];
			xDif += direction.xDif;
			yDif += direction.yDif;
		}
		this.xDif = xDif;
		this.yDif = yDif;
	}

	public StraightPath(double angle) {
		if (angle == 0 || angle == 360) {
			direction = new Direction[] {Direction.RIGHT};
		} else if (angle == 90) {
			direction = new Direction[] {Direction.DOWN};
		} else if (angle == 180) {
			direction = new Direction[] {Direction.LEFT};
		} else if (angle == 270) {
			direction = new Direction[] {Direction.UP};
		} else {
			throw new IllegalArgumentException("angle (" + angle + ") must be right");
		}
		this.xDif = direction[0].xDif;
		this.yDif = direction[0].yDif;
	}
	
	public StraightPath(int xDif, int yDif) {
		this.xDif = xDif;
		this.yDif = yDif;
	}

	@Override
	public Point makeMove(float speed) {
		Point p = new Point((int) (xDif * speed), (int) (yDif * speed));
		return p;
	}

	@Override
	public void drawIndicator(Graphics g, Point p, int range) {
		int x1 = p.x;
		int y1 = p.y;
		int x2 = x1; 
		int y2 = y1;
		
		if (xDif != 0) {
			x2 += xDif * range;
		}
		if (yDif != 0) {
			y2 += yDif * range;
		}

		g.drawLine(x1, y1, x2, y2);
	}

	@Override
	public StraightPath clone() {
		return new StraightPath(direction);
	}
}
