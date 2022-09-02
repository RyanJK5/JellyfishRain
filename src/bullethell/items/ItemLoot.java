package bullethell.items;

public class ItemLoot {
    
    public final ItemID item;
    public final float chance;
    public final int minAmount;
    public final int maxAmount;

    public ItemLoot(ItemID itemID, float chance, int minAmount, int maxAmount) {
        this.item = itemID;
        this.chance = chance;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public ItemLoot(ItemID itemID, float chance) {
        if (!ItemID.getItem(itemID).canStack) {
            throw new IllegalArgumentException("itemID must refer to a stackable item");
        }
        this.item = itemID;
        this.chance = chance;
        minAmount = 1;
        maxAmount = 1;
    }

}
