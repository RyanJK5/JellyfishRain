package bullethell.items;

public class MagicDust extends Item {

    @Override
    protected void setValues() {
        name = "Magic Dust";
        canStack = true;
        id = ItemID.MAGIC_DUST;
    }
    
}
