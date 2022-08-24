package bullethell.movement;

import java.awt.Graphics;
import java.awt.Point;

public abstract class Path {
	

	public static final Path DEFAULT_PATH = new Path() {
		@Override
		protected Point makeMove(float speed) {
			return new Point(0, 0);
		}
		@Override
		public void drawIndicator(Graphics g, Point p, int range) { }
		@Override
		public Path clone() { return this; }
	};

	protected static final int DEFAULT_INDICATOR_DRAW_DISTANCE = 6000;
	protected boolean active = true;
	protected boolean drawIndicator = false;

	public Path() { }

	public Point move(float speed) {
		return active ? makeMove(speed) : new Point(0,0);
	};

	protected abstract Point makeMove(float speed);

	public abstract void drawIndicator(Graphics g, Point p, int range);

	public abstract Path clone();
	public final boolean equals(Path p) {
		return getClass() == p.getClass();
	};

	public final void drawIndicator(Graphics g, Point p) {
		drawIndicator(g, p, DEFAULT_INDICATOR_DRAW_DISTANCE);
	}

	public boolean isActive() { return active; }

	public void start() { 
		active = true;
	}

	public void stop() {
		active = false;
	}
}
