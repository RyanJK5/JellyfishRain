package bullethell.items;

public class Metal extends Item {

    @Override
    protected void setValues() {
        id = ItemID.METAL;
        canStack = true;
        name = "Metal";
    }

}
