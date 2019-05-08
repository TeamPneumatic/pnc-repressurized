package me.desht.pneumaticcraft.common.itemblock;

import me.desht.pneumaticcraft.common.block.BlockLiquidHopper;
import me.desht.pneumaticcraft.common.capabilities.FluidItemWrapper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemBlockLiquidHopper extends ItemBlock {
    public ItemBlockLiquidHopper(BlockLiquidHopper blockLiquidHopper) {
        super(blockLiquidHopper);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidItemWrapper(stack,"Tank", PneumaticValues.NORMAL_TANK_CAPACITY);
    }
}
