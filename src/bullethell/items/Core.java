package bullethell.items;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Core extends Item {
	private float[] multipliers = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private int[] bonuses = new int[10];

    public Core(String name) throws IOException {
        super(name);
        updateData();
    }

    public Core(BufferedImage sprite, String name) throws IOException {
        super(sprite, name);
        updateData();
    }
    
    @Override
    public void updateData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < multipliers.length; j++) {
                String num = (i == 0 ? Float.toString(multipliers[j]) : 
                  Integer.toString(bonuses[j]));
				if (i == 0) {
                    num = num.substring(0, num.indexOf('.') + (i == 0 ? 2 : 0));
                }
                if (i == 0 ? multipliers[j] != 1 : bonuses[j] != 0) {
                    String str = (i == 0 ? "x" : (bonuses[j] > 0 ? "+" : "")) + num + " ";
                    switch(j) {
                        case 0:
                            str += "HP";
                            break;
                        case 1:
                            str += "damage";
                            break;
                        case 2:
                            str += "fire time";
                            break;
                        case 3:
                            str += "range";
                            break;
                        case 4:
                            str = "Increased invincibility time";
                            break;
                        case 5:
                            str = (i == 0 ? multipliers[j] > 1 : bonuses[j] > 0) ? "Increased regeneration" 
                                : "Decreased regeneration";
                            j += 2;
                            break;
                        case 6:
                            str = (i == 0 ? multipliers[j] > 1 : bonuses[j] > 0) ? "Increased regeneration" 
                                : "Decreased regeneration";
                            j++;
                            break;
                        case 7:
                            str = (i == 0 ? multipliers[j] > 1 : bonuses[j] > 0) ? "Increased regeneration" 
                                : "Decreased regeneration";
                            break;
                        case 8:
                            str += "shot speed";
                            break;
                        case 9:
                            str += "speed";
                            break;
                    }
                    strings.add(str);
                }
            }
            strings.add("");
        }
       
        String[] arr = new String[strings.size() + 1];
        arr[0] = getName();
        for (int i = 0; i < strings.size(); i++) {
            arr[i + 1] = "    " + strings.get(i);
        }
        setData(arr);
    }

    public void setMultipliers(float[] multipliers) { 
        this.multipliers = multipliers;
        updateData(); 
    }
    public void setBonuses(int[] bonuses) { 
        this.bonuses = bonuses;
        updateData();
    }

    public float[] getMultipliers() { return multipliers; }
    public int[] getBonuses() { return bonuses; }

    @Override
    public Core clone() {
        try {
            Core obj = new Core(sprite, name);
            obj.setLocation(getLocation());
            if (!isAlive()) {
                obj.kill();
            }
            obj.setEssential(isEssential());
            obj.setMultipliers(multipliers);
            obj.setBonuses(bonuses);
            return obj;
        } catch (IOException e) {
            return null;
        }
    }
}