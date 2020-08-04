package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemGlycerol extends Item {
    public ItemGlycerol() {
        super(ModItems.defaultProps());
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return 800;
    }
}
