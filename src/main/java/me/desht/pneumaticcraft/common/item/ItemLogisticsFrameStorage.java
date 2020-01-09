package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockStorage;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

public class ItemLogisticsFrameStorage extends ItemLogisticsFrame {

    public ItemLogisticsFrameStorage() {
        super(SemiBlockStorage.ID);
    }

    @Override
    protected ContainerType<?> getContainerType() {
        return ModContainers.LOGISTICS_FRAME_STORAGE;
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return 0xFFFFFF00;
    }
}
