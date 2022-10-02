package bullethell.combat;

public enum EnchantmentPool {
    MELEE_WEAPON;

    private final EnchantmentType[] types;

    private EnchantmentPool(EnchantmentType... types) {
        this.types = types;
    }

    public EnchantmentType[] getTypes() {
        return types;
    }
}
