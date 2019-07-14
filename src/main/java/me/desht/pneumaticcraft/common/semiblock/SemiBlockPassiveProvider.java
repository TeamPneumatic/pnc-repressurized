package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class SemiBlockPassiveProvider extends SemiBlockActiveProvider {
    public static final String ID = "logistics_frame_passive_provider";

    @Override
    public int getColor() {
        return 0xFFFF0000;
    }

    @Override
    public boolean shouldProvideTo(int priority) {
        return priority > 2;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new ItemStack(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER).getDisplayName();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerLogistics(ModContainerTypes.LOGISTICS_FRAME_PASSIVE_PROVIDER, i, playerInventory, getPos());
    }
}
