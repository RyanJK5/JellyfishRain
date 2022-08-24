package bullethell.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import bullethell.Player;

public class Recipe extends Item {

	private final Item[] components;
    private final Item result;

    public Recipe(Item[] components, Item result) {
        super(result.getSprite(), result.getName());
        Objects.requireNonNull(components);
        Objects.requireNonNull(result);
        this.components = new Item[components.length];
        for (int i = 0; i < this.components.length; i++) {
            this.components[i] = (Item) components[i].clone();
        }
        this.result = (Item) result.clone();
        updateData();
    }

    @Override
    public void updateData() {
        setData(result.getData());
    }

    public Item craft(Item[] items) {
        List<Item> seenItems = new ArrayList<>();
        HashMap<StackableItem, StackableItem> providedStackablesToRecipe = new HashMap<>();
        for (Item item : components) {
            boolean successful = false;
            outer: for (int i = 0; i < items.length; i++) {
                Item oItem = items[i];

                for (Item dupeItem : seenItems) {
                    if (dupeItem.equals(oItem)) {
                        continue outer;
                    }
                }
                seenItems.add(oItem);

                if (!item.equals(oItem)) {
                    continue;
                }
                if (item instanceof StackableItem stack && oItem instanceof StackableItem oStack) {
                    if (oStack.getCount() >= stack.getCount()) {
                        providedStackablesToRecipe.put(oStack, stack);
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
            if (item instanceof StackableItem stack) {
                stack.take(providedStackablesToRecipe.get(stack).getCount());
            } else {
                item.kill();
            }
        }
        return (Item) result.clone();
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
            testInv.add((Item) item.clone());
        }
        return craft(testInv) != null;
        
    }

    public Item[] getComponents() { return components; }

    public List<Item> getComponentList() {
        List<Item> result = new ArrayList<>();
        for (Item item : components) {
            result.add(item);
        }
        return result;
    }

    public Recipe clone() {
        Recipe obj = new Recipe(components, result);
		obj.setLocation(getLocation());
		obj.setData(getData());
        if (!isAlive()) {
            obj.kill();
        }
		obj.setEssential(isEssential());
        return obj;
    }

    @Override
    public boolean equals(Item item) {
        return item instanceof Recipe recipe && result.equals(recipe.result);
    }
}
