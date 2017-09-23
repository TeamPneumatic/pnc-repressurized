package me.desht.pneumaticcraft.common.item;

public abstract class ItemPneumaticSubtyped extends ItemPneumatic {
    public ItemPneumaticSubtyped(String registryName) {
        super(registryName);
        setHasSubtypes(true);
    }

    /**
     * Get the subtype name for discovering the item's model.
     *
     * @return model name to use
     */
    public abstract String getSubtypeModelName(int meta);
}
