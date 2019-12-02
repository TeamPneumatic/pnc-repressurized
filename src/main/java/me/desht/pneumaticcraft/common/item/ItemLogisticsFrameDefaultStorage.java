package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockDefaultStorage;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

public class ItemLogisticsFrameDefaultStorage extends ItemLogisticsFrame {

    public ItemLogisticsFrameDefaultStorage() {
        super(SemiBlockDefaultStorage.ID);
    }

    @Override
    protected ContainerType<?> getContainerType() {
        return ModContainerTypes.LOGISTICS_FRAME_DEFAULT_STORAGE;
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return 0xFF008800;
    }
}
