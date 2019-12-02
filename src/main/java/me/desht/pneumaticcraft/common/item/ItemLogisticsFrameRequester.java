package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

public class ItemLogisticsFrameRequester extends ItemLogisticsFrame {

    public ItemLogisticsFrameRequester() {
        super(SemiBlockRequester.ID);
    }

    @Override
    protected ContainerType<?> getContainerType() {
        return ModContainerTypes.LOGISTICS_FRAME_REQUESTER;
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return 0xFF0000FF;
    }
}
