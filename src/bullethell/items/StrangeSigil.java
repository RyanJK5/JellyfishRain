package bullethell.items;

public class StrangeSigil extends Item {

    @Override
    protected void setValues() {
        name = "Strange Sigil";
        id = ItemID.STRANGE_SIGIL;
    }

    @Override
    protected void addRecipes() {
        new Recipe(new ItemID[] {ItemID.MAGIC_DUST, ItemID.METAL}, id, new int[] {4, 6}, 1);
    }
    
}
