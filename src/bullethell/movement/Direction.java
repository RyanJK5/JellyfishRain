package bullethell.movement;

public enum Direction {
	UP(0,-1), 
	DOWN(0,1), 
	LEFT(-1,0), 
	RIGHT(1,0);

	public final int xDif, yDif;
	private Direction(int x, int y) {
		this.xDif = x;
		this.yDif = y;
	}
	
	public static Direction[] getArray(int dx, int dy) {
		Direction dir1;
		Direction dir2;
		switch (dx) {
			case 1:
				dir1 = RIGHT;
				break;
			case -1:
				dir1 = LEFT;
				break;
			default:
				dir1 = null;
				break;
		}
		switch (dy) {
			case 1:
				dir2 = DOWN;
				break;
			case -1:
				dir2 = UP;
				break;
			default:
				dir2 = null;
				break;
		}
		if (dir1 == null) {
			return new Direction[] {dir2};
		}
		if (dir2 == null) {
			return new Direction[] {dir1};
		}
		return new Direction[] {dir1, dir2};
	}
}