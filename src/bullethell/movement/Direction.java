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
}