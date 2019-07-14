package me.desht.pneumaticcraft.common.itemblock;

import me.desht.pneumaticcraft.common.block.BlockLiquidHopper;
import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.common.item.ItemPneumatic;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemBlockLiquidHopper extends BlockItem {
    public ItemBlockLiquidHopper(BlockLiquidHopper blockLiquidHopper) {
        super(blockLiquidHopper, ItemPneumatic.DEFAULT_PROPS);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new FluidItemWrapper(stack,"Tank", PneumaticValues.NORMAL_TANK_CAPACITY);
    }
}
