package bullethell.movement;

import java.awt.Point;

import bullethell.GameObject;

public class ChargePath extends SeekingPath {
    
	private int chargesDone = 0;
    private int chargeNum = 0;

    private final int chargeTime;
    private final int chargeDelay;

    private int age = 0;

    public ChargePath(GameObject predator, GameObject prey, int chargeTime, int chargeDelay) {
        super(predator, prey, false);
        this.chargeTime = chargeTime;
        this.chargeDelay = chargeDelay;
    }
    
    @Override
    public Point move(float speed) {
        if (!active) return new Point(0,0);

        
        if (age <= chargeTime) {
            age++;
            return makeMove(speed);
        } 
        if (age > chargeTime + chargeDelay) {
            age = 0;
            chargesDone++;
            if (chargeNum > 0 && chargesDone >= chargeNum) {
                active = false;
                return new Point(0, 0);
            }
            setLoop(true);
            Point result = makeMove(speed);
            setLoop(false);
            return result;
        }
        
        age++;
        return new Point(0,0);
    }

    public void setNumOfCharges(int chargeNum) { this.chargeNum = chargeNum; }
    public int getNumOfCharges() { return chargeNum; }
    public int getChargeTime() { return chargeTime; }
    public int getChargeDelay() { return chargeDelay; }
}
