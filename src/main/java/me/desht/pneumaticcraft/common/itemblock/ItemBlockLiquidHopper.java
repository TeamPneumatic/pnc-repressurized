package me.desht.pneumaticcraft.common.itemblock;

import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nullable;

public class ItemBlockLiquidHopper extends BlockItem {
    public static final String TANK_NAME = "Tank";

    public ItemBlockLiquidHopper(Block block) {
        super(block, ModItems.defaultProps());
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        boolean creative = UpgradableItemUtils.hasCreativeUpgrade(itemStack);
        return FluidUtil.getFluidHandler(itemStack).map(handler -> {
            // TODO can (or indeed should) we support recipes which drain amounts other than 1000mB?
            handler.drain(1000, creative ? FluidAction.SIMULATE : FluidAction.EXECUTE);
            return handler.getContainer().copy();
        }).orElseThrow(RuntimeException::new);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new FluidItemWrapper(stack,TANK_NAME, PneumaticValues.NORMAL_TANK_CAPACITY);
    }
}
