package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockActiveProvider;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

public class ItemLogisticsFrameActiveProvider extends ItemLogisticsFrame {
    public ItemLogisticsFrameActiveProvider() {
        super(SemiBlockActiveProvider.ID);
    }

    @Override
    protected ContainerType<?> getContainerType() {
        return ModContainers.LOGISTICS_FRAME_PASSIVE_PROVIDER;
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return 0xFF93228C;
    }
}
