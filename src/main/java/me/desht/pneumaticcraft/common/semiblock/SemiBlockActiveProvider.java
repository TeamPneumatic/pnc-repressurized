package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class SemiBlockActiveProvider extends SemiBlockLogistics implements ISpecificProvider {
    public static final ResourceLocation ID = RL("logistics_frame_active_provider");

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean canProvide(ItemStack providingStack) {
        return passesFilter(providingStack);
    }

    @Override
    public boolean canProvide(FluidStack providingStack) {
        return passesFilter(providingStack.getFluid());
    }

    @Override
    public ITextComponent getDisplayName() {
        return new ItemStack(ModItems.LOGISTICS_FRAME_ACTIVE_PROVIDER.get()).getDisplayName();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        // same container as passive provider
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), i, playerInventory, getPos());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
