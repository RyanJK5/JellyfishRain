package bullethell.items;

public class WeaponModifiers extends AbstractModifiers {
    
    @Modifier(isMultiplier = true)
    public float mDMG = 0;
    @Modifier(isMultiplier = true)
    public float mFireTime = 0;
    @Modifier(isMultiplier = true)
    public float mRange = 0;

    @Modifier(isMultiplier = false)
    public int pDMG = 0;
    @Modifier(isMultiplier = false)
    public int pFireTime = 0;
    @Modifier(isMultiplier = false)
    public int pRange = 0;
}