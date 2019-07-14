package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockStorage;
import net.minecraft.inventory.container.ContainerType;

public class ItemLogisticsFrameStorage extends ItemLogisticsFrame {

    public ItemLogisticsFrameStorage() {
        super(SemiBlockStorage.ID);
    }

    @Override
    protected ContainerType<?> getContainerType() {
        return ModContainerTypes.LOGISTICS_FRAME_STORAGE;
    }
}
