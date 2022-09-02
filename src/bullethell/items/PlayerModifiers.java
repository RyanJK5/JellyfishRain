package bullethell.items;

public class PlayerModifiers extends AbstractModifiers {

    @Modifier(isMultiplier = true)
    public float mHP = 0;
    @Modifier(isMultiplier = true)
    public float mInvincTime = 0;
    @Modifier(isMultiplier = true)
    public float mRegen = 0;
    @Modifier(isMultiplier = true)
    public float mSpeed = 0;

    @Modifier(isMultiplier = false)
    public int pHP = 0;
    @Modifier(isMultiplier = false)
    public int pInvincTime = 0;
    @Modifier(isMultiplier = false)
    public int pRegen = 0;
    @Modifier(isMultiplier = false)
    public int pSpeed = 0;
}
