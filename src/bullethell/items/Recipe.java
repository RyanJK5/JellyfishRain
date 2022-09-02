package bullethell.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import bullethell.Player;

public class Recipe {

	private final ItemID[] components;
    private final int[] componentCounts;

    private final ItemID result;
    private final int resultCount;

    public Recipe(ItemID[] components, ItemID result, int[] componentCounts, int resultCount) {
        Objects.requireNonNull(components);
        this.components = components;
        this.componentCounts = componentCounts;
        this.result = result;
        this.resultCount = resultCount;
        Player.get().addRecipe(this);
    }

    public Item craft(Item[] items) {
        List<Item> seenItems = new ArrayList<>();
        HashMap<Item, Integer> providedStackablesToRecipe = new HashMap<>();
        for (int j = 0; j < components.length; j++) {
            ItemID itemID = components[j];

            boolean successful = false;
            outer: for (int i = 0; i < items.length; i++) {
                Item oItem = items[i];

                for (Item dupeItem : seenItems) {
                    if (dupeItem.equals(oItem)) {
                        continue outer;
                    }
                }
                seenItems.add(oItem);

                if (itemID != oItem.id) {
                    continue;
                }
                if (ItemID.getItem(itemID).canStack && oItem.canStack) {
                    if (oItem.count >= componentCounts[j]) {
                        providedStackablesToRecipe.put(oItem, componentCounts[j]);
                    } else {
                        continue;
                    }
                }
                successful = true;
                break;
            }
            if (!successful) {
                return null;
            }
        }

        for (Item item : seenItems) {
            if (item.canStack) {
                item.take(providedStackablesToRecipe.get(item));
                if (item.count == 0) {
                    item.kill();
                }
            }
        }
        return ItemID.getItem(result).clone(resultCount);
    }

    public Item craft(List<Item> items) {
        return craft(items.toArray(new Item[0]));
    }

    public Item craftFromInv() {
        return craft(Player.get().getInventory().hasItems(components));
    }

    public boolean canCraftFromInv() {
        List<Item> testInv = new ArrayList<>();
        for (Item item : Player.get().getInventory().hasItems(components)) {
            testInv.add((Item) item.clone(item.count));
        }
        return craft(testInv) != null;
        
    }

    public ItemID[] getComponents() { return components; }
    public int[] getComponentCounts() { return componentCounts; }
    public ItemID getResult() { return result; }

    public List<ItemID> getComponentList() {
        List<ItemID> result = new ArrayList<>();
        for (ItemID item : components) {
            result.add(item);
        }
        return result;
    }
}
